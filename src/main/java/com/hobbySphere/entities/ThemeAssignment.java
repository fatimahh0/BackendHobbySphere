package com.hobbySphere.entities;

import jakarta.persistence.*;

@Entity
@Table(
    name = "theme_assignment",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"business_id"}),
        @UniqueConstraint(columnNames = {"user_id"})
    }
)
public class ThemeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Only one of these should be set per row
    @ManyToOne
    @JoinColumn(name = "business_id", referencedColumnName = "business_id")
    private Businesses business;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id", referencedColumnName = "id")
    private Theme theme;

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public Businesses getBusiness() {
        return business;
    }

    public void setBusiness(Businesses business) {
        this.business = business;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Theme getTheme() {
        return theme;
    }
    
    public void setTheme (Theme theme) {
    	this.theme = theme;
    	}
}
