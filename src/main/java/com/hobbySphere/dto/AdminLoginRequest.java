package com.hobbySphere.dto;

public class AdminLoginRequest {
    private String email;
    private String password;
    private Long businessId;
    private String businessEmail;
    private String businessPassword;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }

    public String getBusinessPassword() { return businessPassword; }
    public void setBusinessPassword(String businessPassword) { this.businessPassword = businessPassword; }
}
