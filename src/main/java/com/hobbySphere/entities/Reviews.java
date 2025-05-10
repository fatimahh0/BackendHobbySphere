package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activities activity;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "review_datetime", updatable = false)
    private LocalDateTime reviewDatetime;

    public Reviews() {}

    public Reviews(Users user, Activities activity, int rating, String comment) {
        this.user = user;
        this.activity = activity;
        setRating(rating); // Enforces range via setter
        this.comment = comment;
    }

    
    @PrePersist
    protected void onCreate() {
        this.reviewDatetime = LocalDateTime.now();
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewDatetime() {
        return reviewDatetime;
    }

    public void setReviewDatetime(LocalDateTime reviewDatetime) {
        this.reviewDatetime = reviewDatetime;
    }
}
