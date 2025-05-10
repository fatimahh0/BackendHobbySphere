package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comments")
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

   
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)  // Foreign key to Posts
    private Posts post;

    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Foreign key to Users
    private Users user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "comment_datetime", updatable = false)
    private LocalDateTime commentDatetime;

   
    public Comments() {}

    public Comments(Posts post, Users user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        this.commentDatetime = LocalDateTime.now();
    }

  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Posts getPost() {
        return post;
    }

    public void setPost(Posts post) {
        this.post = post;
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

    public LocalDateTime getCommentDatetime() {
        return commentDatetime;
    }

    public void setCommentDatetime(LocalDateTime commentDatetime) {
        this.commentDatetime = commentDatetime;
    }
}
