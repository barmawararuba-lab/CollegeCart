package com.collegecart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(
        name = "student_listing_images",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_listing_images_listing_order",
                columnNames = {"listing_id", "display_order"}
        ),
        indexes = @Index(name = "idx_student_listing_images_listing", columnList = "listing_id")
)
public class StudentListingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A listing is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private StudentListing listing;

    @NotBlank(message = "Image URL is required")
    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    @URL(regexp = "(?i)^https?://.*$", message = "Image URL must be a valid HTTP or HTTPS URL")
    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @NotNull(message = "Image display order is required")
    @Min(value = 0, message = "Image display order cannot be negative")
    @Max(value = 2, message = "Image display order cannot exceed two")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public StudentListingImage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentListing getListing() {
        return listing;
    }

    public void setListing(StudentListing listing) {
        this.listing = listing;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
