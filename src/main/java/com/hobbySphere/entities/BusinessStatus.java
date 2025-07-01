package com.hobbySphere.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "ACTIVE", "INACTIVE", "DELETED"
    
    public BusinessStatus() {
    }
    
    public BusinessStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


}
