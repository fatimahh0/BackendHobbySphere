package com.hobbySphere.repositories;

import com.hobbySphere.entities.ThemeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ThemeAssignmentRepository extends JpaRepository<ThemeAssignment, Long> {
    Optional<ThemeAssignment> findByBusiness_Id(Long businessId);
    Optional<ThemeAssignment> findByUser_Id(Long userId);
}