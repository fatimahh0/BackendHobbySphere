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
import java.util.Optional;

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
        Businesses business = businessService.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Business not found"));
        return notificationsService.getAllByBusiness(business);
    }


    // ✅ BUSINESS: Count all
    @GetMapping("/business/count")
    public long getBusinessNotificationCount(Principal principal) {
    	Optional<Businesses> optionalBusiness = businessService.findByEmail(principal.getName());
    	if (optionalBusiness.isEmpty()) {
    	    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No business account found for this user");
    	}
    	Businesses business = optionalBusiness.get();

        return notificationsService.getAllByBusiness(business).size();
    }

    // ✅ BUSINESS: Count unread
    @GetMapping("/business/unread-count")
    public int getBusinessUnreadNotificationCount(Principal principal) {
    	Optional<Businesses> optionalBusiness = businessService.findByEmail(principal.getName());
    	if (optionalBusiness.isEmpty()) {
    	    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No business account found for this user");
    	}
    	Businesses business = optionalBusiness.get();

        return notificationsService.getUnreadByBusiness(business).size();
    }


    // ✅ BUSINESS: Mark as read
    @PutMapping("/business/{id}/read")
    public void markBusinessNotificationAsRead(@PathVariable Long id, Principal principal) {
    	Optional<Businesses> optionalBusiness = businessService.findByEmail(principal.getName());
    	if (optionalBusiness.isEmpty()) {
    	    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No business account found for this user");
    	}
    	Businesses business = optionalBusiness.get();

        notificationsService.markAsReadForBusiness(id, business);
    }


    // ✅ BUSINESS: Delete notification
  
    @DeleteMapping("/business/{id}")
    public void deleteBusinessNotification(@PathVariable Long id, Principal principal) {
    	Optional<Businesses> optionalBusiness = businessService.findByEmail(principal.getName());
    	if (optionalBusiness.isEmpty()) {
    	    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No business account found for this user");
    	}
    	Businesses business = optionalBusiness.get();

        Notifications notification = notificationsService.getById(id);

        if (!notification.getBusiness().getId().equals(business.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this notification");
        }

        notificationsService.delete(notification);
    }
    
    // ✅ Helper to get Users object from Businesses
    private Users getUserFromBusiness(Optional<Businesses> business) {
        try {
            return (Users) business.getClass().getMethod("getUser").invoke(business);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user from business");
            }
}
}
