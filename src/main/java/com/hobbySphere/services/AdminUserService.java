package com.hobbySphere.services;

import com.hobbySphere.dto.UserSummaryDTO;
import com.hobbySphere.entities.*;
import com.hobbySphere.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private AdminUsersRepository adminUserRepository;

    @Autowired
    private BusinessAdminsRepository businessAdminsRepository;

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsersRepository usersRepository;

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
                managerRole);

        return adminUserRepository.save(manager);
    }

    public AdminUsers promoteUserToManager(Users user, Businesses business) {
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseThrow(() -> new RuntimeException("Role MANAGER not found"));

        AdminUsers manager = new AdminUsers();
        manager.setUsername(user.getUsername());
        manager.setFirstName(user.getFirstName());
        manager.setLastName(user.getLastName());
        manager.setEmail(user.getEmail());
        manager.setPasswordHash(user.getPasswordHash());
        manager.setRole(managerRole);
        manager.setBusiness(business);

        AdminUsers savedManager = adminUserRepository.save(manager);

        BusinessAdmins businessAdmins = new BusinessAdmins(business, savedManager);
        businessAdminsRepository.save(businessAdmins);

        return savedManager;
    }

    public List<UserSummaryDTO> getAllUserSummaries() {
        List<UserSummaryDTO> result = new ArrayList<>();

        List<UserSummaryDTO> users = usersRepository.findAll().stream()
                .map(u -> new UserSummaryDTO(
                        u.getId(),
                        u.getFirstName() + " " + u.getLastName(),
                        u.getEmail(),
                        "USER"))
                .collect(Collectors.toList());

        List<UserSummaryDTO> admins = adminUserRepository.findAll().stream()
                .map(a -> new UserSummaryDTO(
                        a.getAdminId(),
                        a.getFirstName() + " " + a.getLastName(),
                        a.getEmail(),
                        a.getRole().getName()))
                .collect(Collectors.toList());

        result.addAll(users);
        result.addAll(admins);

        return result;
    }

    @Transactional
    public void deleteUserAndDependencies(Long userId) {
        reviewRepository.deleteByCustomer_Id(userId); // ✅ use userId here
        activityBookingsRepository.deleteByUserId(userId);
        usersRepository.deleteById(userId);
    }

    @Transactional
    public void deleteManagerById(Long adminId) {
        // Step 1: Delete the BusinessAdmins relationship
        businessAdminsRepository.findByAdmin_AdminId(adminId)
                .ifPresent(businessAdminsRepository::delete);

        // Step 2: Delete the AdminUsers entry
        adminUserRepository.findById(adminId)
                .ifPresent(adminUserRepository::delete);
    }

    public List<UserSummaryDTO> getUsersByRole(String role) {
        List<UserSummaryDTO> result = new ArrayList<>();

        if ("USER".equalsIgnoreCase(role)) {
            result = usersRepository.findAll().stream()
                    .map(u -> new UserSummaryDTO(
                            u.getId(),
                            u.getFirstName() + " " + u.getLastName(),
                            u.getEmail(),
                            "USER"))
                    .collect(Collectors.toList());
        } else {
            result = adminUserRepository.findAll().stream()
                    .filter(a -> a.getRole().getName().equalsIgnoreCase(role))
                    .map(a -> new UserSummaryDTO(
                            a.getAdminId(),
                            a.getFirstName() + " " + a.getLastName(),
                            a.getEmail(),
                            a.getRole().getName()))
                    .collect(Collectors.toList());
        }

        return result;
    }

    public boolean isUserAlreadyManager(Users user, Businesses business) {
        List<AdminUsers> results = adminUserRepository.findByEmailAndBusiness(user.getEmail(), business);
        return !results.isEmpty();
}

    public void deleteManagerByEmail(String email) {
        Optional<AdminUsers> admin = adminUserRepository.findByEmail(email);
        admin.ifPresent(a -> adminUserRepository.deleteById(a.getAdminId()));
    }

    public Optional<AdminUsers> findByUserEmail(String email) {
        return adminUserRepository.findByEmail(email); // email-based lookup
    }

    public List<AdminUsers> findAllByUserEmail(String email) {
        return adminUserRepository.findAllByEmail(email);
    }


	

}
