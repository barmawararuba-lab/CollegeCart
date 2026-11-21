package com.collegecart.dto;

import com.collegecart.entity.OrderItem;
import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public OrderItemResponse(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.productId = orderItem.getProduct() == null
                ? null
                : orderItem.getProduct().getId();
        this.productName = orderItem.getProductNameSnapshot();
        this.imageUrl = orderItem.getProductImageUrlSnapshot();
        this.unitPrice = orderItem.getUnitPriceSnapshot();
        this.quantity = orderItem.getQuantity();
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageUrl() {
        return imageUrl;
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
}
