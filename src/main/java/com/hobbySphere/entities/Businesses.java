package com.hobbySphere.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Businesses")
public class Businesses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id")
    private Long id;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(nullable = true, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "business_logo_url")
    private String businessLogoUrl;

    @Column(name = "business_banner_url")
    private String businessBannerUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "website_url")
    private String websiteUrl;

    // ✅ FOREIGN KEY version of BusinessStatus
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status")  // Keep the same column name
    private BusinessStatus status;

    @Column(name = "is_public_profile", nullable = true)
    private Boolean isPublicProfile = true;

    @OneToMany(mappedBy = "business", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Activities> activities;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Businesses() {
    }

    public Businesses(String businessName, String email, String phoneNumber, String passwordHash,
                      String businessLogoUrl, String businessBannerUrl, String description, String websiteUrl) {
        this.businessName = businessName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.businessLogoUrl = businessLogoUrl;
        this.businessBannerUrl = businessBannerUrl;
        this.description = description;
        this.websiteUrl = websiteUrl;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getBusinessLogoUrl() { return businessLogoUrl; }
    public void setBusinessLogoUrl(String businessLogoUrl) { this.businessLogoUrl = businessLogoUrl; }

    public String getBusinessBannerUrl() { return businessBannerUrl; }
    public void setBusinessBannerUrl(String businessBannerUrl) { this.businessBannerUrl = businessBannerUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public BusinessStatus getStatus() { return status; }
    public void setStatus(BusinessStatus status) { this.status = status; }

    public Boolean getIsPublicProfile() { return isPublicProfile; }
    public void setIsPublicProfile(Boolean isPublicProfile) { this.isPublicProfile = isPublicProfile; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
        if (this.isPublicProfile == null) this.isPublicProfile = true;
        // ❌ don't set status here — it must be injected from service
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
