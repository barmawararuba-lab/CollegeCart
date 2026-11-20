
package com.collegecart.dto;

import com.collegecart.entity.User;
import com.collegecart.enums.Role;

import java.time.LocalDateTime;

public class UserManagementResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    public UserManagementResponse(User user) {
        this.userId = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
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

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
