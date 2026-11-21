package com.collegecart.dto;

import com.collegecart.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VendorOrderStatusUpdateRequest {

    @NotNull(message = "A new order status is required")
    private OrderStatus status;

    @Size(max = 500, message = "Status note must not exceed 500 characters")
    private String note;

    public VendorOrderStatusUpdateRequest() {
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
