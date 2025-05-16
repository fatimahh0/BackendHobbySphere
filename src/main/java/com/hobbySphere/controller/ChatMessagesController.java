package com.hobbySphere.controller;

import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.ChatMessagesService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class ChatMessagesController {

    private final ChatMessagesService chatService;
    private final UserService usersService;

    public ChatMessagesController(ChatMessagesService chatService, UserService usersService) {
        this.chatService = chatService;
        this.usersService = usersService;
    }
 
    @PostMapping("/send/{receiverId}")
    public ChatMessages sendMessage(@PathVariable Long receiverId,
                                    @RequestParam String message,
                                    Principal principal) {
        Users sender = usersService.getUserByEmaill(principal.getName());
        Users receiver = usersService.getUserById(receiverId);
        return chatService.sendMessage(sender, receiver, message);
    }

    @GetMapping("/conversation/{userId}")
    public List<ChatMessages> getConversation(@PathVariable Long userId, Principal principal) {
        Users user1 = usersService.getUserByEmaill(principal.getName());
        Users user2 = usersService.getUserById(userId);
        return chatService.getConversation(user1, user2);
    }
}
