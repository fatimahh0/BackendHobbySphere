package com.hobbySphere.repositories;

import com.hobbySphere.entities.PostVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostVisibilityRepository extends JpaRepository<PostVisibility, Long> {
    Optional<PostVisibility> findByName(String name);
}
