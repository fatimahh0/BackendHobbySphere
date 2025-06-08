package com.hobbySphere.enums;

public enum NotificationType {
    ACTIVITY_UPDATE("Activity Update"),
    MESSAGE("Message"),
    BOOKING_REMINDER("Booking Reminder"),
    EVENT_REMINDER("Event Reminder"),

    //  New friend/block types
    FRIEND_REQUEST_SENT("Friend Request Sent"),
    FRIEND_REQUEST_ACCEPTED("Friend Request Accepted"),
    FRIEND_REQUEST_REJECTED("Friend Request Rejected"),
    FRIEND_REMOVED("Friend Removed"),
    FRIEND_REQUEST_CANCELLED("Friend Request Cancelled"),
    FRIEND_BLOCKED("Friend Blocked");
   

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
