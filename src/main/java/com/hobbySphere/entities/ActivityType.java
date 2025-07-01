package com.hobbySphere.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hobbySphere.enums.InterestEnum;

import jakarta.persistence.*;

@Entity
public class ActivityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_name")              // DB column
    @JsonProperty("activity_type")               // JSON field
    private String name;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "interest_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Interests interest;

    public ActivityType() {}

    public ActivityType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public ActivityType(Long id, String name, Interests interest) {
        this.id = id;
        this.name = name;
        this.interest = interest;
    }
    
    public ActivityType(String name, Interests interest) {
        this.name = name;
        this.interest = interest;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Interests getInterest() { return interest; }
    public void setInterest(Interests interest) { this.interest = interest; }
}
