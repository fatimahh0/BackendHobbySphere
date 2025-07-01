package com.hobbySphere.repositories;

import com.hobbySphere.entities.FeedType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedTypeRepository extends JpaRepository<FeedType, Long> {
    Optional<FeedType> findByName(String name);
    boolean existsByName(String name);
}
