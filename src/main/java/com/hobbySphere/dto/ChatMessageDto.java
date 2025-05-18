package com.hobbySphere.dto;

import com.hobbySphere.entities.ChatMessages;

public class ChatMessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String message;
    private String sentAt;
    private boolean isMine;

    public ChatMessageDto(ChatMessages chat, Long currentUserId) {
        this.id = chat.getId(); // ✅ fixed
        this.senderId = chat.getSender().getId();
        this.senderName = chat.getSender().getFirstName() + " " + chat.getSender().getLastName();
        this.receiverId = chat.getReceiver().getId();
        this.message = chat.getMessage();
        this.sentAt = chat.getSentAt().toString();
        this.isMine = this.senderId.equals(currentUserId);
    }

    // Getters and Setters
    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }
}
