package com.collegecart.dto;

import com.collegecart.entity.OrderStatusHistory;
import com.collegecart.enums.OrderStatus;
import java.time.LocalDateTime;

public class OrderStatusHistoryResponse {

    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private LocalDateTime changedAt;
    private String note;

    public OrderStatusHistoryResponse(OrderStatusHistory history) {
        this.previousStatus = history.getPreviousStatus();
        this.newStatus = history.getNewStatus();
        this.changedAt = history.getChangedAt();
        this.note = history.getNote();
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }
}
