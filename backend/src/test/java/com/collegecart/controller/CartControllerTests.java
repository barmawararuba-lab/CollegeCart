package com.collegecart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collegecart.config.SecurityConfig;
import com.collegecart.dto.CartAddItemRequest;
import com.collegecart.dto.CartItemResponse;
import com.collegecart.dto.CartQuantityUpdateRequest;
import com.collegecart.dto.CartResponse;
import com.collegecart.entity.CartItem;
import com.collegecart.entity.Product;
import com.collegecart.repository.UserRepository;
import com.collegecart.service.CartService;
import com.collegecart.service.JwtService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        cartResponse = cartResponse();
    }

    @Test
    void getCartReturnsAuthenticatedStudentsCart() throws Exception {
        when(cartService.getCart(any(Authentication.class))).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart").with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(100))
                .andExpect(jsonPath("$.items[0].subtotal").value(8.5))
                .andExpect(jsonPath("$.total").value(8.5));
    }

    @Test
    void postAddsItemAndReturnsCreated() throws Exception {
        when(cartService.addItem(any(CartAddItemRequest.class), any(Authentication.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(post("/api/cart/items")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":100,\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void patchUpdatesItemAndReturnsOk() throws Exception {
        when(cartService.updateItemQuantity(
                eq(30L), any(CartQuantityUpdateRequest.class), any(Authentication.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(patch("/api/cart/items/30")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(8.5));
    }

    @Test
    void deleteRemovesItemAndReturnsNoContent() throws Exception {
        doNothing().when(cartService).removeItem(eq(30L), any(Authentication.class));

        mockMvc.perform(delete("/api/cart/items/30")
                        .with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidPostAndPatchBodiesReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":0,\"quantity\":0}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/cart/items/30")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedUserCannotAccessCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonStudentUserCannotAccessCart() throws Exception {
        mockMvc.perform(get("/api/cart").with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isForbidden());
    }

    private CartResponse cartResponse() {
        Product product = new Product();
        product.setId(100L);
        product.setName("Notebook");
        product.setPrice(new BigDecimal("4.25"));
        product.setStock(5);
        product.setActive(true);

        CartItem item = new CartItem();
        item.setId(30L);
        item.setProduct(product);
        item.setQuantity(2);

        return new CartResponse(List.of(item));
    }
}
