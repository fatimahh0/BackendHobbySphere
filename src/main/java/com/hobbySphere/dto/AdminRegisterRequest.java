package com.hobbySphere.dto;

public class AdminRegisterRequest {

    // Required admin fields
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    // Optional fields for linking to a business (with confirmation)
    private Long businessId;
    private String businessEmail;
    private String businessPassword;

    // Constructors
    public AdminRegisterRequest() {}

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getBusinessPassword() {
        return businessPassword;
    }

    public void setBusinessPassword(String businessPassword) {
        this.businessPassword = businessPassword;
    }
}
