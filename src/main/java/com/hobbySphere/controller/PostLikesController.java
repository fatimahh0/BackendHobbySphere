package com.hobbySphere.controller;

import com.hobbySphere.entities.Users;
import com.hobbySphere.services.PostLikesService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
public class PostLikesController {

    private final PostLikesService likesService;
    private final UserService usersService;

    public PostLikesController(PostLikesService likesService, UserService usersService) {
        this.likesService = likesService;
        this.usersService = usersService;
    }

    @PostMapping("/{postId}/like")
    public String toggleLike(@PathVariable Long postId, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return likesService.toggleLike(postId, user);
    }

    @GetMapping("/{postId}/likes")
    public long countLikes(@PathVariable Long postId) {
        return likesService.countLikes(postId);
    }
}
