package com.hobbySphere.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
    private Long id;
    private String activity;
    private String customer;
    private int rating;
    private String feedback;
    private LocalDateTime date;

    public ReviewDTO(Long id, String activity, String customer, int rating, String feedback, LocalDateTime date) {
        this.id = id;
        this.activity = activity;
        this.customer = customer;
        this.rating = rating;
        this.feedback = feedback;
        this.date = date;
    }

}
