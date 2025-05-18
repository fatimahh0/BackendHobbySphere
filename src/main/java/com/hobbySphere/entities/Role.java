package com.hobbySphere.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "role") // match your DB table name
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true) // ✅ this must match your DB column name
    private String name;

    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
