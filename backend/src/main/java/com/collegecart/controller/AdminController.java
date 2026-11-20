package com.collegecart.controller;

import com.collegecart.dto.PendingVendorResponse;
import com.collegecart.enums.ApprovalStatus;
import com.collegecart.service.VendorProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.collegecart.dto.PendingVendorResponse;
import com.collegecart.enums.ApprovalStatus;

import org.springframework.security.core.Authentication;

import com.collegecart.dto.UserManagementResponse;
import com.collegecart.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VendorProfileService vendorProfileService;
    private final UserService userService;

    public AdminController(
        VendorProfileService vendorProfileService,
        UserService userService) {

        this.vendorProfileService = vendorProfileService;
        this.userService = userService;
    }

    @GetMapping("/vendors/pending")
    public ResponseEntity<List<PendingVendorResponse>> getPendingVendors() {
        return ResponseEntity.ok(
                vendorProfileService.findPendingVendors()
        );
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserManagementResponse>> getAllUsers() {

        return ResponseEntity.ok(
                userService.findAllUsers()
        );
    }

    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(
            @PathVariable Long userId,
            Authentication authentication) {

        if (userService.findByEmail(authentication.getName())
                .map(user -> user.getId().equals(userId))
                .orElse(false)) {
            return ResponseEntity.badRequest()
                    .body("You cannot deactivate your own admin account");
        }

        userService.updateUserStatus(userId, false);

        return ResponseEntity.ok("User deactivated successfully");
    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<String> activateUser(
            @PathVariable Long userId) {

        userService.updateUserStatus(userId, true);

        return ResponseEntity.ok("User activated successfully");
    }

        @PatchMapping("/vendors/{vendorId}/approve")
    public ResponseEntity<String> approveVendor(
            @PathVariable Long vendorId) {

        vendorProfileService.updateApprovalStatus(
                vendorId,
                ApprovalStatus.APPROVED
        );

        return ResponseEntity.ok("Vendor approved successfully");
    }

    @PatchMapping("/vendors/{vendorId}/reject")
    public ResponseEntity<String> rejectVendor(
            @PathVariable Long vendorId) {

        vendorProfileService.updateApprovalStatus(
                vendorId,
                ApprovalStatus.REJECTED
        );

        return ResponseEntity.ok("Vendor application rejected");
    }
}
