package com.hobbySphere.dto;

public class UserSummaryDTO {
    private String fullName;
    private String email;
    private String role;

    public UserSummaryDTO(String fullName, String email, String role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
