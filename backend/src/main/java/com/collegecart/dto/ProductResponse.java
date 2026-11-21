package com.collegecart.dto;

import com.collegecart.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private Long categoryId;
    private BigDecimal price;
    private Integer stock;
    private String image;
    private boolean active;
    private LocalDateTime updatedAt;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.category = product.getCategory().getName();
        this.categoryId = product.getCategory().getId();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.image = product.getImageUrl();
        this.active = product.isActive();
        this.updatedAt = product.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getImage() {
        return image;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
