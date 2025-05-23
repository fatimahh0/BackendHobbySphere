package com.hobbySphere.dto;

import java.time.LocalDateTime;

public class AdminActivityDTO {

	private Long id;
    private String title;
    private String businessName;
    private LocalDateTime date;
    private int participants;
    private String description;

    public AdminActivityDTO(Long id, String title, String businessName, LocalDateTime date, int participants, String description) {
    	this.id = id;
        this.title = title;
        this.businessName = businessName;
        this.date = date;
        this.participants = participants;
        this.description = description;
    }

    // Optionally: Add getters
    public Long getId() {return id;}
    public String getTitle() { return title; }
    public String getBusinessName() { return businessName; }
    public LocalDateTime getDate() { return date; }
    public int getParticipants() { return participants; }
    public String getDescription() { return description; }
}
