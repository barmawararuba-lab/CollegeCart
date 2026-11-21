package com.collegecart.service;

import com.collegecart.dto.ListingBrowseResponse;
import com.collegecart.dto.ListingCreateRequest;
import com.collegecart.dto.ListingResponse;
import com.collegecart.dto.ListingStatusUpdateRequest;
import com.collegecart.dto.ListingUpdateRequest;
import com.collegecart.entity.Category;
import com.collegecart.entity.StudentListing;
import com.collegecart.entity.StudentListingImage;
import com.collegecart.entity.User;
import com.collegecart.enums.ListingCondition;
import com.collegecart.enums.ListingStatus;
import com.collegecart.enums.Role;
import com.collegecart.exception.ResourceNotFoundException;
import com.collegecart.repository.CategoryRepository;
import com.collegecart.repository.StudentListingRepository;
import com.collegecart.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentListingService {

    private static final int MAX_IMAGES = 3;
    private static final int MAX_IMAGE_URL_LENGTH = 2048;

    private final StudentListingRepository studentListingRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public StudentListingService(
            StudentListingRepository studentListingRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            EntityManager entityManager) {
        this.studentListingRepository = studentListingRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public ListingResponse createListing(
            ListingCreateRequest request,
            Authentication authentication) {
        User seller = resolveActiveStudent(authentication);
        validateCreateRequest(request);
        Category category = findActiveCategory(request.getCategoryId());
        List<String> imageUrls = normalizeImageUrls(request.getImageUrls());

        StudentListing listing = new StudentListing();
        listing.setSeller(seller);
        listing.setTitle(request.getTitle().trim());
        listing.setDescription(request.getDescription().trim());
        listing.setAskingPrice(request.getAskingPrice());
        listing.setCategory(category);
        listing.setCondition(request.getCondition());
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setSoldAt(null);
        addImages(listing, imageUrls);

        return new ListingResponse(studentListingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public ListingBrowseResponse browseListings(
            String searchTerm,
            Long categoryId,
            ListingCondition condition,
            BigDecimal minimumPrice,
            BigDecimal maximumPrice,
            Pageable pageable) {
        validateBrowseFilters(minimumPrice, maximumPrice, pageable);
        String normalizedSearch = searchTerm == null || searchTerm.isBlank()
                ? null
                : searchTerm.trim();
        Page<StudentListing> results = studentListingRepository.searchAvailableListings(
                normalizedSearch,
                categoryId,
                condition,
                minimumPrice,
                maximumPrice,
                pageable);
        return new ListingBrowseResponse(results);
    }

    @Transactional(readOnly = true)
    public ListingResponse findAvailableListing(Long listingId) {
        StudentListing listing = studentListingRepository.findById(listingId)
                .filter(candidate -> candidate.getStatus() == ListingStatus.AVAILABLE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        return new ListingResponse(listing);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findMyListings(Authentication authentication) {
        User seller = resolveActiveStudent(authentication);
        return studentListingRepository
                .findBySellerIdOrderByCreatedAtDesc(seller.getId(), Pageable.unpaged())
                .getContent()
                .stream()
                .map(ListingResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ListingResponse findMyListing(Long listingId, Authentication authentication) {
        User seller = resolveActiveStudent(authentication);
        return findOwnedListing(listingId, seller.getId())
                .map(ListingResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    @Transactional
    public ListingResponse updateListing(
            Long listingId,
            ListingUpdateRequest request,
            Authentication authentication) {
        User seller = resolveActiveStudent(authentication);
        validateUpdateRequest(request);
        StudentListing listing = findOwnedListing(listingId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        requireAvailableForEdit(listing);
        Category category = findActiveCategory(request.getCategoryId());
        List<String> imageUrls = normalizeImageUrls(request.getImageUrls());

        // Delete old ordered rows before inserting replacements with the same order indexes.
        listing.getImages().clear();
        studentListingRepository.flush();

        listing.setTitle(request.getTitle().trim());
        listing.setDescription(request.getDescription().trim());
        listing.setAskingPrice(request.getAskingPrice());
        listing.setCategory(category);
        listing.setCondition(request.getCondition());
        addImages(listing, imageUrls);

        return new ListingResponse(studentListingRepository.save(listing));
    }

    @Transactional
    public ListingResponse updateListingStatus(
            Long listingId,
            ListingStatusUpdateRequest request,
            Authentication authentication) {
        User seller = resolveActiveStudent(authentication);
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("A listing status is required");
        }
        if (request.getStatus() != ListingStatus.SOLD
                && request.getStatus() != ListingStatus.REMOVED) {
            throw new IllegalArgumentException("Listings may only be marked SOLD or REMOVED");
        }

        StudentListing listing = findOwnedListing(listingId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        requireAvailableForEdit(listing);

        listing.setStatus(request.getStatus());
        listing.setSoldAt(request.getStatus() == ListingStatus.SOLD
                ? java.time.LocalDateTime.now()
                : null);
        return new ListingResponse(studentListingRepository.save(listing));
    }

    private User resolveActiveStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Authentication is required");
        }

        String email = authentication.getName().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            throw new AccessDeniedException("Inactive accounts cannot manage student listings");
        }
        if (user.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only students can manage student listings");
        }
        return user;
    }

    private Category findActiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.isActive()) {
            throw new IllegalArgumentException("Inactive categories cannot be assigned to listings");
        }
        return category;
    }

    private java.util.Optional<StudentListing> findOwnedListing(Long listingId, Long sellerId) {
        return studentListingRepository.findByIdAndSellerId(listingId, sellerId);
    }

    private void requireAvailableForEdit(StudentListing listing) {
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new IllegalArgumentException("Only AVAILABLE listings can be edited or have their status changed");
        }
    }

    private void validateCreateRequest(ListingCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Listing details are required");
        }
        validateEditableFields(
                request.getTitle(), request.getDescription(), request.getAskingPrice(),
                request.getCategoryId(), request.getCondition());
    }

    private void validateUpdateRequest(ListingUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Listing details are required");
        }
        validateEditableFields(
                request.getTitle(), request.getDescription(), request.getAskingPrice(),
                request.getCategoryId(), request.getCondition());
    }

    private void validateEditableFields(
            String title,
            String description,
            BigDecimal askingPrice,
            Long categoryId,
            ListingCondition condition) {
        if (title == null || title.isBlank() || title.trim().length() < 3
                || title.length() > 100) {
            throw new IllegalArgumentException("Listing title must be between 3 and 100 characters");
        }
        if (description == null || description.isBlank() || description.length() > 2000) {
            throw new IllegalArgumentException("Listing description is required and must not exceed 2000 characters");
        }
        if (askingPrice == null || askingPrice.signum() <= 0
                || askingPrice.scale() > 2 || askingPrice.precision() - askingPrice.scale() > 6) {
            throw new IllegalArgumentException("Asking price must be positive and support two decimal places");
        }
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be positive");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Listing condition is required");
        }
    }

    private void validateBrowseFilters(
            BigDecimal minimumPrice,
            BigDecimal maximumPrice,
            Pageable pageable) {
        if (minimumPrice != null && minimumPrice.signum() < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }
        if (maximumPrice != null && maximumPrice.signum() < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }
        if (minimumPrice != null && maximumPrice != null
                && minimumPrice.compareTo(maximumPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot exceed maximum price");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pagination settings are required");
        }
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        if (imageUrls.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("A listing may have at most three images");
        }

        List<String> normalizedUrls = new ArrayList<>(imageUrls.size());
        Set<String> uniqueUrls = new LinkedHashSet<>();
        for (String imageUrl : imageUrls) {
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new IllegalArgumentException("Image URL cannot be blank");
            }
            String normalizedUrl = imageUrl.trim();
            if (normalizedUrl.length() > MAX_IMAGE_URL_LENGTH || !isHttpUrl(normalizedUrl)) {
                throw new IllegalArgumentException("Image URL must be a valid HTTP or HTTPS URL");
            }
            if (!uniqueUrls.add(normalizedUrl)) {
                throw new IllegalArgumentException("Image URLs must not contain duplicates");
            }
            normalizedUrls.add(normalizedUrl);
        }
        return normalizedUrls;
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null && scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void addImages(StudentListing listing, List<String> imageUrls) {
        for (int index = 0; index < imageUrls.size(); index++) {
            StudentListingImage image = new StudentListingImage();
            image.setListing(listing);
            image.setImageUrl(imageUrls.get(index));
            image.setDisplayOrder(index);
            listing.getImages().add(image);
        }
    }
}
