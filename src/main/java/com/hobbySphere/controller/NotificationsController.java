package com.hobbySphere.controller;

import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.NotificationsService;
import com.hobbySphere.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationsService notificationsService;
    private final UserService usersService;

    public NotificationsController(NotificationsService notificationsService, UserService usersService) {
        this.notificationsService = notificationsService;
        this.usersService = usersService;
    }

    // ✅ Get all notifications
    @GetMapping
    public List<Notifications> getNotifications(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return notificationsService.getAllByUser1(user);
    }

    // ✅ Mark as read
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        notificationsService.markAsRead(id, user);
    }


    // ✅ Get count of ALL notifications
    @GetMapping("/count")
    public long getNotificationCount(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return notificationsService.getAllByUser1(user).size();
    }

    // ✅ Get count of UNREAD notifications (recommended for badge)
    @GetMapping("/unread-count")
    public int getUnreadNotificationCount(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return notificationsService.getUnreadByUser(user).size();
    }
    
    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        Notifications notification = notificationsService.getById(id);

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this notification");
        }

        notificationsService.delete(notification);
    }

}
