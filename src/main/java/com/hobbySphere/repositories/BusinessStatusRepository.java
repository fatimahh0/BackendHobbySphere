package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessStatusRepository extends JpaRepository<BusinessStatus, Long> {
    Optional<BusinessStatus> findByName(String name);
    Optional<BusinessStatus> findByNameIgnoreCase(String name);
}
