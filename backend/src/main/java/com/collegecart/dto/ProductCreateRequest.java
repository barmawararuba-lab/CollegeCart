package com.collegecart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.hibernate.validator.constraints.URL;

public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 80, message = "Product name must be between 3 and 80 characters")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(max = 300, message = "Product description must not exceed 300 characters")
    private String description;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be greater than zero")
    @Digits(integer = 6, fraction = 2, message = "Product price supports up to two decimal places")
    private BigDecimal price;

    @NotNull(message = "Product stock is required")
    @Min(value = 0, message = "Product stock cannot be negative")
    @Max(value = 10000, message = "Product stock cannot exceed 10000 units")
    private Integer stock;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    @URL(regexp = "(?i)^https?://.*$", message = "Image URL must be a valid HTTP or HTTPS URL")
    private String imageUrl;

    public ProductCreateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
