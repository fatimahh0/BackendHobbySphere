package com.hobbySphere.entities;

public enum FeedType {
    POST("Post"),
    EVENT("Event"),
    ACTIVITY("Activity"),
    REVIEW("Review");

    private final String value;

    FeedType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
