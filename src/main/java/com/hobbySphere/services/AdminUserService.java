package com.hobbySphere.services;

import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Role;
import com.hobbySphere.repositories.AdminUsersRepository;
import com.hobbySphere.repositories.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;


@Service
public class AdminUserService {

    @Autowired
    private AdminUsersRepository adminUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Existing methods
    public Optional<AdminUsers> findByEmail(String email) {
        return adminUserRepository.findByEmail(email);
    }

    public Optional<AdminUsers> findByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    public void save(AdminUsers adminUser) {
        adminUserRepository.save(adminUser);
    }

    // ✅ NEW: Create Admin with role and encoded password
    public AdminUsers createAdminUser(String username, String firstName, String lastName,
            String email, String plainPassword, String roleName) {

// Check if role exists
Role role = roleRepository.findByName(roleName.toUpperCase())
.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

// Check if admin user exists
AdminUsers existingAdmin = adminUserRepository.findByEmail(email)
.orElseThrow(() -> new RuntimeException("Admin user with email " + email + " already exists"));

// Encode password
String encodedPassword = passwordEncoder.encode(plainPassword);

// Create AdminUsers object
AdminUsers admin = new AdminUsers(username, firstName, lastName, email, encodedPassword, role);

// Save to DB
return adminUserRepository.save(admin);
}

}
