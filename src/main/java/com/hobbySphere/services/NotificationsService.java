package com.hobbySphere.services;
import com.hobbySphere.enums.*;

import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.NotificationsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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

    @Transactional
    public void markAsRead(Long notificationId, Users user) {
        Notifications notif = notificationsRepo.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        System.out.println("Notification user ID = " + notif.getUser().getId());
        System.out.println("Logged-in user ID = " + user.getId());

        if (notif.getUser().getId().equals(user.getId())) {
            notif.setIsRead(true); 

            System.out.println("✔ Notification ID " + notif.getId() + " marked as read.");
            notificationsRepo.save(notif); // 
        } else {
            throw new RuntimeException("Non autorisé");
        }
    }


   

    public List<Notifications> getAllByUser1(Users user) {
        return notificationsRepo.findByUserOrderByCreatedAtDesc(user);
    }
    public List<Notifications> getUnreadByUser(Users user) {
        return notificationsRepo.findByUserAndIsReadFalse(user);
    }

    public Notifications getById(Long id) {
        return notificationsRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    public void delete(Notifications notification) {
        notificationsRepo.delete(notification);
    }

    



}
