package com.collegecart.controller;

import com.collegecart.dto.CartAddItemRequest;
import com.collegecart.dto.CartQuantityUpdateRequest;
import com.collegecart.dto.CartResponse;
import com.collegecart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('STUDENT')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody CartAddItemRequest request,
            Authentication authentication) {
        CartResponse cart = cartService.addItem(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody CartQuantityUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                cartService.updateItemQuantity(itemId, request, authentication));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        cartService.removeItem(itemId, authentication);
        return ResponseEntity.noContent().build();
    }
}
