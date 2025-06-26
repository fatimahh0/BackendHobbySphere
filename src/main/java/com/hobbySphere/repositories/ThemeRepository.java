package com.hobbySphere.repositories;

import com.hobbySphere.entities.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(String name);
    Optional<Theme> findById(Long id);
    // You can add more if you add fields to Theme, like findByName, etc.
}