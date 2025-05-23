package com.hobbySphere.repositories;

import com.hobbySphere.entities.AdminUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUsersRepository extends JpaRepository<AdminUsers, Long> {

    
    Optional<AdminUsers> findByUsername(String username);
    
    Optional<AdminUsers> findByEmail(String email);
    
    Optional<AdminUsers> findByUsernameOrEmail(String username, String email);
    
}
