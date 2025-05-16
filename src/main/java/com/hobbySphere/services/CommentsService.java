package com.hobbySphere.services;

import com.hobbySphere.entities.Comments;
import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.NotificationType;
import com.hobbySphere.repositories.CommentsRepository;
import com.hobbySphere.repositories.PostsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentsService {

    private final CommentsRepository commentsRepo;
    private final PostsRepository postsRepo;
    private final NotificationsService notificationsService;

    public CommentsService(CommentsRepository commentsRepo,
                           PostsRepository postsRepo,
                           NotificationsService notificationsService) {
        this.commentsRepo = commentsRepo;
        this.postsRepo = postsRepo;
        this.notificationsService = notificationsService;
    }

    public Comments addComment(Long postId, String content, Users user) {
        Posts post = postsRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comments comment = new Comments(post, user, content);
        Comments savedComment = commentsRepo.save(comment);

        // 🔔 Ajouter notification au propriétaire du post
        if (!user.getId().equals(post.getUser().getId())) {
            notificationsService.createNotification(
                    post.getUser(),
                    user.getUsername() + " comment in your post",
                    NotificationType.ACTIVITY_UPDATE
            );
        }

        return savedComment;
    }

    public List<Comments> getCommentsByPost(Long postId) {
        Posts post = postsRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentsRepo.findByPost(post);
    }
    
    
}
