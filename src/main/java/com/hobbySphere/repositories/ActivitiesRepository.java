package com.hobbySphere.repositories;

import com.hobbySphere.entities.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activities, Long> {

    List<Activities> findByBusinessId(Long businessId);

    // ✅ Utilise une native query pour obtenir l’activité la plus réservée
    @Query(value = "SELECT a.activity_name FROM activity_bookings b " +
            "JOIN activities a ON b.activity_id = a.activity_id " +
            "WHERE a.business_id = :businessId " +
            "GROUP BY a.activity_name " +
            "ORDER BY COUNT(b.booking_id) DESC LIMIT 1", nativeQuery = true)
String findTopActivityNameByBusinessId(@Param("businessId") Long businessId);



    // ✅ Supprimer par l'ID standard (hérité de JpaRepository mais ok de le préciser)
    void deleteById(Long id);
}
