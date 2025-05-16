package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    // Many-to-One relationship: Many posts can belong to one user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column
    private String hashtags;

    @Column(name = "post_datetime", updatable = false)
    private LocalDateTime postDatetime;

   
    public Posts() {}

    public Posts(Users user, String content, String imageUrl, String hashtags) {
        this.user = user;
        this.content = content;
        this.imageUrl = imageUrl;
        this.hashtags = hashtags;
    }

   
    @PrePersist
    protected void onCreate() {
        this.postDatetime = LocalDateTime.now();
    }

  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }

    public LocalDateTime getPostDatetime() {
        return postDatetime;
    }

    public void setPostDatetime(LocalDateTime postDatetime) {
        this.postDatetime = postDatetime;
    }
}
