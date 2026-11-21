package com.collegecart.controller;

import com.collegecart.dto.ListingBrowseResponse;
import com.collegecart.dto.ListingCreateRequest;
import com.collegecart.dto.ListingResponse;
import com.collegecart.dto.ListingStatusUpdateRequest;
import com.collegecart.dto.ListingUpdateRequest;
import com.collegecart.enums.ListingCondition;
import com.collegecart.service.StudentListingService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('STUDENT')")
public class StudentListingController {

    private final StudentListingService studentListingService;

    public StudentListingController(StudentListingService studentListingService) {
        this.studentListingService = studentListingService;
    }

    @GetMapping("/api/secondhand/listings")
    public ResponseEntity<ListingBrowseResponse> browseListings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ListingCondition condition,
            @RequestParam(name = "minPrice", required = false) BigDecimal minimumPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maximumPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
                    Pageable pageable) {
        return ResponseEntity.ok(studentListingService.browseListings(
                search, categoryId, condition, minimumPrice, maximumPrice, pageable));
    }

    @GetMapping("/api/secondhand/listings/{id}")
    public ResponseEntity<ListingResponse> getAvailableListing(@PathVariable Long id) {
        return ResponseEntity.ok(studentListingService.findAvailableListing(id));
    }

    @GetMapping("/api/student/listings")
    public ResponseEntity<List<ListingResponse>> getMyListings(Authentication authentication) {
        return ResponseEntity.ok(studentListingService.findMyListings(authentication));
    }

    @PostMapping("/api/student/listings")
    public ResponseEntity<ListingResponse> createListing(
            @Valid @RequestBody ListingCreateRequest request,
            Authentication authentication) {
        ListingResponse listing = studentListingService.createListing(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(listing);
    }

    @GetMapping("/api/student/listings/{id}")
    public ResponseEntity<ListingResponse> getMyListing(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(studentListingService.findMyListing(id, authentication));
    }

    @PutMapping("/api/student/listings/{id}")
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(studentListingService.updateListing(id, request, authentication));
    }

    @PatchMapping("/api/student/listings/{id}/status")
    public ResponseEntity<ListingResponse> updateListingStatus(
            @PathVariable Long id,
            @Valid @RequestBody ListingStatusUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                studentListingService.updateListingStatus(id, request, authentication));
    }
}
