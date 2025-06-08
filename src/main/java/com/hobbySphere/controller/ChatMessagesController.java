package com.hobbySphere.controller;

import com.hobbySphere.dto.ChatMessageDto;
import com.hobbySphere.dto.ContactMessageCountDto;
import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.FriendshipRepository;
import com.hobbySphere.services.ChatMessagesService;
import com.hobbySphere.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class ChatMessagesController {

    private final ChatMessagesService chatService;
    private final UserService usersService;
    private final FriendshipRepository friendshipRepo;

    public ChatMessagesController(ChatMessagesService chatService, UserService usersService, FriendshipRepository friendshipRepo) {
        this.chatService = chatService;
        this.usersService = usersService;
        this.friendshipRepo = friendshipRepo;
    }

    @PostMapping(value = "/send/{receiverId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatMessageDto sendMessageWithImage(@PathVariable Long receiverId,
                                               @RequestParam(required = false) String message,
                                               @RequestPart(required = false) MultipartFile image,
                                               Principal principal) {
        Users sender = usersService.getUserByEmaill(principal.getName());
        Users receiver = usersService.getUserById(receiverId);

        if (sender == null || receiver == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender or receiver not found.");
        }

        // ✅ Block message if not friends
        if (!friendshipRepo.areFriends(sender.getId(), receiver.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only message your friends.");
        }

        String imageUrl = (image != null && !image.isEmpty()) ? chatService.uploadImage(image) : null;

        ChatMessages chat = chatService.sendMessageWithImage(sender, receiver, message, imageUrl);
        return new ChatMessageDto(chat, sender.getId());
    }


    @GetMapping("/conversation/{userId}")
    public List<ChatMessageDto> getConversation(@PathVariable Long userId, Principal principal) {
        Users currentUser = usersService.getUserByEmaill(principal.getName());
        Users otherUser = usersService.getUserById(userId);

        if (currentUser == null || otherUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (!friendshipRepo.areFriends(currentUser.getId(), otherUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not friends.");
        }

        chatService.markMessagesAsRead(currentUser, otherUser);

        List<ChatMessages> messages = chatService.getConversation(currentUser, otherUser);
        return messages.stream()
                .map(msg -> new ChatMessageDto(msg, currentUser.getId()))
                .collect(Collectors.toList());
    }


    @GetMapping("/my")
    public List<ChatMessageDto> getMyMessages(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        List<ChatMessages> messages = chatService.getMessagesByUser(user);
        return messages.stream()
                .map(msg -> new ChatMessageDto(msg, user.getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/count/my")
    public Long countMyMessages(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return chatService.countAllMessagesForUser(user);
    }

    @GetMapping("/count/by-contact")
    public List<ContactMessageCountDto> countMessagesByContact(Principal principal) {
        Users currentUser = usersService.getUserByEmaill(principal.getName());
        List<Object[]> counts = chatService.countMessagesGroupedByContact(currentUser);
        return counts.stream()
                .map(obj -> new ContactMessageCountDto((Long) obj[0], (Long) obj[1]))
                .collect(Collectors.toList());
    }

    @GetMapping("/unread/by-contact")
    public List<ContactMessageCountDto> countUnreadByContact(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        List<Object[]> unreadCounts = chatService.countUnreadMessagesGroupedByContact(user.getId());
        return unreadCounts.stream()
                .map(obj -> new ContactMessageCountDto((Long) obj[0], (Long) obj[1]))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long messageId, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        boolean deleted = chatService.deleteMessageByIdAndUser(messageId, user);

        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this message");
        }
    }

    @PatchMapping("/{messageId}/read")
    public void markMessageAsRead(@PathVariable Long messageId, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        chatService.markSingleMessageAsRead(user, messageId);
    }
}
