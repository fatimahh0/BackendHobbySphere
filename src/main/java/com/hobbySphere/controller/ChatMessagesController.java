package com.hobbySphere.controller;

import com.hobbySphere.dto.ChatMessageDto;
import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.ChatMessagesService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class ChatMessagesController {

    private final ChatMessagesService chatService;
    private final UserService usersService;

    public ChatMessagesController(ChatMessagesService chatService, UserService usersService) {
        this.chatService = chatService;
        this.usersService = usersService;
    }

    // ✅ Send a new chat message
    @PostMapping("/send/{receiverId}")
    public ChatMessageDto sendMessage(@PathVariable Long receiverId,
                                      @RequestParam String message,
                                      Principal principal) {
        Users sender = usersService.getUserByEmaill(principal.getName());
        Users receiver = usersService.getUserById(receiverId);
        ChatMessages chat = chatService.sendMessage(sender, receiver, message);
        return new ChatMessageDto(chat, sender.getId());
    }

    // ✅ Get conversation with another user
    @GetMapping("/conversation/{userId}")
    public List<ChatMessageDto> getConversation(@PathVariable Long userId, Principal principal) {
        Users user1 = usersService.getUserByEmaill(principal.getName());
        Users user2 = usersService.getUserById(userId);
        List<ChatMessages> messages = chatService.getConversation(user1, user2);
        return messages.stream()
                .map(msg -> new ChatMessageDto(msg, user1.getId()))
                .collect(Collectors.toList());
    }

    // ✅ Get all messages involving the logged-in user
    @GetMapping("/my")
    public List<ChatMessageDto> getMyMessages(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        List<ChatMessages> messages = chatService.getMessagesByUserr(user);
        return messages.stream()
                .map(msg -> new ChatMessageDto(msg, user.getId()))
                .collect(Collectors.toList());
    }
}
