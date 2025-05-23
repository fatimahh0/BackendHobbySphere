package com.hobbySphere.dto;

public class AdminNotificationPreferencesDTO {
    private String username; // ✅ Add this field
    private boolean notifyActivityUpdates;
    private boolean notifyUserFeedback;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isNotifyActivityUpdates() {
        return notifyActivityUpdates;
    }

    public void setNotifyActivityUpdates(boolean notifyActivityUpdates) {
        this.notifyActivityUpdates = notifyActivityUpdates;
    }

    public boolean isNotifyUserFeedback() {
        return notifyUserFeedback;
    }

    public void setNotifyUserFeedback(boolean notifyUserFeedback) {
        this.notifyUserFeedback = notifyUserFeedback;
    }
}
