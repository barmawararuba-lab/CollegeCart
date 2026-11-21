package com.collegecart.repository;

import com.collegecart.entity.VendorOrder;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorOrderRepository extends JpaRepository<VendorOrder, Long> {

    List<VendorOrder> findByVendorProfileIdOrderByCreatedAtDesc(Long vendorProfileId);

    Optional<VendorOrder> findByIdAndVendorProfileId(Long id, Long vendorProfileId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select vendorOrder from VendorOrder vendorOrder "
            + "where vendorOrder.id = :id and vendorOrder.vendorProfile.id = :vendorProfileId")
    Optional<VendorOrder> findByIdAndVendorProfileIdForUpdate(
            @Param("id") Long id,
            @Param("vendorProfileId") Long vendorProfileId);

    List<VendorOrder> findByCheckoutOrderIdAndCheckoutOrderStudentIdOrderByCreatedAtAsc(
            Long checkoutOrderId,
            Long studentId);
}
