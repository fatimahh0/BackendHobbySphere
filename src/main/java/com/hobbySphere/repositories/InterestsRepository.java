package com.hobbySphere.repositories;

import com.hobbySphere.entities.Interests;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestsRepository extends JpaRepository<Interests, Long> {

	Optional<Interests> findByName(String name);

	boolean existsByNameIgnoreCase(String name);

   
}
