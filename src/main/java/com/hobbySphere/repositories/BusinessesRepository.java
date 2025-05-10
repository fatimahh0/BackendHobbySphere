package com.hobbySphere.repositories;

import com.hobbySphere.entities.Businesses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessesRepository extends JpaRepository<Businesses, Long> {

    Businesses findByBusinessName(String businessName);

    Businesses findById (long id);

    Optional<Businesses> findByEmail(String email);
}
