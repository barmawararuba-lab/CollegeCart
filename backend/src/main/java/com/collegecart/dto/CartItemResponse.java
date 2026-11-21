package com.collegecart.dto;

import com.collegecart.entity.CartItem;
import com.collegecart.entity.Product;
import java.math.BigDecimal;

public class CartItemResponse {

    private Long cartItemId;
    private Long productId;
    private String name;
    private String image;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private boolean available;

    public CartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        this.cartItemId = cartItem.getId();
        this.productId = product.getId();
        this.name = product.getName();
        this.image = product.getImageUrl();
        this.unitPrice = product.getPrice();
        this.quantity = cartItem.getQuantity();
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.available = product.isActive() && product.getStock() >= quantity;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public boolean isAvailable() {
        return available;
    }
}
