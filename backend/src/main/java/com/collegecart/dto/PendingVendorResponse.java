
package com.collegecart.dto;

import com.collegecart.entity.VendorProfile;
import com.collegecart.enums.ApprovalStatus;

public class PendingVendorResponse {

    private Long vendorProfileId;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String shopName;
    private String shopDescription;
    private String shopAddress;
    private ApprovalStatus approvalStatus;

    public PendingVendorResponse(VendorProfile vendor) {
        this.vendorProfileId = vendor.getId();
        this.userId = vendor.getUser().getId();
        this.fullName = vendor.getUser().getFullName();
        this.email = vendor.getUser().getEmail();
        this.phoneNumber = vendor.getUser().getPhoneNumber();
        this.shopName = vendor.getShopName();
        this.shopDescription = vendor.getShopDescription();
        this.shopAddress = vendor.getShopAddress();
        this.approvalStatus = vendor.getApprovalStatus();
    }

    public Long getVendorProfileId() {
        return vendorProfileId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }
}
