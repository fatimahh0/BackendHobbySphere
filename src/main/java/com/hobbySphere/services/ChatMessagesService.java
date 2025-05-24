package com.hobbySphere.services;

import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.NotificationType;
import com.hobbySphere.repositories.ChatMessagesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatMessagesService {

    private final ChatMessagesRepository chatRepo;
    private final NotificationsService notificationsService;

    public ChatMessagesService(ChatMessagesRepository chatRepo,
                               NotificationsService notificationsService) {
        this.chatRepo = chatRepo;
        this.notificationsService = notificationsService;
    }

    /**
     * Sends a basic text message from sender to receiver.
     * Also triggers a notification if the sender and receiver are not the same.
     */
    public ChatMessages sendMessage(Users sender, Users receiver, String message) {
        ChatMessages chat = new ChatMessages(sender, receiver, message);
        chat.setSentAt(LocalDateTime.now());
        ChatMessages saved = chatRepo.save(chat);

        if (!sender.getId().equals(receiver.getId())) {
            notificationsService.createNotification(
                receiver,
                sender.getUsername() + " sent you a message.",
                NotificationType.MESSAGE
            );
        }
        return saved;
    }

    /**
     * Sends a message that may include both text and an optional image.
     * Also sends a notification if sender and receiver are different.
     */
    public ChatMessages sendMessageWithImage(Users sender, Users receiver, String message, String imageUrl) {
        ChatMessages chat = new ChatMessages();
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setMessage(message != null ? message : "");
        chat.setImageUrl(imageUrl);
        chat.setSentAt(LocalDateTime.now());

        ChatMessages saved = chatRepo.save(chat);

        if (!sender.getId().equals(receiver.getId())) {
            notificationsService.createNotification(
                receiver,
                sender.getUsername() + " sent you a message.",
                NotificationType.MESSAGE
            );
        }
        return saved;
    }

    /**
     * Uploads an image file to the server.
     * Returns the accessible URL path of the uploaded image.
     */
    public String uploadImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get("uploads/" + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    /**
     * Returns the total number of messages (sent or received) for a given user.
     */
    public Long countAllMessagesForUser(Users user) {
        return chatRepo.countBySenderOrReceiver(user);
    }

    /**
     * Returns the number of messages grouped by each contact (user involved in chat).
     */
    public List<Object[]> countMessagesGroupedByContact(Users user) {
        return chatRepo.countMessagesGroupedByContact(user.getId());
    }

    /**
     * Returns the count of unread messages grouped by contact for the given user.
     */
    public List<Object[]> countUnreadMessagesGroupedByContact(Long userId) {
        return chatRepo.countUnreadMessagesGroupedByContact(userId);
    }

    /**
     * Marks all unread messages as read from a specific sender to a receiver.
     */
    public void markMessagesAsRead(Users receiver, Users sender) {
        List<ChatMessages> unreadMessages = chatRepo.findUnreadMessages(receiver.getId(), sender.getId());
        for (ChatMessages msg : unreadMessages) {
            msg.setIsRead(true);
        }
        chatRepo.saveAll(unreadMessages);
    }

    /**
     * Retrieves the full conversation (messages) between two users.
     */
    public List<ChatMessages> getConversation(Users user1, Users user2) {
        return chatRepo.findConversationBetween(user1.getId(), user2.getId());
    }

    /**
     * Returns all messages (sent or received) for the given user,
     * ordered by the most recent first.
     */
    public List<ChatMessages> getMessagesByUser(Users user) {
        return chatRepo.findBySenderOrReceiverOrderByMessageDatetimeDesc(user, user);
    }

    public boolean deleteMessageByIdAndUser(Long messageId, Users user) {
        ChatMessages message = chatRepo.findById(messageId).orElse(null);
        if (message == null || !message.getSender().getId().equals(user.getId())) {
            return false;
        }

        chatRepo.delete(message);
        return true;
    }

    @Transactional
    public void markSingleMessageAsRead(Users currentUser, Long messageId) {
        ChatMessages message = chatRepo.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        // Only receiver can mark as read
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to mark this message as read");
        }

        message.setIsRead(true);
        chatRepo.save(message);
    }
}
