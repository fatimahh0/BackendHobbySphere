package com.hobbySphere.controller;

import com.hobbySphere.entities.Comments;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.CommentsService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
    public Comments addComment(@PathVariable Long postId,
            @RequestParam String content,
            Principal principal) {
		Users user = usersService.getUserByEmaill(principal.getName());
		return commentsService.addComment(postId, content, user); 
		}

		@GetMapping("/{postId}")
		public List<Comments> getComments(@PathVariable Long postId) {
		return commentsService.getCommentsByPost(postId); // 
		}

}
