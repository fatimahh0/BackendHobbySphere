package com.hobbySphere.entities;

import com.hobbySphere.enums.CurrencyType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "currency_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false, unique = true)
    private CurrencyType currencyType;

    // ✅ Add created_at and updated_at as the last two columns
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Default constructor (required by JPA)
    public Currency() {}

    // Constructor that accepts enum
    public Currency(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    // ✅ Constructor that accepts String
    public Currency(String currencyTypeString) {
        this.currencyType = CurrencyType.valueOf(currencyTypeString.toUpperCase());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
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
