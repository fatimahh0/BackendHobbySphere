package com.hobbySphere.dto;

import java.time.LocalDateTime;

public class ActivitySummaryDTO {
    private Long id;
    private String title;
    private String businessName;
    private LocalDateTime date;
    private int participants;

    public ActivitySummaryDTO(Long id, String title, String businessName, LocalDateTime date, int participants) {
        this.id = id;
        this.title = title;
        this.businessName = businessName;
        this.date = date;
        this.participants = participants;
    }

    // Getters and setters (or use Lombok @Data if preferred)
}
