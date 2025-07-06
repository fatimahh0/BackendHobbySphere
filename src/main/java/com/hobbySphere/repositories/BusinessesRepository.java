package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessStatus; // ✅ updated import
import com.hobbySphere.entities.Businesses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessesRepository extends JpaRepository<Businesses, Long> {

    Businesses findByBusinessName(String businessName);

    Businesses findById(long id);

    @Query("SELECT b FROM Businesses b WHERE b.email = :email AND b.email IS NOT NULL")
    Optional<Businesses> findByEmail(@Param("email") String email);

    @Query("SELECT b FROM Businesses b WHERE b.phoneNumber = :phoneNumber AND b.phoneNumber IS NOT NULL")
    Optional<Businesses> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);


    // ✅ Use BusinessStatus entity as foreign key
    List<Businesses> findByIsPublicProfileTrueAndStatus(BusinessStatus status);
}
