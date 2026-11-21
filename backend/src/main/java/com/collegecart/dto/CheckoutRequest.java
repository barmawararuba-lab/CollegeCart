package com.collegecart.dto;

import com.collegecart.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CheckoutRequest {

    @Size(max = 500, message = "Pickup note must not exceed 500 characters")
    private String pickupNote;

    @NotNull(message = "A payment method is required")
    private PaymentMethod paymentMethod;

    public CheckoutRequest() {
    }

    public String getPickupNote() {
        return pickupNote;
    }

    public void setPickupNote(String pickupNote) {
        this.pickupNote = pickupNote;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
