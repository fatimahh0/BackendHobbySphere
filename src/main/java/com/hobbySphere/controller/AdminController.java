package com.hobbySphere.controller;

import com.hobbySphere.dto.*;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.AdminActivityService;
import com.hobbySphere.services.AdminStatsService;
import com.hobbySphere.services.AdminUserService;
import com.hobbySphere.repositories.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/superadmin")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
@Tag(name = "Admin Dashboard", description = "Admin-level statistics and monitoring")
public class AdminController {

    @Autowired
    private AdminStatsService statsService;

    @Autowired
    private AdminUserService adminUserService; // ✅ from File1

    @Autowired
    private AdminActivityService adminActivityService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AdminUsersRepository adminUsersRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Operation(summary = "Get system stats", description = "Returns total users, activities, bookings, and feedback for the selected period")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(@RequestParam(defaultValue = "today") String period) {
        return ResponseEntity.ok(statsService.getStats(period));
    }

    @Operation(summary = "Get monthly user registration counts", description = "Returns user registration counts per month for the last 6 months")
    @GetMapping("/registrations/monthly")
    public ResponseEntity<?> getMonthlyUserRegistrations() {
        Map<String, Long> registrations = statsService.getMonthlyRegistrations();
        return ResponseEntity.ok(registrations);
    }

    @Operation(summary = "Get popular activities", description = "Returns most booked or viewed activities and their popularity metrics")
    @GetMapping("/activities/popular")
    public ResponseEntity<?> getPopularActivities() {
        List<Map<String, Object>> activities = statsService.getPopularActivities();
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Get all activities posted by businesses", description = "Returns title, business name, date, participants, and description for all activities")
    @GetMapping("/activities")
    public ResponseEntity<?> getAllActivities() {
        List<AdminActivityDTO> activities = adminActivityService.getAllActivities();
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Toggle user status", description = "Toggle a user’s status between Active and Disabled")
    @PutMapping("/{userId}/toggle-status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long userId) {
        Optional<Users> optionalUser = usersRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Users user = optionalUser.get();
        String currentStatus = user.getStatus();
        user.setStatus("Active".equalsIgnoreCase(currentStatus) ? "Disabled" : "Active");
        usersRepository.save(user);
        return ResponseEntity.ok("User status updated to: " + user.getStatus());
    }

    @Operation(summary = "Delete user", description = "Permanently delete a user account by ID")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        Optional<Users> optionalUser = usersRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            adminUserService.deleteUserAndDependencies(userId); // ✅ cascade delete
            return ResponseEntity.ok("User and all related data deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete user: " + e.getMessage());
        }
    }

    @Operation(summary = "Update admin profile", description = "Update admin profile information (first name, last name, username, email)")
    @PutMapping("/profile")
    public ResponseEntity<?> updateAdminProfile(@RequestBody AdminProfileUpdateDTO dto) {
        Long currentAdminId = 1L; // Replace with dynamic logic
        AdminUsers admin = adminUsersRepository.findById(currentAdminId).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(404).body("Admin user not found.");
        }
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setUsername(dto.getUsername());
        admin.setEmail(dto.getEmail());
        adminUsersRepository.save(admin);
        return ResponseEntity.ok("Admin profile updated successfully.");
    }

    @PutMapping("/password")
    public ResponseEntity<String> updateAdminPassword(@RequestBody AdminPasswordUpdateDTO dto) {
        Optional<AdminUsers> optionalAdmin = adminUsersRepository.findByUsername(dto.getUsername());
        if (optionalAdmin.isEmpty()) {
            return ResponseEntity.status(404).body("Admin user not found.");
        }

        AdminUsers admin = optionalAdmin.get();
        if (!passwordEncoder.matches(dto.getCurrentPassword(), admin.getPasswordHash())) {
            return ResponseEntity.status(403).body("Current password is incorrect.");
        }

        admin.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        adminUsersRepository.save(admin);
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PutMapping("/notifications")
    public ResponseEntity<String> updateNotificationPreferences(@RequestBody AdminNotificationPreferencesDTO dto) {
        AdminUsers admin = adminUsersRepository.findByUsername(dto.getUsername()).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(404).body("Admin user not found.");
        }

        admin.setNotifyActivityUpdates(dto.isNotifyActivityUpdates());
        admin.setNotifyUserFeedback(dto.isNotifyUserFeedback());
        adminUsersRepository.save(admin);

        return ResponseEntity.ok("Notification preferences updated successfully.");
    }

    @Operation(summary = "Get all feedback", description = "Returns submitter name, content, rating, and date for all feedback")
    @GetMapping("/feedback")
    public ResponseEntity<?> getAllFeedback() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }
}
