package com.hobbySphere.services;

import com.hobbySphere.entities.AdminUsers;

import com.hobbySphere.entities.Role;
import com.hobbySphere.repositories.AdminUsersRepository;
import com.hobbySphere.repositories.RoleRepository;
import com.hobbySphere.entities.Users;
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
    
    public Optional<AdminUsers> findById(Long id) {
        return adminUserRepository.findById(id);
    }


    public void save(AdminUsers adminUser) {
        adminUserRepository.save(adminUser);
    }

    public AdminUsers createAdminUser(String username, String firstName, String lastName,
            String email, String plainPassword, String roleName) {


Role role = roleRepository.findByName(roleName.toUpperCase())
.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

Optional<AdminUsers> existingAdmin = adminUserRepository.findByEmail(email);
if (existingAdmin.isPresent()) {
    throw new RuntimeException("Admin user with email " + email + " already exists");
}

String encodedPassword = passwordEncoder.encode(plainPassword);


AdminUsers admin = new AdminUsers(username, firstName, lastName, email, encodedPassword, role);

return adminUserRepository.save(admin);


}
    
    public Optional<AdminUsers> findByUsernameOrEmail(String input) {
        return adminUserRepository.findByUsernameOrEmail(input, input);
    }

    public AdminUsers promoteUserToManager(Users user) {
        Role managerRole = roleRepository.findByName("MANAGER")
            .orElseThrow(() -> new RuntimeException("Manager role not found"));

        Optional<AdminUsers> existing = adminUserRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new RuntimeException("User already promoted to admin");
        }

        AdminUsers manager = new AdminUsers(
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPasswordHash(), // assuming hash is valid
            managerRole
        );

        return adminUserRepository.save(manager);
    }

}
