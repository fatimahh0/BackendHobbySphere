package com.hobbySphere.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feed_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // Values like "Post", "Event", etc.
    
    public FeedType(String name) {
        this.name = name;
    }
}
