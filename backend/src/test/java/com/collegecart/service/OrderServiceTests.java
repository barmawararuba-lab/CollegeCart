package com.collegecart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collegecart.dto.CheckoutOrderResponse;
import com.collegecart.dto.CheckoutRequest;
import com.collegecart.dto.OrderItemResponse;
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
import com.collegecart.enums.PaymentMethod;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CheckoutOrderRepository checkoutOrderRepository;
    @Mock private VendorOrderRepository vendorOrderRepository;
    @Mock private VendorProfileRepository vendorProfileRepository;
    @Mock private VendorApprovalChecker vendorApprovalChecker;
    @Mock private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    private User student;
    private Cart cart;
    private User vendorUser;
    private VendorProfile vendorProfile;

    @BeforeEach
    void setUp() {
        student = user(10L, "student@example.edu", Role.STUDENT, true);
        cart = new Cart();
        cart.setId(20L);
        cart.setUser(student);
        vendorUser = user(30L, "vendor@example.edu", Role.VENDOR, true);
        vendorProfile = vendorProfile(40L, vendorUser, ApprovalStatus.APPROVED, "Campus Books");
    }

    @Test
    void rejectsInactiveStudentDuringCheckout() {
        student.setActive(false);
        givenStudentAuthentication();

        assertThrows(AccessDeniedException.class,
                () -> orderService.placeOrder(checkoutRequest(), authentication));
        verify(cartRepository, never()).findByUserId(10L);
    }

    @Test
    void rejectsNonStudentDuringCheckout() {
        student.setRole(Role.VENDOR);
        givenStudentAuthentication();

        assertThrows(AccessDeniedException.class,
                () -> orderService.placeOrder(checkoutRequest(), authentication));
        verify(cartRepository, never()).findByUserId(10L);
    }

    @Test
    void rejectsEmptyOrMissingCart() {
        givenStudentAuthentication();
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(checkoutRequest(), authentication));
        verify(productRepository, never()).findAllByIdInSortedForUpdate(anyCollection());
        verify(checkoutOrderRepository, never()).save(any(CheckoutOrder.class));
        verify(cartItemRepository, never()).deleteAll(any());
    }

    @Test
    void successfulSingleVendorCheckoutCreatesSnapshotsTotalsAndInitialHistory() {
        Product product = product(100L, vendorProfile, "Notebook", "https://img.test/book.png", "4.25", 8, true);
        CartItem cartItem = cartItem(50L, product, 2);
        givenCheckout(List.of(cartItem), List.of(product));

        CheckoutOrderResponse response = orderService.placeOrder(checkoutRequest(), authentication);

        assertEquals(new BigDecimal("8.50"), response.getTotal());
        assertEquals(1, response.getVendorOrders().size());
        VendorOrderResponse vendorResponse = response.getVendorOrders().get(0);
        assertEquals(new BigDecimal("8.50"), vendorResponse.getTotal());
        assertEquals(OrderStatus.PENDING, vendorResponse.getStatus());
        assertEquals(1, vendorResponse.getItems().size());
        OrderItemResponse itemResponse = vendorResponse.getItems().get(0);
        assertEquals("Notebook", itemResponse.getProductName());
        assertEquals("https://img.test/book.png", itemResponse.getImageUrl());
        assertEquals(new BigDecimal("4.25"), itemResponse.getUnitPrice());
        assertEquals(2, itemResponse.getQuantity());
        assertEquals(new BigDecimal("8.50"), itemResponse.getSubtotal());
        assertEquals(1, vendorResponse.getStatusHistory().size());
        assertNull(vendorResponse.getStatusHistory().get(0).getPreviousStatus());
        assertEquals(OrderStatus.PENDING, vendorResponse.getStatusHistory().get(0).getNewStatus());
        assertEquals(6, product.getStock());
        verify(cartItemRepository).deleteAll(List.of(cartItem));

        ArgumentCaptor<CheckoutOrder> orderCaptor = ArgumentCaptor.forClass(CheckoutOrder.class);
        verify(checkoutOrderRepository).save(orderCaptor.capture());
        assertEquals(student, orderCaptor.getValue().getStudent());
        assertEquals(PaymentMethod.CASH, orderCaptor.getValue().getPaymentMethod());
        assertEquals(1, orderCaptor.getValue().getVendorOrders().size());
        assertEquals("Notebook", orderCaptor.getValue().getVendorOrders().get(0)
                .getItems().get(0).getProductNameSnapshot());
        assertEquals(new BigDecimal("4.25"), orderCaptor.getValue().getVendorOrders().get(0)
                .getItems().get(0).getUnitPriceSnapshot());
    }

    @Test
    void successfulCheckoutSplitsProductsAcrossVendorsAndUsesPersistedPrices() {
        VendorProfile secondVendor = vendorProfile(
                41L, user(31L, "vendor2@example.edu", Role.VENDOR, true),
                ApprovalStatus.APPROVED, "Campus Tech");
        Product firstProduct = product(100L, vendorProfile, "Notebook", null, "10.25", 5, true);
        Product secondProduct = product(101L, secondVendor, "USB Cable", "https://img.test/cable.png", "3.40", 4, true);
        CartItem firstItem = cartItem(50L, firstProduct, 2);
        CartItem secondItem = cartItem(51L, secondProduct, 3);
        givenCheckout(List.of(firstItem, secondItem), List.of(secondProduct, firstProduct));

        CheckoutOrderResponse response = orderService.placeOrder(checkoutRequest(), authentication);

        assertEquals(2, response.getVendorOrders().size());
        assertEquals(new BigDecimal("30.70"), response.getTotal());
        assertEquals(new BigDecimal("20.50"), response.getVendorOrders().get(0).getTotal());
        assertEquals(new BigDecimal("10.20"), response.getVendorOrders().get(1).getTotal());
        assertEquals("Campus Books", response.getVendorOrders().get(0).getVendorShopName());
        assertEquals("Campus Tech", response.getVendorOrders().get(1).getVendorShopName());
        assertEquals(new BigDecimal("10.25"), response.getVendorOrders().get(0)
                .getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("3.40"), response.getVendorOrders().get(1)
                .getItems().get(0).getUnitPrice());
        assertEquals(3, firstProduct.getStock());
        assertEquals(1, secondProduct.getStock());
    }

    @Test
    void rejectsInactiveProductWithoutChangingStockOrCart() {
        Product product = product(100L, vendorProfile, "Notebook", null, "4.25", 8, false);
        assertCheckoutRejectedWithoutSideEffects(product);
    }

    @Test
    void rejectsInsufficientStockWithoutChangingStockOrCart() {
        Product product = product(100L, vendorProfile, "Notebook", null, "4.25", 1, true);
        assertCheckoutRejectedWithoutSideEffects(product);
    }

    @Test
    void rejectsInactiveVendorWithoutChangingStockOrCart() {
        vendorUser.setActive(false);
        Product product = product(100L, vendorProfile, "Notebook", null, "4.25", 8, true);
        assertCheckoutRejectedWithoutSideEffects(product);
    }

    @Test
    void rejectsUnapprovedVendorWithoutChangingStockOrCart() {
        vendorProfile.setApprovalStatus(ApprovalStatus.PENDING);
        Product product = product(100L, vendorProfile, "Notebook", null, "4.25", 8, true);
        assertCheckoutRejectedWithoutSideEffects(product);
    }

    @Test
    void rejectsMissingProductReturnedByLockQueryWithoutSideEffects() {
        Product cartProduct = product(100L, vendorProfile, "Notebook", null, "4.25", 8, true);
        CartItem cartItem = cartItem(50L, cartProduct, 2);
        givenStudentAuthentication();
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L)).thenReturn(List.of(cartItem));
        when(productRepository.findAllByIdInSortedForUpdate(anyCollection())).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(checkoutRequest(), authentication));
        verify(checkoutOrderRepository, never()).save(any(CheckoutOrder.class));
        verify(productRepository, never()).saveAll(any());
        verify(cartItemRepository, never()).deleteAll(any());
    }

    @Test
    void studentHistoryAndDetailsUseStudentScopedQueries() {
        givenStudentAuthentication();
        CheckoutOrder checkoutOrder = checkoutOrder(70L);
        when(checkoutOrderRepository.findByStudentIdOrderByPlacedAtDesc(10L))
                .thenReturn(List.of(checkoutOrder));
        when(checkoutOrderRepository.findByIdAndStudentId(70L, 10L))
                .thenReturn(Optional.of(checkoutOrder));

        List<CheckoutOrderResponse> history = orderService.findStudentOrders(authentication);
        CheckoutOrderResponse detail = orderService.findStudentOrder(70L, authentication);

        assertEquals(1, history.size());
        assertEquals(70L, detail.getId());
        verify(checkoutOrderRepository).findByStudentIdOrderByPlacedAtDesc(10L);
        verify(checkoutOrderRepository).findByIdAndStudentId(70L, 10L);
    }

    @Test
    void studentCannotRetrieveAnotherStudentsCheckout() {
        givenStudentAuthentication();
        when(checkoutOrderRepository.findByIdAndStudentId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.findStudentOrder(99L, authentication));
        verify(checkoutOrderRepository).findByIdAndStudentId(99L, 10L);
    }

    @Test
    void vendorHistoryAndDetailsUseVendorScopedQueries() {
        givenVendorAuthentication(true, true);
        VendorOrder vendorOrder = vendorOrder(80L, vendorProfile, OrderStatus.PENDING);
        when(vendorOrderRepository.findByVendorProfileIdOrderByCreatedAtDesc(40L))
                .thenReturn(List.of(vendorOrder));
        when(vendorOrderRepository.findByIdAndVendorProfileId(80L, 40L))
                .thenReturn(Optional.of(vendorOrder));

        List<VendorOrderResponse> history = orderService.findVendorOrders(authentication);
        VendorOrderResponse detail = orderService.findVendorOrder(80L, authentication);

        assertEquals(1, history.size());
        assertEquals(80L, detail.getId());
        verify(vendorOrderRepository).findByVendorProfileIdOrderByCreatedAtDesc(40L);
        verify(vendorOrderRepository).findByIdAndVendorProfileId(80L, 40L);
    }

    @Test
    void vendorCannotRetrieveAnotherVendorsOrder() {
        givenVendorAuthentication(true, true);
        when(vendorOrderRepository.findByIdAndVendorProfileId(99L, 40L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.findVendorOrder(99L, authentication));
        verify(vendorOrderRepository).findByIdAndVendorProfileId(99L, 40L);
    }

    @Test
    void inactiveOrUnapprovedVendorsCannotManageOrders() {
        givenVendorAuthentication(false, true);
        assertThrows(AccessDeniedException.class, () -> orderService.findVendorOrders(authentication));

        vendorUser.setActive(true);
        givenVendorAuthentication(true, false);
        assertThrows(AccessDeniedException.class, () -> orderService.findVendorOrders(authentication));

        verify(vendorOrderRepository, never()).findByVendorProfileIdOrderByCreatedAtDesc(40L);
    }

    @Test
    void permitsEveryAllowedStatusTransitionAndRecordsHistory() {
        givenVendorAuthentication(true, true);
        VendorOrder vendorOrder = vendorOrder(80L, vendorProfile, OrderStatus.PENDING);
        when(vendorOrderRepository.findByIdAndVendorProfileIdForUpdate(80L, 40L))
                .thenReturn(Optional.of(vendorOrder));
        when(vendorOrderRepository.save(vendorOrder)).thenReturn(vendorOrder);
        when(orderItemRepository.findByVendorOrderIdOrderByIdAsc(80L)).thenReturn(List.of());

        List<StatusTransition> transitions = List.of(
                new StatusTransition(OrderStatus.PENDING, OrderStatus.ACCEPTED),
                new StatusTransition(OrderStatus.PENDING, OrderStatus.CANCELLED),
                new StatusTransition(OrderStatus.ACCEPTED, OrderStatus.PREPARING),
                new StatusTransition(OrderStatus.ACCEPTED, OrderStatus.CANCELLED),
                new StatusTransition(OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP),
                new StatusTransition(OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED));

        for (StatusTransition transition : transitions) {
            vendorOrder.setStatus(transition.from());
            VendorOrderStatusUpdateRequest request = statusRequest(transition.to(), "status note");

            VendorOrderResponse response = orderService.updateVendorOrderStatus(80L, request, authentication);

            assertEquals(transition.to(), response.getStatus());
            OrderStatusHistory latestHistory = vendorOrder.getStatusHistory()
                    .get(vendorOrder.getStatusHistory().size() - 1);
            assertEquals(transition.from(), latestHistory.getPreviousStatus());
            assertEquals(transition.to(), latestHistory.getNewStatus());
            assertEquals("status note", latestHistory.getNote());
            assertTrue(latestHistory.getChangedAt() != null);
        }
    }

    @Test
    void rejectsEveryInvalidStatusTransition() {
        givenVendorAuthentication(true, true);
        VendorOrder vendorOrder = vendorOrder(80L, vendorProfile, OrderStatus.PENDING);
        when(vendorOrderRepository.findByIdAndVendorProfileIdForUpdate(80L, 40L))
                .thenAnswer(invocation -> Optional.of(vendorOrder));

        int invalidTransitions = 0;
        for (OrderStatus current : OrderStatus.values()) {
            for (OrderStatus requested : OrderStatus.values()) {
                if (isAllowed(current, requested)) {
                    continue;
                }
                invalidTransitions++;
                vendorOrder.setStatus(current);
                assertThrows(IllegalArgumentException.class,
                        () -> orderService.updateVendorOrderStatus(
                                80L, statusRequest(requested, null), authentication),
                        current + " -> " + requested + " must be rejected");
                assertEquals(current, vendorOrder.getStatus());
            }
        }
        assertEquals(30, invalidTransitions);
        assertTrue(vendorOrder.getStatusHistory().isEmpty());
        verify(orderItemRepository, never()).findByVendorOrderIdOrderByIdAsc(80L);
    }

    @Test
    void cancellationFromPreparingOrLaterIsRejected() {
        givenVendorAuthentication(true, true);
        VendorOrder vendorOrder = vendorOrder(80L, vendorProfile, OrderStatus.PREPARING);
        when(vendorOrderRepository.findByIdAndVendorProfileIdForUpdate(80L, 40L))
                .thenAnswer(invocation -> Optional.of(vendorOrder));

        for (OrderStatus current : List.of(
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED)) {
            vendorOrder.setStatus(current);
            assertThrows(IllegalArgumentException.class,
                    () -> orderService.updateVendorOrderStatus(
                            80L, statusRequest(OrderStatus.CANCELLED, null), authentication));
        }
        verify(orderItemRepository, never()).findByVendorOrderIdOrderByIdAsc(80L);
    }

    @Test
    void cancellationRestoresStockOnlyOnceAndAppendsHistory() {
        givenVendorAuthentication(true, true);
        VendorOrder vendorOrder = vendorOrder(80L, vendorProfile, OrderStatus.PENDING);
        Product product = product(100L, vendorProfile, "Notebook", null, "4.25", 3, true);
        OrderItem item = orderItem(vendorOrder, product, "Notebook", "4.25", 2);
        when(vendorOrderRepository.findByIdAndVendorProfileIdForUpdate(80L, 40L))
                .thenReturn(Optional.of(vendorOrder));
        when(vendorOrderRepository.save(vendorOrder)).thenReturn(vendorOrder);
        when(orderItemRepository.findByVendorOrderIdOrderByIdAsc(80L)).thenReturn(List.of(item));
        when(productRepository.findAllByIdInSortedForUpdate(anyCollection())).thenReturn(List.of(product));

        VendorOrderResponse cancelled = orderService.updateVendorOrderStatus(
                80L, statusRequest(OrderStatus.CANCELLED, "vendor cancelled"), authentication);

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(5, product.getStock());
        assertEquals(1, vendorOrder.getStatusHistory().size());
        assertEquals(OrderStatus.PENDING, vendorOrder.getStatusHistory().get(0).getPreviousStatus());
        assertEquals(OrderStatus.CANCELLED, vendorOrder.getStatusHistory().get(0).getNewStatus());
        assertEquals("vendor cancelled", vendorOrder.getStatusHistory().get(0).getNote());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateVendorOrderStatus(
                        80L, statusRequest(OrderStatus.CANCELLED, null), authentication));
        assertEquals(5, product.getStock());
        verify(orderItemRepository).findByVendorOrderIdOrderByIdAsc(80L);
        verify(productRepository).saveAll(List.of(product));
    }

    @Test
    void cancellationPreservesSnapshotsAndSkipsDeletedProducts() {
        givenVendorAuthentication(true, true);
        VendorOrder vendorOrder = vendorOrder(80L, vendorProfile, OrderStatus.ACCEPTED);
        OrderItem deletedProductItem = orderItem(vendorOrder, null, "Old notebook", "9.99", 3);
        when(vendorOrderRepository.findByIdAndVendorProfileIdForUpdate(80L, 40L))
                .thenReturn(Optional.of(vendorOrder));
        when(vendorOrderRepository.save(vendorOrder)).thenReturn(vendorOrder);
        when(orderItemRepository.findByVendorOrderIdOrderByIdAsc(80L))
                .thenReturn(List.of(deletedProductItem));

        VendorOrderResponse response = orderService.updateVendorOrderStatus(
                80L, statusRequest(OrderStatus.CANCELLED, null), authentication);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals("Old notebook", response.getItems().get(0).getProductName());
        assertNull(response.getItems().get(0).getProductId());
        assertEquals(new BigDecimal("9.99"), response.getItems().get(0).getUnitPrice());
        verify(productRepository, never()).findAllByIdInSortedForUpdate(anyCollection());
        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void cancellationSkipsAProductMissingFromTheLockQuery() {
        givenVendorAuthentication(true, true);
        VendorOrder vendorOrder = vendorOrder(80L, vendorProfile, OrderStatus.PENDING);
        Product previouslyExistingProduct = product(100L, vendorProfile, "Notebook", null, "4.25", 3, true);
        OrderItem item = orderItem(vendorOrder, previouslyExistingProduct, "Notebook", "4.25", 2);
        when(vendorOrderRepository.findByIdAndVendorProfileIdForUpdate(80L, 40L))
                .thenReturn(Optional.of(vendorOrder));
        when(vendorOrderRepository.save(vendorOrder)).thenReturn(vendorOrder);
        when(orderItemRepository.findByVendorOrderIdOrderByIdAsc(80L)).thenReturn(List.of(item));
        when(productRepository.findAllByIdInSortedForUpdate(anyCollection())).thenReturn(List.of());

        assertEquals(OrderStatus.CANCELLED, orderService.updateVendorOrderStatus(
                80L, statusRequest(OrderStatus.CANCELLED, null), authentication).getStatus());
        assertEquals(3, previouslyExistingProduct.getStock());
    }

    private void assertCheckoutRejectedWithoutSideEffects(Product product) {
        CartItem item = cartItem(50L, product, 2);
        int initialStock = product.getStock();
        givenCheckout(List.of(item), List.of(product));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(checkoutRequest(), authentication));

        assertEquals(initialStock, product.getStock());
        verify(checkoutOrderRepository, never()).save(any(CheckoutOrder.class));
        verify(productRepository, never()).saveAll(any());
        verify(cartItemRepository, never()).deleteAll(any());
    }

    private void givenCheckout(List<CartItem> cartItems, List<Product> lockedProducts) {
        givenStudentAuthentication();
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L)).thenReturn(cartItems);
        when(productRepository.findAllByIdInSortedForUpdate(anyCollection())).thenReturn(lockedProducts);
        lenient().when(checkoutOrderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        lenient().when(checkoutOrderRepository.save(any(CheckoutOrder.class))).thenAnswer(invocation -> {
            CheckoutOrder order = invocation.getArgument(0);
            order.setId(70L);
            return order;
        });
    }

    private void givenStudentAuthentication() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
    }

    private void givenVendorAuthentication(boolean active, boolean approved) {
        vendorUser.setActive(active);
        vendorProfile.setApprovalStatus(approved ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(vendorUser.getEmail());
        when(userRepository.findByEmail(vendorUser.getEmail())).thenReturn(Optional.of(vendorUser));
        if (active) {
            when(vendorProfileRepository.findByUserId(vendorUser.getId()))
                    .thenReturn(Optional.of(vendorProfile));
            if (approved) {
                when(vendorApprovalChecker.isApproved(authentication)).thenReturn(true);
            }
        }
    }

    private CheckoutRequest checkoutRequest() {
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setPickupNote("Student Centre counter");
        return request;
    }

    private VendorOrderStatusUpdateRequest statusRequest(OrderStatus status, String note) {
        VendorOrderStatusUpdateRequest request = new VendorOrderStatusUpdateRequest();
        request.setStatus(status);
        request.setNote(note);
        return request;
    }

    private CheckoutOrder checkoutOrder(Long id) {
        CheckoutOrder order = new CheckoutOrder();
        order.setId(id);
        order.setStudent(student);
        order.setOrderNumber("CC-TEST-" + id);
        order.setTotal(BigDecimal.ZERO);
        order.setPaymentMethod(PaymentMethod.CASH);
        order.setVendorOrders(new ArrayList<>());
        return order;
    }

    private VendorOrder vendorOrder(Long id, VendorProfile profile, OrderStatus status) {
        VendorOrder order = new VendorOrder();
        order.setId(id);
        order.setVendorProfile(profile);
        order.setCheckoutOrder(checkoutOrder(70L));
        order.setTotal(BigDecimal.ZERO);
        order.setStatus(status);
        order.setItems(new ArrayList<>());
        order.setStatusHistory(new ArrayList<>());
        return order;
    }

    private OrderItem orderItem(
            VendorOrder vendorOrder,
            Product product,
            String name,
            String unitPrice,
            int quantity) {
        OrderItem item = new OrderItem();
        item.setId(90L);
        item.setVendorOrder(vendorOrder);
        item.setProduct(product);
        item.setProductNameSnapshot(name);
        item.setProductImageUrlSnapshot("https://img.test/snapshot.png");
        item.setUnitPriceSnapshot(new BigDecimal(unitPrice));
        item.setQuantity(quantity);
        vendorOrder.getItems().add(item);
        return item;
    }

    private Product product(
            Long id,
            VendorProfile profile,
            String name,
            String imageUrl,
            String price,
            int stock,
            boolean active) {
        Product product = new Product();
        product.setId(id);
        product.setVendorProfile(profile);
        product.setName(name);
        product.setImageUrl(imageUrl);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setActive(active);
        return product;
    }

    private CartItem cartItem(Long id, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private User user(Long id, String email, Role role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);
        user.setFullName(email);
        return user;
    }

    private VendorProfile vendorProfile(
            Long id, User user, ApprovalStatus approvalStatus, String shopName) {
        VendorProfile profile = new VendorProfile();
        ReflectionTestUtils.setField(profile, "id", id);
        profile.setUser(user);
        profile.setApprovalStatus(approvalStatus);
        profile.setShopName(shopName);
        profile.setShopAddress("Campus");
        return profile;
    }

    private boolean isAllowed(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING -> to == OrderStatus.ACCEPTED || to == OrderStatus.CANCELLED;
            case ACCEPTED -> to == OrderStatus.PREPARING || to == OrderStatus.CANCELLED;
            case PREPARING -> to == OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> to == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private record StatusTransition(OrderStatus from, OrderStatus to) {
    }
}
