package com.collegecart.service;

import com.collegecart.dto.CartAddItemRequest;
import com.collegecart.dto.CartQuantityUpdateRequest;
import com.collegecart.dto.CartResponse;
import com.collegecart.entity.Cart;
import com.collegecart.entity.CartItem;
import com.collegecart.entity.Product;
import com.collegecart.entity.User;
import com.collegecart.enums.Role;
import com.collegecart.exception.ResourceNotFoundException;
import com.collegecart.repository.CartItemRepository;
import com.collegecart.repository.CartRepository;
import com.collegecart.repository.ProductRepository;
import com.collegecart.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Authentication authentication) {
        User student = resolveActiveStudent(authentication);
        return cartRepository.findByUserId(student.getId())
                .map(this::toCartResponse)
                .orElseGet(() -> new CartResponse(List.of()));
    }

    @Transactional
    public CartResponse addItem(
            CartAddItemRequest request,
            Authentication authentication) {
        User student = resolveActiveStudent(authentication);
        int quantityToAdd = requireValidQuantity(request.getQuantity());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        validateProductAvailability(product, quantityToAdd);

        Cart cart = getOrCreateCart(student);
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int updatedQuantity;
        try {
            updatedQuantity = Math.addExact(cartItem.getQuantity(), quantityToAdd);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Requested quantity is too large");
        }
        validateProductAvailability(product, updatedQuantity);

        cartItem.setQuantity(updatedQuantity);
        cartItemRepository.save(cartItem);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(
            Long cartItemId,
            CartQuantityUpdateRequest request,
            Authentication authentication) {
        User student = resolveActiveStudent(authentication);
        int quantity = requireValidQuantity(request.getQuantity());
        Cart cart = findCart(student);
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateProductAvailability(cartItem.getProduct(), quantity);
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return toCartResponse(cart);
    }

    @Transactional
    public void removeItem(Long cartItemId, Authentication authentication) {
        User student = resolveActiveStudent(authentication);
        Cart cart = findCart(student);
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    private User resolveActiveStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        String email = authentication.getName().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new AccessDeniedException("Inactive accounts cannot access a cart");
        }
        if (user.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only students can access a cart");
        }
        return user;
    }

    private Cart getOrCreateCart(User student) {
        return cartRepository.findByUserId(student.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(student);
                    return cartRepository.save(cart);
                });
    }

    private Cart findCart(User student) {
        return cartRepository.findByUserId(student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private CartResponse toCartResponse(Cart cart) {
        return new CartResponse(
                cartItemRepository.findByCartIdOrderByIdAsc(cart.getId()));
    }

    private int requireValidQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least one");
        }
        return quantity;
    }

    private void validateProductAvailability(Product product, int quantity) {
        if (!product.isActive()) {
            throw new IllegalArgumentException("Inactive products cannot be added to or updated in a cart");
        }
        if (product.getStock() == null || quantity > product.getStock()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
    }
}
