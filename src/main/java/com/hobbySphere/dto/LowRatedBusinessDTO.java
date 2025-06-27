package com.hobbySphere.dto;

import com.hobbySphere.enums.BusinessStatus;

public class LowRatedBusinessDTO {
    private Long id;
    private String name;
    private BusinessStatus status;
    private double averageRating;

    public LowRatedBusinessDTO(Long id, String name, BusinessStatus status, double averageRating) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.averageRating = averageRating;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BusinessStatus getStatus() {
        return status;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
