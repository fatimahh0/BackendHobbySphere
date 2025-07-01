package com.hobbySphere.services;

import com.hobbySphere.entities.NotificationTypeEntity;
import com.hobbySphere.repositories.NotificationTypeRepository;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.NotificationsRepository;
import com.hobbySphere.repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationsService {

    private final NotificationsRepository notificationsRepo;
    private final UsersRepository usersRepo;
    private final NotificationTypeRepository notificationTypeRepo;

    public NotificationsService(
            NotificationsRepository notificationsRepo,
            UsersRepository usersRepo,
            NotificationTypeRepository notificationTypeRepo
    ) {
        this.notificationsRepo = notificationsRepo;
        this.usersRepo = usersRepo;
        this.notificationTypeRepo = notificationTypeRepo;
    }

    public void createNotification(Users receiver, String message, String typeCode) {
        System.out.println(" Create notification : " + message + " to " + receiver.getUsername());

        NotificationTypeEntity type = notificationTypeRepo.findByCode(typeCode)
                .orElseThrow(() -> new RuntimeException("NotificationType not found: " + typeCode));

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

        if (notif.getUser().getId().equals(user.getId())) {
            notif.setIsRead(true);
            notificationsRepo.save(notif);
            System.out.println("✔ Notification ID " + notif.getId() + " marked as read.");
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

    public void notifyBusiness(Businesses business, String message, String typeCode) {
        try {
            if (business == null || business.getId() == null) {
                throw new RuntimeException("Business is null or invalid");
            }

            NotificationTypeEntity type = notificationTypeRepo.findByCode(typeCode)
                    .orElseThrow(() -> new RuntimeException("NotificationType not found: " + typeCode));

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
    public void markAsReadForBusiness(Long notificationId, Businesses business) {
        Notifications notif = notificationsRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notif.getBusiness() != null && notif.getBusiness().getId().equals(business.getId())) {
            notif.setIsRead(true);
            notificationsRepo.save(notif);
        } else {
            throw new RuntimeException("Unauthorized");
        }
    }

    public void notifyAdmin(AdminUsers admin, String message, String typeCode) {
        NotificationTypeEntity type = notificationTypeRepo.findByCode(typeCode)
                .orElseThrow(() -> new RuntimeException("NotificationType not found: " + typeCode));

        Notifications notification = new Notifications();
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());

        Users user = new Users();
        user.setId(admin.getAdminId());
        user.setUsername(admin.getUsername());
        user.setEmail(admin.getEmail());

        notification.setUser(user);

        notificationsRepo.save(notification);
        System.out.println("✅ Admin notification sent to: " + admin.getEmail());
    }
}
