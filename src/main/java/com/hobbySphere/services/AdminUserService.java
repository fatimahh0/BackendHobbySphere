package com.hobbySphere.services;

import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.repositories.AdminUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminUserService {

    @Autowired
    private AdminUsersRepository adminUserRepository;

    public Optional<AdminUsers> findByEmail(String email) {
        return adminUserRepository.findByEmail(email);
    }

    public Optional<AdminUsers> findByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    public void save(AdminUsers adminUser) {
        adminUserRepository.save(adminUser);
    }
}
