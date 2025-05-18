package com.hobbySphere.dto;

import com.hobbySphere.entities.Comments;
import com.hobbySphere.entities.Users;

public class CommentDto {
    public Long id;
    public String content;
    public String createdAt;

    public Long userId;
    public String firstName;
    public String lastName;
    public String profilePictureUrl;
    public boolean isMine;

    public CommentDto(Comments comment, Long currentUserId) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.firstName = comment.getUser().getFirstName();
        this.lastName = comment.getUser().getLastName();
        this.profilePictureUrl = comment.getUser().getProfilePictureUrl();
        this.isMine = comment.getUser().getId().equals(currentUserId);
    }
}
