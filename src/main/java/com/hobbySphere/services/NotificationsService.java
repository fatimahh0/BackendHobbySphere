package com.hobbySphere.services;

import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.NotificationType;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.NotificationsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationsService {

    private final NotificationsRepository notificationsRepo;

    public NotificationsService(NotificationsRepository notificationsRepo) {
        this.notificationsRepo = notificationsRepo;
    }

    public void createNotification(Users receiver, String message, NotificationType type) {
        System.out.println(" Create notification : " + message + " to " + receiver.getUsername());
        Notifications notification = new Notifications(receiver, message, type);
        notificationsRepo.save(notification);
    }


    public List<Notifications> getAllByUser(Users user) {
        return notificationsRepo.findAll().stream()
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public void markAsRead(Long notificationId, Users user) {
        Notifications notif = notificationsRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notif.getUser().getId().equals(user.getId())) {
            notif.setIsRead(true);
            notificationsRepo.save(notif);
        } else {
            throw new RuntimeException("Non autorisé");
        }
    }

   

    public List<Notifications> getAllByUser1(Users user) {
        return notificationsRepo.findByUserOrderByCreatedAtDesc(user);
    }

}
