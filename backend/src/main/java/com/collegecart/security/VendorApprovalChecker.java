
package com.collegecart.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.collegecart.entity.User;
import com.collegecart.enums.ApprovalStatus;
import com.collegecart.enums.Role;
import com.collegecart.repository.UserRepository;
import com.collegecart.repository.VendorProfileRepository;

@Component("vendorApprovalChecker")
public class VendorApprovalChecker {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;

    public VendorApprovalChecker(
            UserRepository userRepository,
            VendorProfileRepository vendorProfileRepository) {

        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
    }

    public boolean isApproved(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null || user.getRole() != Role.VENDOR) {
            return false;
        }

        return vendorProfileRepository.existsByUserIdAndApprovalStatus(
                user.getId(),
                ApprovalStatus.APPROVED
        );
    }
}
