package com.hobbySphere.controller;

import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.NotificationsService;
import com.hobbySphere.services.UserService;
import com.hobbySphere.services.BusinessService;

import com.hobbySphere.entities.Businesses;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationsService notificationsService;
    private final UserService usersService;
    private final BusinessService businessService;

    public NotificationsController(
        NotificationsService notificationsService,
        UserService usersService,
        BusinessService businessService
    ) {
        this.notificationsService = notificationsService;
        this.usersService = usersService;
        this.businessService = businessService;
    }

    // ✅ Get all user notifications
    @GetMapping
    public List<Notifications> getUserNotifications(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return notificationsService.getAllByUser1(user);
    }

    // ✅ Mark notification as read for user
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        notificationsService.markAsRead(id, user);
    }

    // ✅ Count all user notifications
    @GetMapping("/count")
    public long getNotificationCount(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return notificationsService.getAllByUser1(user).size();
    }

    // ✅ Count unread user notifications
    @GetMapping("/unread-count")
    public int getUnreadNotificationCount(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return notificationsService.getUnreadByUser(user).size();
    }

    // ✅ Delete user notification
    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        Notifications notification = notificationsService.getById(id);

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this notification");
        }

        notificationsService.delete(notification);
    }

    // ✅ BUSINESS: Get notifications
    @GetMapping("/business")
    public List<Notifications> getBusinessNotifications(Principal principal) {
        Businesses business = businessService.findByEmailOrThrow(principal.getName());
        return notificationsService.getAllByUser1(getUserFromBusiness(business));
    }

    // ✅ BUSINESS: Count all
    @GetMapping("/business/count")
    public long getBusinessNotificationCount(Principal principal) {
        Businesses business = businessService.findByEmailOrThrow(principal.getName());
        return notificationsService.getAllByUser1(getUserFromBusiness(business)).size();
    }

    // ✅ BUSINESS: Count unread
    @GetMapping("/business/unread-count")
    public int getBusinessUnreadNotificationCount(Principal principal) {
        Businesses business = businessService.findByEmailOrThrow(principal.getName());
        return notificationsService.getUnreadByUser(getUserFromBusiness(business)).size();
    }

    // ✅ BUSINESS: Mark as read
    @PutMapping("/business/{id}/read")
    public void markBusinessNotificationAsRead(@PathVariable Long id, Principal principal) {
        Businesses business = businessService.findByEmailOrThrow(principal.getName());
        notificationsService.markAsRead(id, getUserFromBusiness(business));
    }

    // ✅ BUSINESS: Delete notification
    @DeleteMapping("/business/{id}")
    public void deleteBusinessNotification(@PathVariable Long id, Principal principal) {
        Businesses business = businessService.findByEmailOrThrow(principal.getName());
        Notifications notification = notificationsService.getById(id);
        Users businessUser = getUserFromBusiness(business);

        if (!notification.getUser().getId().equals(businessUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this notification");
        }

        notificationsService.delete(notification);
    }

    // ✅ Helper to get Users object from Businesses
    private Users getUserFromBusiness(Businesses business) {
        try {
            return (Users) business.getClass().getMethod("getUser").invoke(business);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user from business");
        }
    }
}
