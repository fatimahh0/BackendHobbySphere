package com.hobbySphere.entities;

public enum NotificationType {
    ACTIVITY_UPDATE("Activity Update"),
    MESSAGE("Message"),
    BOOKING_REMINDER("Booking Reminder"),
    EVENT_REMINDER("Event Reminder");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
