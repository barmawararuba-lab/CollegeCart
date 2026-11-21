package com.collegecart.dto;

import com.collegecart.entity.CheckoutOrder;
import com.collegecart.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CheckoutOrderResponse {

    private Long id;
    private String orderNumber;
    private LocalDateTime placedAt;
    private BigDecimal total;
    private String pickupNote;
    private PaymentMethod paymentMethod;
    private List<VendorOrderResponse> vendorOrders;

    public CheckoutOrderResponse(CheckoutOrder checkoutOrder) {
        this.id = checkoutOrder.getId();
        this.orderNumber = checkoutOrder.getOrderNumber();
        this.placedAt = checkoutOrder.getPlacedAt();
        this.total = checkoutOrder.getTotal();
        this.pickupNote = checkoutOrder.getPickupNote();
        this.paymentMethod = checkoutOrder.getPaymentMethod();
        this.vendorOrders = checkoutOrder.getVendorOrders().stream()
                .map(VendorOrderResponse::new)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getPickupNote() {
        return pickupNote;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public List<VendorOrderResponse> getVendorOrders() {
        return vendorOrders;
    }
}
