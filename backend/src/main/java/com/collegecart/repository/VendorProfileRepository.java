package com.collegecart.repository;

import com.collegecart.entity.VendorProfile;
import com.collegecart.enums.ApprovalStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VendorProfileRepository
        extends JpaRepository<VendorProfile, Long> {

    Optional<VendorProfile> findByUserId(Long userId);

    List<VendorProfile> findByApprovalStatus(ApprovalStatus approvalStatus);

    boolean existsByUserIdAndApprovalStatus(
            Long userId,
            ApprovalStatus approvalStatus);
}