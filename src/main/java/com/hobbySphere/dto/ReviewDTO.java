package com.hobbySphere.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private Long activityId;  
    private int rating;
    private String feedback;
    private LocalDateTime date;

    public ReviewDTO() {}

    public ReviewDTO(Long id, Long activityId, int rating, String feedback, LocalDateTime date) {
        this.id = id;
        this.activityId = activityId;
        this.rating = rating;
        this.feedback = feedback;
        this.date = date;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
