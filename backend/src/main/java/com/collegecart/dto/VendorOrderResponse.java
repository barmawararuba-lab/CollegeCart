package com.collegecart.dto;

import com.collegecart.entity.VendorOrder;
import com.collegecart.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VendorOrderResponse {

    private Long id;
    private String vendorShopName;
    private String vendorShopAddress;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;

    public VendorOrderResponse(VendorOrder vendorOrder) {
        this.id = vendorOrder.getId();
        this.vendorShopName = vendorOrder.getVendorProfile().getShopName();
        this.vendorShopAddress = vendorOrder.getVendorProfile().getShopAddress();
        this.total = vendorOrder.getTotal();
        this.status = vendorOrder.getStatus();
        this.createdAt = vendorOrder.getCreatedAt();
        this.updatedAt = vendorOrder.getUpdatedAt();
        this.items = vendorOrder.getItems().stream()
                .map(OrderItemResponse::new)
                .toList();
        this.statusHistory = vendorOrder.getStatusHistory().stream()
                .map(OrderStatusHistoryResponse::new)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getVendorShopName() {
        return vendorShopName;
    }

    public String getVendorShopAddress() {
        return vendorShopAddress;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public List<OrderStatusHistoryResponse> getStatusHistory() {
        return statusHistory;
    }
}
