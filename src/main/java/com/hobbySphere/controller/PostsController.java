package com.hobbySphere.controller;

import com.hobbySphere.dto.PostDto;
import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.PostService;
import com.hobbySphere.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostsController {

    private final PostService postsService;
    private final UserService usersService;

    public PostsController(PostService postsService, UserService usersService) {
        this.postsService = postsService;
        this.usersService = usersService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public Posts createPost(@RequestParam String content,
                            @RequestParam(required = false) MultipartFile image,
                            @RequestParam(required = false) String hashtags,
                            Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return postsService.createPost(content, image, hashtags, user);
    }

    @GetMapping
    public ResponseEntity<List<PostDto>> getAllPosts(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            Users user = usersService.getUserByEmaill(principal.getName());
            List<PostDto> postDtos = postsService.getAllPostDtos(user.getId());
            return ResponseEntity.ok(postDtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        postsService.deletePost(postId, user);
        return ResponseEntity.ok("post delete succeful");
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDto>> getPostsByUser(@PathVariable Long userId) {
        List<PostDto> postDtos = postsService.getPostDtosByUser(userId);
        return ResponseEntity.ok(postDtos);
    }

    // Delete a specific post if it belongs to the user
    @DeleteMapping("/{postId}/user/{userId}")
    public ResponseEntity<?> deletePostByUser(@PathVariable Long postId, @PathVariable Long userId) {
        boolean deleted = postsService.deletePostByUser(postId, userId);
        if (deleted) {
            return ResponseEntity.ok("Post deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this post.");
        }
    }
   
} 
