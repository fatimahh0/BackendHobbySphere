package com.hobbySphere.repositories;

import com.hobbySphere.entities.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(String name);

    Optional<Theme> findById(Long id);

    Optional<Theme> findByIsActiveTrue();

    @Modifying
    @Transactional
    @Query("UPDATE Theme t SET t.isActive = false WHERE t.isActive = true")
    void deactivateAllThemes();

}