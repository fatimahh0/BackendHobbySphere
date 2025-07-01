package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessStatus; // ✅ updated import
import com.hobbySphere.entities.Businesses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessesRepository extends JpaRepository<Businesses, Long> {

    Businesses findByBusinessName(String businessName);

    Businesses findById(long id);

    Optional<Businesses> findByEmail(String email);

    Optional<Businesses> findByPhoneNumber(String phoneNumber);

    // ✅ Use BusinessStatus entity as foreign key
    List<Businesses> findByIsPublicProfileTrueAndStatus(BusinessStatus status);
}
