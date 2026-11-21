package com.collegecart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collegecart.dto.CartAddItemRequest;
import com.collegecart.dto.CartItemResponse;
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

@ExtendWith(MockitoExtension.class)
class CartServiceTests {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CartService cartService;

    private User student;
    private Cart cart;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(10L);
        student.setEmail("student@example.edu");
        student.setRole(Role.STUDENT);
        student.setActive(true);

        cart = new Cart();
        cart.setId(20L);
        cart.setUser(student);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("student@example.edu");
        when(userRepository.findByEmail("student@example.edu"))
                .thenReturn(Optional.of(student));
    }

    @Test
    void returnsExistingStudentsCart() {
        givenCart();
        CartItem item = cartItem(30L, 100L, "Notebook", "4.25", 2, true, 5);
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L)).thenReturn(List.of(item));

        CartResponse response = cartService.getCart(authentication);

        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("8.50"), response.getTotal());
        verify(cartRepository).findByUserId(10L);
    }

    @Test
    void returnsEmptyCartWhenStudentHasNoCart() {
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart(authentication);

        assertTrue(response.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getTotal());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void createsCartAndAddsNewProduct() {
        Product product = product(100L, "Notebook", "4.25", 5, true);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            savedCart.setId(20L);
            return savedCart;
        });
        when(cartItemRepository.findByCartIdAndProductId(20L, 100L))
                .thenReturn(Optional.empty());
        List<CartItem> savedItems = stubItemSaveAndCartListing();

        CartResponse response = cartService.addItem(addRequest(100L, 2), authentication);

        assertEquals(1, savedItems.size());
        assertEquals(2, savedItems.get(0).getQuantity());
        assertEquals(student, savedItems.get(0).getCart().getUser());
        assertEquals(product, savedItems.get(0).getProduct());
        assertEquals(new BigDecimal("8.50"), response.getTotal());
        assertEquals(5, product.getStock());
    }

    @Test
    void addingExistingProductIncreasesItsQuantity() {
        Product product = product(100L, "Notebook", "4.25", 8, true);
        CartItem existingItem = cartItem(30L, 100L, "Notebook", "4.25", 2, true, 8);
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        givenCart();
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(20L, 100L))
                .thenReturn(Optional.of(existingItem));
        List<CartItem> savedItems = stubItemSaveAndCartListing();

        CartResponse response = cartService.addItem(addRequest(100L, 3), authentication);

        assertEquals(5, savedItems.get(0).getQuantity());
        assertEquals(new BigDecimal("21.25"), response.getTotal());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void rejectsInactiveProductWhenAdding() {
        Product inactiveProduct = product(100L, "Notebook", "4.25", 5, false);
        when(productRepository.findById(100L)).thenReturn(Optional.of(inactiveProduct));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(addRequest(100L, 1), authentication));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void rejectsQuantityGreaterThanAvailableStockWhenAdding() {
        Product product = product(100L, "Notebook", "4.25", 2, true);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addItem(addRequest(100L, 3), authentication));

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updatesOwnedCartItemQuantity() {
        Product product = product(100L, "Notebook", "4.25", 6, true);
        CartItem item = cartItem(30L, 100L, "Notebook", "4.25", 2, true, 6);
        item.setCart(cart);
        item.setProduct(product);
        givenCart();
        when(cartItemRepository.findByIdAndCartId(30L, 20L)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L)).thenReturn(List.of(item));

        CartResponse response = cartService.updateItemQuantity(
                30L, updateRequest(4), authentication);

        assertEquals(4, item.getQuantity());
        assertEquals(new BigDecimal("17.00"), response.getTotal());
        verify(cartItemRepository).save(item);
    }

    @Test
    void removesOwnedCartItem() {
        CartItem item = cartItem(30L, 100L, "Notebook", "4.25", 2, true, 5);
        givenCart();
        when(cartItemRepository.findByIdAndCartId(30L, 20L)).thenReturn(Optional.of(item));

        cartService.removeItem(30L, authentication);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void cannotAccessCartItemBelongingToAnotherStudent() {
        givenCart();
        when(cartItemRepository.findByIdAndCartId(99L, 20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeItem(99L, authentication));

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void retainsInactiveProductAndMarksCartLineUnavailable() {
        CartItem item = cartItem(30L, 100L, "Notebook", "4.25", 2, false, 5);
        givenCart();
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L)).thenReturn(List.of(item));

        CartResponse response = cartService.getCart(authentication);

        assertEquals(1, response.getItems().size());
        assertFalse(response.getItems().get(0).isAvailable());
        assertEquals(new BigDecimal("8.50"), response.getItems().get(0).getSubtotal());
    }

    @Test
    void retainsInsufficientStockProductAndMarksCartLineUnavailable() {
        CartItem item = cartItem(30L, 100L, "Notebook", "4.25", 4, true, 2);
        givenCart();
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L)).thenReturn(List.of(item));

        CartResponse response = cartService.getCart(authentication);

        assertEquals(1, response.getItems().size());
        assertFalse(response.getItems().get(0).isAvailable());
        assertEquals(new BigDecimal("17.00"), response.getTotal());
    }

    @Test
    void calculatesSubtotalsAndTotalUsingBigDecimal() {
        CartItem firstItem = cartItem(30L, 100L, "Notebook", "10.25", 2, true, 4);
        CartItem secondItem = cartItem(31L, 101L, "Pen", "0.10", 3, true, 3);
        givenCart();
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L))
                .thenReturn(List.of(firstItem, secondItem));

        CartResponse response = cartService.getCart(authentication);

        assertEquals(new BigDecimal("20.50"), response.getItems().get(0).getSubtotal());
        assertEquals(new BigDecimal("0.30"), response.getItems().get(1).getSubtotal());
        assertEquals(new BigDecimal("20.80"), response.getTotal());
    }

    @Test
    void rejectsInactiveUser() {
        student.setActive(false);

        assertThrows(AccessDeniedException.class, () -> cartService.getCart(authentication));
    }

    @Test
    void rejectsNonStudentUser() {
        student.setRole(Role.VENDOR);

        assertThrows(AccessDeniedException.class, () -> cartService.getCart(authentication));
    }

    private void givenCart() {
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(cart));
    }

    private List<CartItem> stubItemSaveAndCartListing() {
        List<CartItem> savedItems = new ArrayList<>();
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem savedItem = invocation.getArgument(0);
            savedItem.setId(30L);
            savedItems.clear();
            savedItems.add(savedItem);
            return savedItem;
        });
        when(cartItemRepository.findByCartIdOrderByIdAsc(20L))
                .thenAnswer(invocation -> List.copyOf(savedItems));
        return savedItems;
    }

    private CartAddItemRequest addRequest(Long productId, int quantity) {
        CartAddItemRequest request = new CartAddItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private CartQuantityUpdateRequest updateRequest(int quantity) {
        CartQuantityUpdateRequest request = new CartQuantityUpdateRequest();
        request.setQuantity(quantity);
        return request;
    }

    private Product product(
            Long id, String name, String price, int stock, boolean active) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setActive(active);
        return product;
    }

    private CartItem cartItem(
            Long itemId,
            Long productId,
            String name,
            String price,
            int quantity,
            boolean active,
            int stock) {
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setQuantity(quantity);
        item.setProduct(product(productId, name, price, stock, active));
        return item;
    }
}
