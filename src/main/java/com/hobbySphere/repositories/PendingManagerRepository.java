package com.hobbySphere.repositories;

import com.hobbySphere.entities.PendingManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingManagerRepository extends JpaRepository<PendingManager, Long> {

    Optional<PendingManager> findByToken(String token);

    Optional<PendingManager> findByEmail(String email);
}
