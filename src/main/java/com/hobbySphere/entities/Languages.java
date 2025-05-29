package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.hobbySphere.enums.*;

@Entity
@Table(name = "Languages")
public class Languages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "language_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_name", nullable = false, unique = true)
    private LanguageType languageName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Languages() {}

    public Languages(LanguageType languageName) {
        this.languageName = languageName;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LanguageType getLanguageName() {
        return languageName;
    }

    public void setLanguageName(LanguageType languageName) {
        this.languageName = languageName;
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
