package com.hobbySphere.repositories;

import com.hobbySphere.entities.Interests;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interests, Long> {
    boolean existsByNameIgnoreCase(String name);
}
