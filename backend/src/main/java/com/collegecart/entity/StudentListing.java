package com.collegecart.entity;

import com.collegecart.enums.ListingCondition;
import com.collegecart.enums.ListingStatus;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "student_listings",
        indexes = {
            @Index(name = "idx_student_listings_seller_status", columnList = "seller_id, status"),
            @Index(name = "idx_student_listings_status_created", columnList = "status, created_at"),
            @Index(name = "idx_student_listings_category_condition", columnList = "category_id, condition")
        }
)
public class StudentListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A seller is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @NotBlank(message = "Listing title is required")
    @Size(min = 3, max = 100, message = "Listing title must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank(message = "Listing description is required")
    @Size(max = 2000, message = "Listing description must not exceed 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;

    @NotNull(message = "Asking price is required")
    @DecimalMin(value = "0.01", message = "Asking price must be greater than zero")
    @Digits(integer = 6, fraction = 2, message = "Asking price supports up to two decimal places")
    @Column(name = "asking_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal askingPrice;

    @NotNull(message = "A category is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull(message = "Listing condition is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingCondition condition;

    @NotNull(message = "Listing status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status = ListingStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    @Valid
    @Size(max = 3, message = "A listing may have at most three images")
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<StudentListingImage> images = new ArrayList<>();

    public StudentListing() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public void setCondition(ListingCondition condition) {
        this.condition = condition;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }

    public List<StudentListingImage> getImages() {
        return images;
    }

    public void setImages(List<StudentListingImage> images) {
        this.images = images;
    }
}
