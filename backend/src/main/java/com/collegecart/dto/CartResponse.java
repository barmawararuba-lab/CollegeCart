package com.collegecart.dto;

import com.collegecart.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private List<CartItemResponse> items;
    private BigDecimal total;

    public CartResponse(List<CartItem> cartItems) {
        this.items = cartItems.stream()
                .map(CartItemResponse::new)
                .toList();
        this.total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
