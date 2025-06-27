package com.hobbySphere.controller;

import com.hobbySphere.dto.*;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.entities.Users;
import com.hobbySphere.enums.BusinessStatus;
import com.hobbySphere.enums.UserStatus;
import com.hobbySphere.services.AdminActivityService;
import com.hobbySphere.services.AdminStatsService;
import com.hobbySphere.services.AdminUserService;
import com.hobbySphere.repositories.*;
import com.hobbySphere.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Autowired private AdminStatsService statsService;
    @Autowired private AdminUserService adminUserService;
    @Autowired private AdminActivityService adminActivityService;
    @Autowired private UsersRepository usersRepository;
    @Autowired private AdminUsersRepository adminUsersRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired
    private com.hobbySphere.services.BusinessService businessService;

    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private boolean isSuperAdmin(String token) {
        try {
            token = token.substring(7).trim();
            String role = jwtUtil.extractRole(token);
            return "SUPER_ADMIN".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system stats", description = "Returns total users, activities, bookings, and feedback for the selected period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stats retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getStats(@RequestParam(defaultValue = "today") String period,
                                      @RequestHeader("Authorization") String token) {
        try {
            token = token.substring(7).trim(); // Remove "Bearer "
            String role = jwtUtil.extractRole(token);

            if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            return ResponseEntity.ok(statsService.getStats(period));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
 
    @GetMapping("/registrations/monthly")
    @Operation(summary = "Get monthly user registration counts", description = "Returns user registration counts per month for the last 6 months")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Monthly registrations retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getMonthlyUserRegistrations(@RequestHeader("Authorization") String token) {
        try {
            token = token.substring(7).trim(); // Remove "Bearer " prefix
            String role = jwtUtil.extractRole(token);

            if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Map<String, Long> registrations = statsService.getMonthlyRegistrations();
            return ResponseEntity.ok(registrations);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
    }

    @GetMapping("/activities/popular")
    @Operation(summary = "Get popular activities", description = "Returns most booked or viewed activities and their popularity metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popular activities retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getPopularActivities(@RequestHeader("Authorization") String token) {
        try {
            token = token.substring(7).trim(); // Remove "Bearer " prefix
            String role = jwtUtil.extractRole(token);

            if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            List<Map<String, Object>> activities = statsService.getPopularActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
    }


    @GetMapping("/activities")
    @Operation(summary = "Get all activities posted by businesses", description = "Returns title, business name, date, participants, and description for all activities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activities retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getAllActivities(@RequestHeader("Authorization") String token) {
        try {
            token = token.substring(7).trim(); // Remove "Bearer " prefix
            String role = jwtUtil.extractRole(token);

            if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            List<AdminActivityDTO> activities = adminActivityService.getAllActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
    }


    @Operation(summary = "Toggle user status", description = "Toggle a user’s status between ACTIVE and INACTIVE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status toggled successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{userId}/toggle-status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long userId,
                                                   @RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<Users> optionalUser = usersRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Users user = optionalUser.get();
        UserStatus currentStatus = user.getStatus();

        // Toggle between ACTIVE and INACTIVE
        if (currentStatus == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }

        usersRepository.save(user);
        return ResponseEntity.ok("User status updated to: " + user.getStatus().name());
    }


    @Operation(summary = "Delete user", description = "Permanently delete a user account by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId,
                                             @RequestHeader("Authorization") String token) {
        try {
            token = token.substring(7).trim(); // remove "Bearer " prefix
            String role = jwtUtil.extractRole(token);

            if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            Optional<Users> optionalUser = usersRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            adminUserService.deleteUserAndDependencies(userId); // ✅ cascades other related deletions
            return ResponseEntity.ok("User and all related data deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete user: " + e.getMessage());
        }
    }

    @Operation(summary = "Update admin profile", description = "Update admin profile information (first name, last name, username, email)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Admin profile updated successfully"),
        @ApiResponse(responseCode = "404", description = "Admin user not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateAdminProfile(@RequestBody AdminProfileUpdateDTO dto,
                                                @RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) return ResponseEntity.status(401).body("Unauthorized");

        token = token.substring(7).trim(); // Remove "Bearer "
        Long currentAdminId = jwtUtil.extractId(token);
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

    @Operation(summary = "Update admin password", description = "Change the password of a SUPER_ADMIN after verifying the current password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "403", description = "Current password incorrect"),
        @ApiResponse(responseCode = "404", description = "Admin user not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/password")
    public ResponseEntity<String> updateAdminPassword(@RequestBody AdminPasswordUpdateDTO dto,
                                                      @RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) return ResponseEntity.status(401).body("Unauthorized");

        token = token.substring(7).trim();
        Long currentAdminId = jwtUtil.extractId(token);

        Optional<AdminUsers> optionalAdmin = adminUsersRepository.findById(currentAdminId);
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

    @Operation(summary = "Update notification preferences", description = "Update admin notification settings for activity and feedback alerts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
        @ApiResponse(responseCode = "404", description = "Admin user not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/notifications")
    public ResponseEntity<String> updateNotificationPreferences(@RequestBody AdminNotificationPreferencesDTO dto,
                                                                @RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) return ResponseEntity.status(401).body("Unauthorized");

        token = token.substring(7).trim();
        Long currentAdminId = jwtUtil.extractId(token);

        AdminUsers admin = adminUsersRepository.findById(currentAdminId).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(404).body("Admin user not found.");
        }

        admin.setNotifyActivityUpdates(dto.isNotifyActivityUpdates());
        admin.setNotifyUserFeedback(dto.isNotifyUserFeedback());
        adminUsersRepository.save(admin);

        return ResponseEntity.ok("Notification preferences updated successfully.");
    }


    @Operation(summary = "Get all feedback", description = "Returns submitter name, content, rating, and date for all feedback")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feedback retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/feedback")
    public ResponseEntity<?> getAllFeedback(@RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(reviewRepository.findAll());
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current admin profile", description = "Returns profile details of the currently logged-in SUPER_ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Admin not found")
    })
    public ResponseEntity<?> getCurrentAdminProfile(@RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        token = token.substring(7).trim(); // Remove "Bearer "
        Long currentAdminId = jwtUtil.extractId(token);
        Optional<AdminUsers> optionalAdmin = adminUsersRepository.findById(currentAdminId);

        if (optionalAdmin.isEmpty()) {
            return ResponseEntity.status(404).body("Admin not found.");
        }

        AdminUsers admin = optionalAdmin.get();

        return ResponseEntity.ok(Map.of(
                "id", admin.getAdminId(),
                "firstName", admin.getFirstName(),
                "lastName", admin.getLastName(),
                "username", admin.getUsername(),
                "email", admin.getEmail(),
                "notifyActivityUpdates", admin.getNotifyActivityUpdates(),
                "notifyUserFeedback", admin.getNotifyUserFeedback()
        ));
    }
    
    @Operation(summary = "Delete a business and all related data", description = "Only SUPER_ADMIN can delete a business account along with all related activities, bookings, reviews, and admin links.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Business deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Business not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/businesses/{businessId}")
    public ResponseEntity<String> deleteBusinessBySuperAdmin(@PathVariable Long businessId,
                                                             @RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) return ResponseEntity.status(401).body("Unauthorized");

        try {
            businessService.delete(businessId); // ✅ this handles everything: activities, bookings, reviews, links
            return ResponseEntity.ok("Business and all related data deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Business not found.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete business: " + e.getMessage());
        }
    }

    @PutMapping("/businesses/{businessId}/disable")
    public ResponseEntity<?> disableBusiness(@PathVariable Long businessId,
                                             @RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) return ResponseEntity.status(401).body("Unauthorized");

        try {
            Businesses business = businessService.findById(businessId);
            business.setStatus(BusinessStatus.INACTIVE);
            businessService.save(business);
            return ResponseEntity.ok("Business marked as INACTIVE due to low rating.");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Business not found.");
        }
    }

    @GetMapping("/businesses/low-rated")
    @Operation(summary = "Get businesses with average rating ≤ 3", description = "Returns list of low-rated businesses for admin review")
    public ResponseEntity<?> getLowRatedBusinesses(@RequestHeader("Authorization") String token) {
        if (!isSuperAdmin(token)) return ResponseEntity.status(401).body("Unauthorized");

        return ResponseEntity.ok(businessService.getLowRatedBusinesses());
    }


}
