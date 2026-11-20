
package com.collegecart.service;

import com.collegecart.entity.VendorProfile;
import com.collegecart.enums.ApprovalStatus;
import com.collegecart.repository.VendorProfileRepository;
import com.collegecart.dto.PendingVendorResponse;
import org.springframework.transaction.annotation.Transactional;
import com.collegecart.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorProfileService {

    private final VendorProfileRepository vendorProfileRepository;

    public VendorProfileService(
            VendorProfileRepository vendorProfileRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
    }

    public Optional<VendorProfile> findByUserId(Long userId) {
        return vendorProfileRepository.findByUserId(userId);
    }

    public List<PendingVendorResponse> findPendingVendors() {
    return vendorProfileRepository
            .findByApprovalStatus(ApprovalStatus.PENDING)
            .stream()
            .map(PendingVendorResponse::new)
            .toList();
}

    
@Transactional
public VendorProfile updateApprovalStatus(
        Long vendorId, ApprovalStatus status) {

    VendorProfile vendor = vendorProfileRepository.findById(vendorId)
        .orElseThrow(() ->
                new ResourceNotFoundException("Vendor profile not found"));

    if (vendor.getApprovalStatus() != ApprovalStatus.PENDING) {
        throw new IllegalArgumentException(
                "This vendor application has already been reviewed");
    }

    vendor.setApprovalStatus(status);

    return vendorProfileRepository.save(vendor);
}

}
