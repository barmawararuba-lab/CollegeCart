package com.collegecart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A vendor order is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_order_id", nullable = false)
    private VendorOrder vendorOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Product product;

    @NotBlank(message = "Product name snapshot is required")
    @Size(max = 80, message = "Product name snapshot must not exceed 80 characters")
    @Column(name = "product_name_snapshot", nullable = false, length = 80)
    private String productNameSnapshot;

    @Size(max = 2048, message = "Product image URL snapshot must not exceed 2048 characters")
    @Column(name = "product_image_url_snapshot", length = 2048)
    private String productImageUrlSnapshot;

    @NotNull(message = "Unit price snapshot is required")
    @Digits(integer = 6, fraction = 2, message = "Unit price snapshot supports up to two decimal places")
    @Column(name = "unit_price_snapshot", nullable = false, precision = 8, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least one")
    @Column(nullable = false)
    private Integer quantity;

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VendorOrder getVendorOrder() {
        return vendorOrder;
    }

    public void setVendorOrder(VendorOrder vendorOrder) {
        this.vendorOrder = vendorOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }

    public String getProductImageUrlSnapshot() {
        return productImageUrlSnapshot;
    }

    public void setProductImageUrlSnapshot(String productImageUrlSnapshot) {
        this.productImageUrlSnapshot = productImageUrlSnapshot;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
