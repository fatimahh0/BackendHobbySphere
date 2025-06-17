package com.hobbySphere.controller;

import com.hobbySphere.dto.UserDto;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.AdminUserService;
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
import java.util.Optional;
import java.util.stream.Collectors;

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
	
	@Autowired
	private AdminUserService adminUserService;

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

            // ✅ DEBUG: print extracted email from token
            String email = jwtUtil.extractUsername(token);
            System.out.println("Extracted email from token: " + email);

            String role = jwtUtil.extractRole(token);

            // SUPER_ADMIN case
            if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
                boolean deleted = userService.deleteUserById(id);
                return deleted
                        ? ResponseEntity.ok("User deleted by SUPER_ADMIN successfully")
                        : ResponseEntity.status(404).body("User not found");
            }

            Users user = usersRepository.findByEmail(email);
            if (user == null || (!user.getId().equals(id) && !"SUPER_ADMIN".equalsIgnoreCase(role))) {
                return ResponseEntity.status(403).body("Access denied: user not found or not authorized");
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
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
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

    @PutMapping("/profile-visibility")
    public ResponseEntity<String> updateProfileVisibility(@RequestParam boolean isPublic,
                                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid token");
            }

            String email = jwtUtil.extractUsername(token.substring(7).trim());
            Users user = usersRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            user.setPublicProfile(isPublic);
            usersRepository.save(user);

            return ResponseEntity.ok("Profile visibility updated to " + (isPublic ? "PUBLIC" : "PRIVATE"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<Users> userOpt = usersRepository.findById(id);
        return userOpt.map(user -> ResponseEntity.ok(new UserDto(user)))
                      .orElse(ResponseEntity.status(404).build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Users> userOpt = usersRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.status(404).body("User not found");

        Users user = userOpt.get();
        user.setStatus(status);
        usersRepository.save(user);
        return ResponseEntity.ok("User status updated to " + status);
    }

    @GetMapping("/{userId}/suggestions")
    public ResponseEntity<?> getFriendSuggestions(@PathVariable Long userId) {
        try {
            List<Users> suggestions = userService.suggestFriendsByInterest(userId);
            List<UserDto> result = suggestions.stream().map(UserDto::new).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching suggestions: " + e.getMessage());
        }
    }


    

    }