package com.hobbySphere.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hobbySphere.enums.PostVisibility; // Import the enum

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column
    private String hashtags;

    @Column(name = "post_datetime", updatable = false)
    private LocalDateTime postDatetime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔹 New field: post visibility (public or friends only)
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PostVisibility visibility = PostVisibility.PUBLIC;

    // Users who liked the post
    @ManyToMany
    @JoinTable(
        name = "post_likes",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<Users> likedUsers = new HashSet<>();

    // Comments linked to this post
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comments> comments = new ArrayList<>();

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
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === Getters & Setters ===

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Users> getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(Set<Users> likedUsers) {
        this.likedUsers = likedUsers;
    }

    public List<Comments> getComments() {
        return comments;
    }

    public void setComments(List<Comments> comments) {
        this.comments = comments;
    }

    // Count total likes
    public int getLikeCount() {
        return likedUsers != null ? likedUsers.size() : 0;
    }

    // Count total comments
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    // 🔹 Getter/Setter for visibility
    public PostVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PostVisibility visibility) {
        this.visibility = visibility;
    }
}
