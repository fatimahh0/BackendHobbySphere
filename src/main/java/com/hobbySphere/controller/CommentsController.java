package com.hobbySphere.controller;

import com.hobbySphere.dto.CommentDto;
import com.hobbySphere.entities.Comments;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.CommentsService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentsController {

    private final CommentsService commentsService;
    private final UserService usersService;

    public CommentsController(CommentsService commentsService, UserService usersService) {
        this.commentsService = commentsService;
        this.usersService = usersService;
    }

    @PostMapping("/{postId}")
    public CommentDto addComment(@PathVariable Long postId,
                                 @RequestParam String content,
                                 Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        Comments comment = commentsService.addComment(postId, content, user);
        return new CommentDto(comment, user.getId()); // ✅ ajoute l’auteur
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId,
                                                        Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        List<Comments> comments = commentsService.getCommentsByPost(postId);
        List<CommentDto> dtos = comments.stream()
                                        .map(c -> new CommentDto(c, user.getId())) 
                                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        commentsService.deleteComment(commentId, user); 
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
