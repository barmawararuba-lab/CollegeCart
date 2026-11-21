package com.collegecart.service;

import com.collegecart.dto.CheckoutOrderResponse;
import com.collegecart.dto.CheckoutRequest;
import com.collegecart.dto.VendorOrderResponse;
import com.collegecart.dto.VendorOrderStatusUpdateRequest;
import com.collegecart.entity.Cart;
import com.collegecart.entity.CartItem;
import com.collegecart.entity.CheckoutOrder;
import com.collegecart.entity.OrderItem;
import com.collegecart.entity.OrderStatusHistory;
import com.collegecart.entity.Product;
import com.collegecart.entity.User;
import com.collegecart.entity.VendorOrder;
import com.collegecart.entity.VendorProfile;
import com.collegecart.enums.ApprovalStatus;
import com.collegecart.enums.OrderStatus;
import com.collegecart.enums.Role;
import com.collegecart.exception.ResourceNotFoundException;
import com.collegecart.repository.CartItemRepository;
import com.collegecart.repository.CartRepository;
import com.collegecart.repository.CheckoutOrderRepository;
import com.collegecart.repository.OrderItemRepository;
import com.collegecart.repository.ProductRepository;
import com.collegecart.repository.UserRepository;
import com.collegecart.repository.VendorOrderRepository;
import com.collegecart.repository.VendorProfileRepository;
import com.collegecart.security.VendorApprovalChecker;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final int ORDER_NUMBER_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CheckoutOrderRepository checkoutOrderRepository;
    private final VendorOrderRepository vendorOrderRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final VendorApprovalChecker vendorApprovalChecker;

    public OrderService(
            UserRepository userRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository,
            CheckoutOrderRepository checkoutOrderRepository,
            VendorOrderRepository vendorOrderRepository,
            VendorProfileRepository vendorProfileRepository,
            VendorApprovalChecker vendorApprovalChecker) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.checkoutOrderRepository = checkoutOrderRepository;
        this.vendorOrderRepository = vendorOrderRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.vendorApprovalChecker = vendorApprovalChecker;
    }

    @Transactional
    public CheckoutOrderResponse placeOrder(
            CheckoutRequest request,
            Authentication authentication) {
        User student = resolveActiveStudent(authentication);
        if (request == null || request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("A payment method is required");
        }

        Cart cart = cartRepository.findByUserId(student.getId())
                .orElseThrow(() -> new IllegalArgumentException("The cart is empty"));
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty");
        }

        LinkedHashSet<Long> requestedProductIds = new LinkedHashSet<>();
        Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct() == null || cartItem.getProduct().getId() == null) {
                throw new ResourceNotFoundException("A product in the cart no longer exists");
            }
            Long productId = cartItem.getProduct().getId();
            int quantity = requirePositiveQuantity(cartItem.getQuantity());
            requestedProductIds.add(productId);
            try {
                requestedQuantities.merge(productId, quantity, Math::addExact);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("Requested product quantity is too large");
            }
        }

        List<Product> lockedProducts = productRepository
                .findAllByIdInSortedForUpdate(requestedProductIds);
        Map<Long, Product> productById = new LinkedHashMap<>();
        for (Product product : lockedProducts) {
            productById.put(product.getId(), product);
        }

        Map<Long, VendorGroup> groupsByVendorId = new LinkedHashMap<>();
        for (Long productId : requestedProductIds) {
            Product product = productById.get(productId);
            if (product == null) {
                throw new ResourceNotFoundException("A product in the cart no longer exists");
            }
            validateProductForCheckout(product, requestedQuantities.get(productId));

            VendorProfile vendorProfile = product.getVendorProfile();
            VendorGroup group = groupsByVendorId.computeIfAbsent(
                    vendorProfile.getId(), ignored -> new VendorGroup(vendorProfile));
            for (CartItem cartItem : cartItems) {
                if (cartItem.getProduct().getId().equals(productId)) {
                    group.cartItems.add(cartItem);
                }
            }
        }

        CheckoutOrder checkoutOrder = new CheckoutOrder();
        checkoutOrder.setStudent(student);
        checkoutOrder.setOrderNumber(generateUniqueOrderNumber());
        checkoutOrder.setPaymentMethod(request.getPaymentMethod());
        checkoutOrder.setPickupNote(normalizeOptionalNote(request.getPickupNote()));
        checkoutOrder.setTotal(BigDecimal.ZERO);
        checkoutOrder.setVendorOrders(new ArrayList<>());

        BigDecimal checkoutTotal = BigDecimal.ZERO;
        for (VendorGroup group : groupsByVendorId.values()) {
            VendorOrder vendorOrder = createVendorOrder(checkoutOrder, group, productById);
            checkoutOrder.getVendorOrders().add(vendorOrder);
            checkoutTotal = checkoutTotal.add(vendorOrder.getTotal());
        }
        checkoutOrder.setTotal(checkoutTotal);

        CheckoutOrder savedOrder = checkoutOrderRepository.save(checkoutOrder);

        for (Product product : lockedProducts) {
            int quantity = requestedQuantities.get(product.getId());
            product.setStock(product.getStock() - quantity);
        }
        productRepository.saveAll(lockedProducts);
        cartItemRepository.deleteAll(cartItems);

        return new CheckoutOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<CheckoutOrderResponse> findStudentOrders(Authentication authentication) {
        User student = resolveActiveStudent(authentication);
        return checkoutOrderRepository.findByStudentIdOrderByPlacedAtDesc(student.getId())
                .stream()
                .map(CheckoutOrderResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public CheckoutOrderResponse findStudentOrder(
            Long checkoutOrderId,
            Authentication authentication) {
        User student = resolveActiveStudent(authentication);
        CheckoutOrder checkoutOrder = checkoutOrderRepository
                .findByIdAndStudentId(checkoutOrderId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new CheckoutOrderResponse(checkoutOrder);
    }

    @Transactional(readOnly = true)
    public List<VendorOrderResponse> findVendorOrders(Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        return vendorOrderRepository.findByVendorProfileIdOrderByCreatedAtDesc(vendorProfile.getId())
                .stream()
                .map(VendorOrderResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendorOrderResponse findVendorOrder(
            Long vendorOrderId,
            Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        VendorOrder vendorOrder = vendorOrderRepository
                .findByIdAndVendorProfileId(vendorOrderId, vendorProfile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new VendorOrderResponse(vendorOrder);
    }

    @Transactional
    public VendorOrderResponse updateVendorOrderStatus(
            Long vendorOrderId,
            VendorOrderStatusUpdateRequest request,
            Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("A new order status is required");
        }

        VendorOrder vendorOrder = vendorOrderRepository
                .findByIdAndVendorProfileIdForUpdate(vendorOrderId, vendorProfile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus previousStatus = vendorOrder.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (!isAllowedTransition(previousStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Order status cannot change from " + previousStatus + " to " + newStatus);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restoreStockForCancellation(vendorOrder);
        }

        vendorOrder.setStatus(newStatus);
        OrderStatusHistory history = new OrderStatusHistory();
        history.setVendorOrder(vendorOrder);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(normalizeOptionalNote(request.getNote()));
        vendorOrder.getStatusHistory().add(history);

        VendorOrder savedOrder = vendorOrderRepository.save(vendorOrder);
        return new VendorOrderResponse(savedOrder);
    }

    private VendorOrder createVendorOrder(
            CheckoutOrder checkoutOrder,
            VendorGroup group,
            Map<Long, Product> productById) {
        VendorOrder vendorOrder = new VendorOrder();
        vendorOrder.setCheckoutOrder(checkoutOrder);
        vendorOrder.setVendorProfile(group.vendorProfile);
        vendorOrder.setStatus(OrderStatus.PENDING);
        vendorOrder.setTotal(BigDecimal.ZERO);
        vendorOrder.setItems(new ArrayList<>());
        vendorOrder.setStatusHistory(new ArrayList<>());

        BigDecimal vendorTotal = BigDecimal.ZERO;
        for (CartItem cartItem : group.cartItems) {
            Product product = productById.get(cartItem.getProduct().getId());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setVendorOrder(vendorOrder);
            orderItem.setProduct(product);
            orderItem.setProductNameSnapshot(product.getName());
            orderItem.setProductImageUrlSnapshot(product.getImageUrl());
            orderItem.setUnitPriceSnapshot(unitPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            vendorOrder.getItems().add(orderItem);
            vendorTotal = vendorTotal.add(subtotal);
        }
        vendorOrder.setTotal(vendorTotal);

        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setVendorOrder(vendorOrder);
        initialHistory.setPreviousStatus(null);
        initialHistory.setNewStatus(OrderStatus.PENDING);
        initialHistory.setNote("Order placed");
        vendorOrder.getStatusHistory().add(initialHistory);

        return vendorOrder;
    }

    private void validateProductForCheckout(Product product, int quantity) {
        if (!product.isActive()) {
            throw new IllegalArgumentException("Inactive products cannot be checked out");
        }
        if (product.getStock() == null || quantity > product.getStock()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Product has an invalid price");
        }

        VendorProfile vendorProfile = product.getVendorProfile();
        if (vendorProfile == null || vendorProfile.getId() == null
                || vendorProfile.getUser() == null) {
            throw new IllegalArgumentException("A product's vendor is unavailable");
        }
        User vendorUser = vendorProfile.getUser();
        if (vendorUser.getRole() != Role.VENDOR || !vendorUser.isActive()) {
            throw new IllegalArgumentException("A product's vendor is not active");
        }
        if (vendorProfile.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalArgumentException("A product's vendor is not approved");
        }
    }

    private boolean isAllowedTransition(OrderStatus currentStatus, OrderStatus requestedStatus) {
        if (currentStatus == null || requestedStatus == null) {
            return false;
        }
        return switch (currentStatus) {
            case PENDING -> requestedStatus == OrderStatus.ACCEPTED
                    || requestedStatus == OrderStatus.CANCELLED;
            case ACCEPTED -> requestedStatus == OrderStatus.PREPARING
                    || requestedStatus == OrderStatus.CANCELLED;
            case PREPARING -> requestedStatus == OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> requestedStatus == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private void restoreStockForCancellation(VendorOrder vendorOrder) {
        List<OrderItem> orderItems = orderItemRepository
                .findByVendorOrderIdOrderByIdAsc(vendorOrder.getId());
        LinkedHashMap<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            if (product == null || product.getId() == null) {
                continue;
            }
            int quantity = requirePositiveQuantity(orderItem.getQuantity());
            try {
                quantitiesByProductId.merge(product.getId(), quantity, Math::addExact);
            } catch (ArithmeticException exception) {
                throw new IllegalStateException("Cancellation stock quantity is too large");
            }
        }

        if (quantitiesByProductId.isEmpty()) {
            return;
        }

        List<Product> lockedProducts = productRepository
                .findAllByIdInSortedForUpdate(quantitiesByProductId.keySet());
        Map<Long, Product> lockedProductById = new LinkedHashMap<>();
        for (Product product : lockedProducts) {
            lockedProductById.put(product.getId(), product);
        }

        for (Map.Entry<Long, Integer> entry : quantitiesByProductId.entrySet()) {
            Product product = lockedProductById.get(entry.getKey());
            if (product == null) {
                // The product was deleted after the order; its snapshots remain in the order item.
                continue;
            }
            if (product.getStock() == null) {
                throw new IllegalStateException("Product stock is unavailable for restoration");
            }
            try {
                product.setStock(Math.addExact(product.getStock(), entry.getValue()));
            } catch (ArithmeticException exception) {
                throw new IllegalStateException("Product stock is too large to restore safely");
            }
        }
        productRepository.saveAll(lockedProducts);
    }

    private User resolveActiveStudent(Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        if (!user.isActive()) {
            throw new AccessDeniedException("Inactive accounts cannot access orders");
        }
        if (user.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only students can access student orders");
        }
        return user;
    }

    private VendorProfile resolveApprovedVendor(Authentication authentication) {
        User user = resolveAuthenticatedUser(authentication);
        if (!user.isActive()) {
            throw new AccessDeniedException("Inactive accounts cannot access vendor orders");
        }
        if (user.getRole() != Role.VENDOR) {
            throw new AccessDeniedException("Only vendors can access vendor orders");
        }

        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        if (vendorProfile.getApprovalStatus() != ApprovalStatus.APPROVED
                || !vendorApprovalChecker.isApproved(authentication)) {
            throw new AccessDeniedException("Vendor approval is required to access orders");
        }
        return vendorProfile;
    }

    private User resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Authentication is required");
        }

        String email = authentication.getName().trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private int requirePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Cart quantity must be at least one");
        }
        return quantity;
    }

    private String generateUniqueOrderNumber() {
        for (int attempt = 0; attempt < ORDER_NUMBER_ATTEMPTS; attempt++) {
            String candidate = "CC-" + UUID.randomUUID().toString()
                    .replace("-", "")
                    .toUpperCase(Locale.ROOT);
            if (!checkoutOrderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique order number");
    }

    private String normalizeOptionalNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        return note.trim();
    }

    private static final class VendorGroup {

        private final VendorProfile vendorProfile;
        private final List<CartItem> cartItems = new ArrayList<>();

        private VendorGroup(VendorProfile vendorProfile) {
            this.vendorProfile = vendorProfile;
        }
    }
}
