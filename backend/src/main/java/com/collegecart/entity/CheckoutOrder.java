package com.collegecart.entity;

import com.collegecart.enums.PaymentMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "checkout_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_checkout_orders_order_number",
                columnNames = "order_number"
        ),
        indexes = @Index(name = "idx_checkout_orders_student_placed", columnList = "student_id, placed_at")
)
public class CheckoutOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A student is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull(message = "An order number is required")
    @Size(max = 40, message = "Order number must not exceed 40 characters")
    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(name = "placed_at", nullable = false, updatable = false)
    private LocalDateTime placedAt;

    @NotNull(message = "Order total is required")
    @DecimalMin(value = "0.00", message = "Order total cannot be negative")
    @Digits(integer = 12, fraction = 2, message = "Order total supports up to two decimal places")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Size(max = 500, message = "Pickup note must not exceed 500 characters")
    @Column(name = "pickup_note", length = 500)
    private String pickupNote;

    @NotNull(message = "A payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "checkoutOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorOrder> vendorOrders = new ArrayList<>();

    public CheckoutOrder() {
    }

    @PrePersist
    protected void onCreate() {
        if (placedAt == null) {
            placedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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

    public List<VendorOrder> getVendorOrders() {
        return vendorOrders;
    }

    public void setVendorOrders(List<VendorOrder> vendorOrders) {
        this.vendorOrders = vendorOrders;
    }
}
