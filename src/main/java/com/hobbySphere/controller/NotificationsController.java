package com.hobbySphere.controller;

import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.NotificationsService;
import com.hobbySphere.services.UserService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.hobbySphere.services.BusinessService;

import com.hobbySphere.entities.Businesses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    
    @Autowired
    private JwtUtil jwtUtil;
    
    // ✅ Get all user notifications
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@GetMapping
    	public ResponseEntity<List<Notifications>> getUserNotifications(
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    // Token validation logic (example, adjust according to your JwtUtil)
    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    // Assuming jwtUtil.isUserToken(token) validates user token
    	    if (!jwtUtil.isUserToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original code unchanged
    	    Users user = usersService.getUserByEmaill(principal.getName());
    	    List<Notifications> notifications = notificationsService.getAllByUser1(user);
    	    return ResponseEntity.ok(notifications);
    	}

    // ✅ Mark notification as read for user
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@PutMapping("/{id}/read")
    	public ResponseEntity<Void> markAsRead(
    	        @PathVariable Long id,
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    if (!jwtUtil.isUserToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original code unchanged
    	    Users user = usersService.getUserByEmaill(principal.getName());
    	    notificationsService.markAsRead(id, user);

    	    return ResponseEntity.ok().build();
    	}


    // ✅ Count all user notifications
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@GetMapping("/count")
    	public ResponseEntity<Long> getNotificationCount(
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    if (!jwtUtil.isUserToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original code unchanged
    	    Users user = usersService.getUserByEmaill(principal.getName());
    	    long count = notificationsService.getAllByUser1(user).size();

    	    return ResponseEntity.ok(count);
    	}


    // ✅ Count unread user notifications
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@GetMapping("/unread-count")
    	public ResponseEntity<Integer> getUnreadNotificationCount(
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    if (!jwtUtil.isUserToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original code unchanged
    	    Users user = usersService.getUserByEmaill(principal.getName());
    	    int count = notificationsService.getUnreadByUser(user).size();

    	    return ResponseEntity.ok(count);
    	}


    // ✅ Delete user notification
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@DeleteMapping("/{id}")
    	public ResponseEntity<Void> deleteNotification(
    	        @PathVariable Long id,
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    if (!jwtUtil.isUserToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original logic unchanged
    	    Users user = usersService.getUserByEmaill(principal.getName());
    	    Notifications notification = notificationsService.getById(id);

    	    if (!notification.getUser().getId().equals(user.getId())) {
    	        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    	    }

    	    notificationsService.delete(notification);
    	    return ResponseEntity.ok().build();
    	}

    // ✅ BUSINESS: Get notifications
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@GetMapping("/business")
    	public ResponseEntity<List<Notifications>> getBusinessNotifications(
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    // Check if token is a valid business token
    	    if (!jwtUtil.isBusinessToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original logic unchanged
    	    Businesses business = businessService.findByEmail(principal.getName())
    	        .orElseThrow(() -> new RuntimeException("Business not found"));

    	    List<Notifications> notifications = notificationsService.getAllByBusiness(business);

    	    return ResponseEntity.ok(notifications);
    	}



    // ✅ BUSINESS: Count all
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@GetMapping("/business/count")
    	public ResponseEntity<Long> getBusinessNotificationCount(
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    // Validate business token
    	    if (!jwtUtil.isBusinessToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original logic unchanged
    	    Optional<Businesses> optionalBusiness = businessService.findByEmail(principal.getName());
    	    if (optionalBusiness.isEmpty()) {
    	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No business account found for this user");
    	    }
    	    Businesses business = optionalBusiness.get();

    	    long count = notificationsService.getAllByBusiness(business).size();
    	    return ResponseEntity.ok(count);
    	}


    // ✅ BUSINESS: Count unread
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@GetMapping("/business/unread-count")
    	public ResponseEntity<Integer> getBusinessUnreadNotificationCount(
    	        @RequestHeader("Authorization") String authHeader,
    	        Principal principal) {

    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);

    	    // Validate business token
    	    if (!jwtUtil.isBusinessToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original logic unchanged
    	    Optional<Businesses> optionalBusiness = businessService.findByEmail(principal.getName());
    	    if (optionalBusiness.isEmpty()) {
    	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No business account found for this user");
    	    }
    	    Businesses business = optionalBusiness.get();

    	    int unreadCount = notificationsService.getUnreadByBusiness(business).size();
    	    return ResponseEntity.ok(unreadCount);
    	}



    // ✅ BUSINESS: Mark as read
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@PutMapping("/business/{id}/read")
    	public ResponseEntity<Void> markBusinessNotificationAsRead(
    	        @PathVariable Long id,
    	        Principal principal,
    	        @RequestHeader("Authorization") String authHeader) {

    	    // Token validation
    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);
    	    if (!jwtUtil.isBusinessToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original code unchanged
    	    Optional<Businesses> optionalBusiness = businessService.findByEmail(principal.getName());
    	    if (optionalBusiness.isEmpty()) {
    	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No business account found for this user");
    	    }
    	    Businesses business = optionalBusiness.get();

    	    notificationsService.markAsReadForBusiness(id, business);

    	    return ResponseEntity.ok().build();
    	}



    // ✅ BUSINESS: Delete notification
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    	@DeleteMapping("/business/{id}")
    	public ResponseEntity<Void> deleteBusinessNotification(
    	        @PathVariable Long id,
    	        Principal principal,
    	        @RequestHeader("Authorization") String authHeader) {

    	    // Validate Bearer token
    	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    String token = authHeader.substring(7);
    	    if (!jwtUtil.isBusinessToken(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }

    	    // Original code unchanged
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

    	    return ResponseEntity.ok().build();
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
