package com.collegecart.repository;

import com.collegecart.entity.StudentListing;
import com.collegecart.enums.ListingCondition;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentListingRepository extends JpaRepository<StudentListing, Long> {

    Optional<StudentListing> findByIdAndSellerId(Long id, Long sellerId);

    Page<StudentListing> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    @Query("""
            select listing from StudentListing listing
            where listing.status = com.collegecart.enums.ListingStatus.AVAILABLE
              and (:categoryId is null or listing.category.id = :categoryId)
              and (:conditionFilter is null or listing.condition = :conditionFilter)
              and (:minimumPrice is null or listing.askingPrice >= :minimumPrice)
              and (:maximumPrice is null or listing.askingPrice <= :maximumPrice)
              and (:searchTerm is null
                   or lower(listing.title) like lower(concat('%', :searchTerm, '%'))
                   or lower(listing.description) like lower(concat('%', :searchTerm, '%')))
            """)
    Page<StudentListing> searchAvailableListings(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId,
            @Param("conditionFilter") ListingCondition conditionFilter,
            @Param("minimumPrice") BigDecimal minimumPrice,
            @Param("maximumPrice") BigDecimal maximumPrice,
            Pageable pageable);
}
