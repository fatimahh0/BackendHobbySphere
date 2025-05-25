package com.hobbySphere.controller;

import com.hobbySphere.dto.UserDto;
import com.hobbySphere.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUserDtos());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {

        String password = requestBody.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        boolean deleted = userService.deleteUserByIdWithPassword(id, password);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(403).body("Invalid password or user not found");
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> sendResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean success = userService.resetPassword(email);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "Reset code sent to your email"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No user found with this email"));
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (userService.verifyResetCode(email, code)) {
            return ResponseEntity.ok(Map.of("message", "Code verified successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid code"));
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing email or password"));
        }

        boolean updated = userService.updatePasswordDirectly(email, newPassword);
        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "User not found"));
        }
    }



    

    
}
