package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Interests")
public class Interests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "id.interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserInterests> userInterests;

    // Constructors
    public Interests() {}

    public Interests(String name) {
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

    public Set<UserInterests> getUserInterests() {
        return userInterests;
    }

    public void setUserInterests(Set<UserInterests> userInterests) {
        this.userInterests = userInterests;
    }
}
