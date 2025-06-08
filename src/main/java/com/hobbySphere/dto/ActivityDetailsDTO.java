package com.hobbySphere.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ActivityDetailsDTO {

    private Long id;
    private String activityName;
    private String description;
    private Long activityTypeId;
    private String activityTypeName; 
    private String location;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private BigDecimal price;
    private int maxParticipants;
    private String status;
    private String imageUrl;
    private String businessName;

    // ✅ Updated Constructor
    public ActivityDetailsDTO(Long id, String activityName, String description,
                              Long activityTypeId, String activityTypeName,
                              String location, LocalDateTime startDatetime, LocalDateTime endDatetime,
                              BigDecimal price, int maxParticipants, String status,
                              String imageUrl, String businessName) {
        this.id = id;
        this.activityName = activityName;
        this.description = description;
        this.activityTypeId = activityTypeId;
        this.activityTypeName = activityTypeName;
        this.location = location;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.price = price;
        this.maxParticipants = maxParticipants;
        this.status = status;
        this.imageUrl = imageUrl;
        this.businessName = businessName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getActivityTypeId() { return activityTypeId; }
    public void setActivityTypeId(Long activityTypeId) { this.activityTypeId = activityTypeId; }

    public String getActivityTypeName() { return activityTypeName; }
    public void setActivityTypeName(String activityTypeName) { this.activityTypeName = activityTypeName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartDatetime() { return startDatetime; }
    public void setStartDatetime(LocalDateTime startDatetime) { this.startDatetime = startDatetime; }

    public LocalDateTime getEndDatetime() { return endDatetime; }
    public void setEndDatetime(LocalDateTime endDatetime) { this.endDatetime = endDatetime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
}
