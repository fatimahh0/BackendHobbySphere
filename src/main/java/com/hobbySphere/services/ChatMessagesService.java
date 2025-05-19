package com.hobbySphere.services;

import com.hobbySphere.entities.ChatMessages;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.NotificationType;
import com.hobbySphere.repositories.ChatMessagesRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


import java.util.List;

@Service
public class ChatMessagesService {

    private final ChatMessagesRepository chatRepo;
    private final NotificationsService notificationsService;

    public ChatMessagesService(ChatMessagesRepository chatRepo,
                               NotificationsService notificationsService) {
        this.chatRepo = chatRepo;
        this.notificationsService = notificationsService;
    }

    

    public ChatMessages sendMessage(Users sender, Users receiver, String message) {
        ChatMessages chat = new ChatMessages(sender, receiver, message);
        chat.setSentAt(LocalDateTime.now()); // ✅ Bien défini ici

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


    public List<ChatMessages> getConversation(Users user1, Users user2) {
        return chatRepo.findConversationBetween(user1.getId(), user2.getId());
    }

    public List<ChatMessages> getMessagesByUser(Users user) {
        return chatRepo.findBySenderOrReceiverOrderByMessageDatetimeDesc(user, user);
    }
}
