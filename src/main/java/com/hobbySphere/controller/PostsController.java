package com.hobbySphere.controller;

import com.hobbySphere.entities.Posts;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.PostService;
import com.hobbySphere.services.UserService;

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
    public List<Posts> getAllPosts() {
        return postsService.getAllPosts();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        postsService.deletePost(postId, user);
        return ResponseEntity.ok("post delete succeful");
    }
}
