package com.hobbySphere.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hobbySphere.enums.ActivityIconEnum;
import com.hobbySphere.enums.IconLibraryEnum;
import com.hobbySphere.enums.InterestEnum;

import jakarta.persistence.*;

@Entity
public class ActivityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_name")
    @JsonProperty("activity_type")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon_name")
    private ActivityIconEnum icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon_library")
    private IconLibraryEnum iconLib;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "interest_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Interests interest;

    // --- Constructors ---
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

    public ActivityType(String name, ActivityIconEnum icon, IconLibraryEnum iconLib, Interests interest) {
        this.name = name;
        this.icon = icon;
        this.iconLib = iconLib;
        this.interest = interest;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ActivityIconEnum getIcon() { return icon; }
    public void setIcon(ActivityIconEnum icon) { this.icon = icon; }

    public IconLibraryEnum getIconLib() { return iconLib; }
    public void setIconLib(IconLibraryEnum iconLib) { this.iconLib = iconLib; }

    public Interests getInterest() { return interest; }
    public void setInterest(Interests interest) { this.interest = interest; }
}
