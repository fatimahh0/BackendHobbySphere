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

    @Transactional
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

    @Transactional
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

    @Transactional
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

    @Transactional
    public Long countAllMessagesForUser(Users user) {
        return chatRepo.countBySenderOrReceiver(user);
    }

    @Transactional
    public List<Object[]> countMessagesGroupedByContact(Users user) {
        return chatRepo.countMessagesGroupedByContact(user.getId());
    }

    @Transactional
    public List<Object[]> countUnreadMessagesGroupedByContact(Long userId) {
        return chatRepo.countUnreadMessagesGroupedByContact(userId);
    }

    @Transactional
    public void markMessagesAsRead(Users receiver, Users sender) {
        List<ChatMessages> unreadMessages = chatRepo.findUnreadMessages(receiver.getId(), sender.getId());
        for (ChatMessages msg : unreadMessages) {
            msg.setIsRead(true);
        }
        chatRepo.saveAll(unreadMessages);
    }

    @Transactional
    public List<ChatMessages> getConversation(Users user1, Users user2) {
        List<ChatMessages> messages = chatRepo.findConversationBetween(user1.getId(), user2.getId());
        // Avoid LazyInitializationException by initializing needed fields
        messages.forEach(msg -> {
            msg.getSender().getUsername();
            msg.getReceiver().getUsername();
        });
        return messages;
    }

    @Transactional
    public List<ChatMessages> getMessagesByUser(Users user) {
        return chatRepo.findBySenderOrReceiverOrderByMessageDatetimeDesc(user, user);
    }

    @Transactional
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

        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to mark this message as read");
        }

        message.setIsRead(true);
        chatRepo.save(message);
    }
}