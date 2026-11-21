package com.collegecart.repository;

import com.collegecart.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByVendorProfileId(Long vendorProfileId);

    Optional<Product> findByIdAndVendorProfileId(Long id, Long vendorProfileId);

    List<Product> findByActiveTrue();

    List<Product> findByActiveTrueAndCategoryId(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id in :productIds order by product.id asc")
    List<Product> findAllByIdInSortedForUpdate(@Param("productIds") Collection<Long> productIds);
}
