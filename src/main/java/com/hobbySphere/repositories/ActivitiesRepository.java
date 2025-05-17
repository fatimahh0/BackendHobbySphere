package com.hobbySphere.repositories;

import com.hobbySphere.entities.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // ✅ Important!
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activities, Long> {

    List<Activities> findByBusinessId(Long businessId);

    @Query("SELECT a.name FROM Activities a " +
           "WHERE a.business.id = :businessId " +
           "ORDER BY SIZE(a.bookings) DESC")
    String findTopActivityByBusinessId(@Param("businessId") Long businessId); // ✅ Use @Param
}
