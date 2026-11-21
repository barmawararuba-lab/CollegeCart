package com.collegecart.repository;

import com.collegecart.entity.StudentListingImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentListingImageRepository extends JpaRepository<StudentListingImage, Long> {

    List<StudentListingImage> findByListingIdAndListingSellerIdOrderByDisplayOrderAsc(
            Long listingId,
            Long sellerId);

    @Query("""
            select image from StudentListingImage image
            where image.listing.id = :listingId
              and image.listing.status = com.collegecart.enums.ListingStatus.AVAILABLE
            order by image.displayOrder asc
            """)
    List<StudentListingImage> findPublicImagesForAvailableListing(
            @Param("listingId") Long listingId);
}
