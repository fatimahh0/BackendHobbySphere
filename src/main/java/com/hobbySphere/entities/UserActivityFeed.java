package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserActivityFeed")
public class UserActivityFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activities activity;

    @Column(name = "feed_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedType feedType;

    @Column(name = "feed_datetime", updatable = false)
    private LocalDateTime feedDatetime;

    // ✅ Add created_at and updated_at as the last two columns
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public UserActivityFeed() {}

    public UserActivityFeed(Users user, Activities activity, FeedType feedType) {
        this.user = user;
        this.activity = activity;
        this.feedType = feedType;
    }

    @PrePersist
    protected void onCreate() {
        this.feedDatetime = LocalDateTime.now();
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Activities getActivity() {
        return activity;
    }

    public void setActivity(Activities activity) {
        this.activity = activity;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    public LocalDateTime getFeedDatetime() {
        return feedDatetime;
    }

    public void setFeedDatetime(LocalDateTime feedDatetime) {
        this.feedDatetime = feedDatetime;
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
