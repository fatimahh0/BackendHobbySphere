package com.hobbySphere.services;
import com.hobbySphere.enums.*;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.NotificationsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hobbySphere.repositories.UsersRepository;

import java.util.List;
import java.util.Objects;

@Service
public class NotificationsService {

	
    private final NotificationsRepository notificationsRepo;
    private final UsersRepository usersRepo;
    
    public NotificationsService(NotificationsRepository notificationsRepo, UsersRepository usersRepo) {
        this.notificationsRepo = notificationsRepo;
        this.usersRepo = usersRepo;
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

    
    public void notifyBusiness(Businesses business, String message, NotificationType type) {
        try {
            if (business == null || business.getId() == null) {
                throw new RuntimeException("Business is null or invalid");
            }

            Notifications notification = new Notifications(business, message, type);
            notificationsRepo.save(notification);

            System.out.println("✅ Notification sent to business: " + business.getBusinessName());

        } catch (Exception e) {
            throw new RuntimeException("⚠ Failed to notify business: " + e.getMessage());
        }
    }



    public int countUnreadByUser(Users user) {
        return notificationsRepo.countByUserAndIsReadFalse(user);
    }
    
    public List<Notifications> getAllByBusiness(Businesses business) {
        return notificationsRepo.findByBusinessOrderByCreatedAtDesc(business);
    }

    public List<Notifications> getUnreadByBusiness(Businesses business) {
        return notificationsRepo.findByBusinessAndIsReadFalse(business);
    }

    @Transactional
    public void markAsReadForBusiness(Long notificationId, Businesses business){
        Notifications notif = notificationsRepo.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notif.getBusiness() != null && notif.getBusiness().getId().equals(business.getId())) {
            notif.setIsRead(true);
            notificationsRepo.save(notif);
        } else {
            throw new RuntimeException("Unauthorized");
        }
    }
}
