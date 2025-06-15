package com.hobbySphere.controller;

import com.hobbySphere.dto.UserSummaryDTO;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Users;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.AdminUserService;
import com.hobbySphere.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Get all users and admins", description = "Fetch all users with their name, email, role, and status")
    @GetMapping
    public List<UserSummaryDTO> getAllUsersAndAdmins() {
        return adminUserService.getAllUserSummaries();
    }

    @Operation(summary = "Get users by role", description = "Fetch all users or admins by specified role")
    @GetMapping("/by-role")
    public List<UserSummaryDTO> getUsersByRole(@RequestParam String role) {
        return adminUserService.getUsersByRole(role);
    }
}
