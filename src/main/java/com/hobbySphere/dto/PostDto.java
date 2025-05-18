package com.hobbySphere.dto;

import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;

public class PostDto {
    public Long id;
    public String content;
    public String hashtags;
    public String imageUrl;
    public boolean isLiked;
    public int likeCount;
    public int commentCount;
    public String postDatetime;
    public Long userId;
    public String firstName;
    public String lastName;
    public String profilePictureUrl;

    public PostDto(Posts post, Long currentUserId) {
        this.id = post.getId();
        this.content = post.getContent();
        this.hashtags = post.getHashtags();
        this.imageUrl = post.getImageUrl();
        this.postDatetime = post.getPostDatetime().toString();
        this.likeCount = post.getLikedUsers() != null ? post.getLikedUsers().size() : 0;
        this.commentCount = post.getComments() != null ? post.getComments().size() : 0;
        this.isLiked = post.getLikedUsers().stream()
                .anyMatch(user -> user.getId().equals(currentUserId));

        Users user = post.getUser();
        if (user != null) {
            this.userId = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.profilePictureUrl = user.getProfilePictureUrl();
        }
    }
}