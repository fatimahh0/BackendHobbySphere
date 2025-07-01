package com.hobbySphere.repositories;

import com.hobbySphere.entities.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    Optional<UserStatus> findByName(String name);
    Optional<UserStatus> findByNameIgnoreCase(String name); 
    boolean existsByName(String name);
}
