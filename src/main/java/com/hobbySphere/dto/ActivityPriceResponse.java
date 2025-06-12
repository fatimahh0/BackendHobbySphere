package com.hobbySphere.dto;

import java.math.BigDecimal;

public class ActivityPriceResponse {
    private Long activityId;
    private String activityName;
    private BigDecimal price;
    private String currencySymbol;

    // Constructor
    public ActivityPriceResponse(Long activityId, String activityName, BigDecimal price, String currencySymbol) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.price = price;
        this.currencySymbol = currencySymbol;
    }

    // Getters and Setters
    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
}
