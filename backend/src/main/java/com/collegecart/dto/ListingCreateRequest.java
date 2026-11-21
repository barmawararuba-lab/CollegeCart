package com.collegecart.dto;

import com.collegecart.enums.ListingCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.validator.constraints.UniqueElements;
import org.hibernate.validator.constraints.URL;

public class ListingCreateRequest {

    @NotBlank(message = "Listing title is required")
    @Size(min = 3, max = 100, message = "Listing title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Listing description is required")
    @Size(max = 2000, message = "Listing description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Asking price is required")
    @DecimalMin(value = "0.01", message = "Asking price must be greater than zero")
    @Digits(integer = 6, fraction = 2, message = "Asking price supports up to two decimal places")
    private BigDecimal askingPrice;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Listing condition is required")
    private ListingCondition condition;

    @Size(max = 3, message = "A listing may have at most three images")
    @UniqueElements(message = "Image URLs must not contain duplicates")
    private List<
            @NotBlank(message = "Image URL cannot be blank")
            @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
            @URL(regexp = "(?i)^https?://.*$", message = "Image URL must be a valid HTTP or HTTPS URL")
            String> imageUrls;

    public ListingCreateRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAskingPrice() {
        return askingPrice;
    }

    public void setAskingPrice(BigDecimal askingPrice) {
        this.askingPrice = askingPrice;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public void setCondition(ListingCondition condition) {
        this.condition = condition;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
