package com.hobbySphere.controller;

import com.hobbySphere.dto.ChatMessageDto;
import com.hobbySphere.dto.ContactMessageCountDto;
import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
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

    public ChatMessagesController(ChatMessagesService chatService, UserService usersService) {
        this.chatService = chatService;
        this.usersService = usersService;
    }

    /**
     * Endpoint to send a message with an optional image.
     * 
     * @param receiverId ID of the receiver
     * @param message Optional text message
     * @param image Optional image to send
     * @param principal Authenticated user
     * @return ChatMessageDto of the saved message
     */
    @PostMapping(value = "/send/{receiverId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatMessageDto sendMessageWithImage(@PathVariable Long receiverId,
                                               @RequestParam(required = false) String message,
                                               @RequestPart(required = false) MultipartFile image,
                                               Principal principal) {
        // Get sender and receiver users
        Users sender = usersService.getUserByEmaill(principal.getName());
        Users receiver = usersService.getUserById(receiverId);

        // Upload image if provided
        String imageUrl = (image != null && !image.isEmpty()) ? chatService.uploadImage(image) : null;

        // Send message via service
        ChatMessages chat = chatService.sendMessageWithImage(sender, receiver, message, imageUrl);

        return new ChatMessageDto(chat, sender.getId());
    }

    /**
     * Get the full conversation between the current user and another user.
     * Also marks all unread messages from the other user as read.
     * 
     * @param userId The other user in the conversation
     * @param principal Authenticated user
     * @return List of ChatMessageDto objects in the conversation
     */
    @GetMapping("/conversation/{userId}")
    public List<ChatMessageDto> getConversation(@PathVariable Long userId, Principal principal) {
        try {
        	Users currentUser = usersService.getUserByEmaill(principal.getName());
        	Users otherUser = usersService.getUserById(userId);

        	if (currentUser == null || otherUser == null) {
        	    System.out.println("User not found: current=" + currentUser + ", other=" + otherUser);
        	    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        	}

        	System.out.println("Current user: " + currentUser.getId());
        	System.out.println("Other user: " + otherUser.getId());


            // Check unread messages
            chatService.markMessagesAsRead(currentUser, otherUser);
            System.out.println("Marked messages as read");

            // Get conversation
            List<ChatMessages> messages = chatService.getConversation(currentUser, otherUser);
            System.out.println("Fetched messages count: " + messages.size());

            return messages.stream()
                    .map(msg -> new ChatMessageDto(msg, currentUser.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Error in getConversation: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch conversation: " + e.getMessage(), e);
        }

    }

    


    /**
     * Returns all messages where the current user is the sender or receiver.
     *
     * @param principal Authenticated user
     * @return List of messages as ChatMessageDto
     */
    @GetMapping("/my")
    public List<ChatMessageDto> getMyMessages(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        List<ChatMessages> messages = chatService.getMessagesByUser(user);
        return messages.stream()
                .map(msg -> new ChatMessageDto(msg, user.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Count total messages (sent or received) for the logged-in user.
     *
     * @param principal Authenticated user
     * @return Number of total messages
     */
    @GetMapping("/count/my")
    public Long countMyMessages(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return chatService.countAllMessagesForUser(user);
    }

    /**
     * Count messages grouped by contact (for chat list overview).
     * Useful to know how many messages exchanged with each user.
     *
     * @param principal Authenticated user
     * @return List of ContactMessageCountDto (contactId + count)
     */
    @GetMapping("/count/by-contact")
    public List<ContactMessageCountDto> countMessagesByContact(Principal principal) {
        Users currentUser = usersService.getUserByEmaill(principal.getName());
        List<Object[]> counts = chatService.countMessagesGroupedByContact(currentUser);

        return counts.stream()
                .map(obj -> new ContactMessageCountDto((Long) obj[0], (Long) obj[1]))
                .collect(Collectors.toList());
    }
    
    /**
     * Count unread messages grouped by contact (to show badge in chat list).
     *
     * @param principal Authenticated user
     * @return List of ContactMessageCountDto (contactId + unreadCount)
     */
    @GetMapping("/unread/by-contact")
    public List<ContactMessageCountDto> countUnreadByContact(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        List<Object[]> unreadCounts = chatService.countUnreadMessagesGroupedByContact(user.getId());

        return unreadCounts.stream()
                .map(obj -> new ContactMessageCountDto((Long) obj[0], (Long) obj[1]))
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a message by its ID (if current user is sender).
     *
     * @param messageId ID of the message to delete
     * @param principal Authenticated user
     */
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
