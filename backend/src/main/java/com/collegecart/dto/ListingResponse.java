package com.collegecart.dto;

import com.collegecart.entity.StudentListing;
import com.collegecart.enums.ListingCondition;
import com.collegecart.enums.ListingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ListingResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal askingPrice;
    private Long categoryId;
    private String categoryName;
    private ListingCondition condition;
    private ListingStatus status;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String sellerDisplayName;

    public ListingResponse(StudentListing listing) {
        this.id = listing.getId();
        this.title = listing.getTitle();
        this.description = listing.getDescription();
        this.askingPrice = listing.getAskingPrice();
        this.categoryId = listing.getCategory().getId();
        this.categoryName = listing.getCategory().getName();
        this.condition = listing.getCondition();
        this.status = listing.getStatus();
        this.imageUrls = listing.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();
        this.createdAt = listing.getCreatedAt();
        this.updatedAt = listing.getUpdatedAt();
        this.sellerDisplayName = listing.getSeller().getFullName();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAskingPrice() {
        return askingPrice;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getSellerDisplayName() {
        return sellerDisplayName;
    }
}
