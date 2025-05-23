package com.hobbySphere.controller;

import com.hobbySphere.dto.UserSummaryDTO;
import com.hobbySphere.services.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "Get all users and admins", description = "Fetch all users with their name, email, role, and status")
    @GetMapping
    public List<UserSummaryDTO> getAllUsersAndAdmins() {
        return adminUserService.getAllUserSummaries();
    }
}
