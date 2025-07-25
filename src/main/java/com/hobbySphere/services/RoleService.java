package com.hobbySphere.services;

import com.hobbySphere.entities.Role;
import com.hobbySphere.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;  // Correct import for newer versions of Jakarta

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
    	
        createRoleIfNotExists("SUPER_ADMIN");
        createRoleIfNotExists("MANAGER");
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.findByName(roleName).isPresent()) {  // Use isPresent() instead of isEmpty()
            Role newRole = new Role();
            newRole.setName(roleName);
            roleRepository.save(newRole);
        }
    }
}
