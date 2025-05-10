package com.hobbySphere.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "post_likes")
public class PostLikes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

    public PostLikes() {}

    public PostLikes(Users user, Posts post) {
        this.user = user;
        this.post = post;
    }

    // getters and setters
}
