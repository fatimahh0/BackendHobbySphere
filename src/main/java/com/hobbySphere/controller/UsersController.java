package com.hobbySphere.controller;

import com.hobbySphere.dto.UserDto;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.hobbySphere.repositories.*;
import com.hobbySphere.security.JwtUtil;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
public class UsersController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UsersRepository usersRepository;

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid token");
            }

            token = token.substring(7).trim();

            // Allow if token is for business
            if (jwtUtil.isBusinessToken(token)) {
                return ResponseEntity.ok(userService.getAllUserDtos());
            }

            // Allow if it's admin with SUPER_ADMIN role
            String role = jwtUtil.extractRole(token);
            if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.ok(userService.getAllUserDtos());
            }
            

            // Allow if it's a regular user (i.e. no role means it's a user token)
            if (role == null || "USER".equalsIgnoreCase(role)) {
                return ResponseEntity.ok(userService.getAllUserDtos());
            }

            return ResponseEntity.status(403).body("Access denied");

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid token");
            }

            token = token.substring(7).trim();
            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token); // Optional

            Users user = usersRepository.findByEmail(email);
            if (user == null || (!user.getId().equals(id) && !"SUPER_ADMIN".equalsIgnoreCase(role))) {
                return ResponseEntity.status(403).body("Access denied");
            }

            String password = requestBody.get("password");
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }

            boolean deleted = userService.deleteUserByIdWithPassword(id, password);
            return deleted
                    ? ResponseEntity.ok("User deleted successfully")
                    : ResponseEntity.status(403).body("Invalid password or user not found");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> sendResetCode(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            Users user = usersRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No user found with this email"));
            }

            boolean success = userService.resetPassword(email);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Reset code sent to your email"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to send reset code"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected error"));
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");

            if (userService.verifyResetCode(email, code)) {
                return ResponseEntity.ok(Map.of("message", "Code verified successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid code"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected error"));
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");

            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "New password is required"));
            }

            boolean updated = userService.updatePasswordDirectly(email, newPassword);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "User not found"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected error"));
        }
    }
    
    
    @PostMapping("/{userId}/interests")
    public ResponseEntity<?> addUserInterests(
            @PathVariable Long userId,
            @RequestBody List<Long> interestIds
    ) {
        userService.addUserInterests(userId, interestIds);
        return ResponseEntity.ok(Map.of("message", "User interests added successfully"));
    }
    
    @DeleteMapping("/delete-profile-image/{id}")
    public ResponseEntity<?> deleteProfileImage(@PathVariable Long id) {
        boolean success = userService.deleteUserProfileImage(id);
        if (success) {
            return ResponseEntity.ok("Profile image deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No profile image found or already deleted");
        }
    }


    }