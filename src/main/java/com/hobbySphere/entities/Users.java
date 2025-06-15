package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.hobbySphere.enums.LanguageType;

@Entity
@Table(name = "Users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = true)
    private String email;
    
    @Column(unique = true,nullable = true)
    private String phoneNumber;


    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "profile_picture_url", nullable = true)
    private String profilePictureUrl;

    @Column(nullable = true) // ✅ Allow null temporarily to avoid migration error
    private String status; // ✅ Status field without hard NOT NULL during migration

    // ✅ Add updated_at as the last column
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language")
    private LanguageType preferredLanguage;

    public LanguageType getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(LanguageType preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ActivityBookings> activityBookings;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "Active"; // ✅ Set default if null
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        }
}