package com.hobbySphere.controller;

import com.hobbySphere.dto.CommentDto;
import com.hobbySphere.entities.Comments;
import com.hobbySphere.entities.Users;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.CommentsService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentsController {

    private final CommentsService commentsService;
    private final UserService usersService;
    private final JwtUtil jwtUtil;

    public CommentsController(CommentsService commentsService, UserService usersService, JwtUtil jwtUtil) {
        this.commentsService = commentsService;
        this.usersService = usersService;
        this.jwtUtil = jwtUtil;
    }

    private Users getUserFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) throw new RuntimeException("Missing or invalid Authorization header");
        String jwt = token.substring(7);
        String email = jwtUtil.extractUsername(jwt);
        return usersService.getUserByEmaill(email);
    }

    @PostMapping("/{postId}")
    public CommentDto addComment(@PathVariable Long postId,
                                 @RequestParam String content,
                                 @RequestHeader("Authorization") String authHeader) {
        Users user = getUserFromToken(authHeader);
        Comments comment = commentsService.addComment(postId, content, user);
        return new CommentDto(comment, user.getId());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId,
                                                        @RequestHeader("Authorization") String authHeader) {
        Users user = getUserFromToken(authHeader);
        List<Comments> comments = commentsService.getCommentsByPost(postId);
        List<CommentDto> dtos = comments.stream()
                                        .map(c -> new CommentDto(c, user.getId()))
                                        .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                @RequestHeader("Authorization") String authHeader) {
        Users user = getUserFromToken(authHeader);
        commentsService.deleteComment(commentId, user);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
