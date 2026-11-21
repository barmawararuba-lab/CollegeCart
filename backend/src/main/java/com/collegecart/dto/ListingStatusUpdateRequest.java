package com.collegecart.dto;

import com.collegecart.enums.ListingStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class ListingStatusUpdateRequest {

    @NotNull(message = "A listing status is required")
    private ListingStatus status;

    public ListingStatusUpdateRequest() {
    }

    @AssertTrue(message = "Listings may only be marked SOLD or REMOVED")
    public boolean isStatusChangeSupported() {
        return status == null
                || status == ListingStatus.SOLD
                || status == ListingStatus.REMOVED;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }
}
