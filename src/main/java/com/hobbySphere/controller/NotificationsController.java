package com.hobbySphere.controller;

import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.NotificationsService;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationsService notificationsService;
    private final UserService usersService;

    public NotificationsController(NotificationsService notificationsService, UserService usersService) {
        this.notificationsService = notificationsService;
        this.usersService = usersService;
    }

    @GetMapping
    public List<Notifications> getNotifications(Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        return notificationsService.getAllByUser1(user);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, Principal principal) {
        Users user = usersService.getUserByEmaill(principal.getName());
        notificationsService.markAsRead(id, user);
    }
}
