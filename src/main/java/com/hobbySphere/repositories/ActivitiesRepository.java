package com.hobbySphere.repositories;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.dto.AdminActivityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activities, Long> {

    List<Activities> findByBusinessId(Long businessId);

    @Query(value = "SELECT a.activity_name FROM activity_bookings b " +
            "JOIN activities a ON b.activity_id = a.activity_id " +
            "WHERE a.business_id = :businessId " +
            "GROUP BY a.activity_name " +
            "ORDER BY COUNT(b.booking_id) DESC LIMIT 1", nativeQuery = true)
    String findTopActivityNameByBusinessId(@Param("businessId") Long businessId);

    void deleteByBusinessId(Long businessId); // ✅ from File1
    void deleteById(Long id);                 // ✅ from File2

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT a.activityName, COUNT(b.id) AS bookings, a.viewCount " +
    	       "FROM Activities a " +
    	       "LEFT JOIN a.bookings b " +
    	       "WHERE a.business.status.name = 'ACTIVE' " +
    	       "AND a.business.isPublicProfile = true " +
    	       "GROUP BY a.id, a.activityName, a.viewCount " +
    	       "ORDER BY bookings DESC, a.viewCount DESC")
    	List<Object[]> findPopularActivities();


    	@Query("SELECT new com.hobbySphere.dto.AdminActivityDTO(" +
    		       "a.id, a.activityName, b.businessName, a.startDatetime, a.maxParticipants, a.description) " +
    		       "FROM Activities a JOIN a.business b " +
    		       "WHERE b.status.name = 'ACTIVE' AND b.isPublicProfile = true")
    		List<AdminActivityDTO> findAllActivitiesWithBusinessInfo();

    	@Query("""
    			SELECT a FROM Activities a
    			WHERE a.activityType.interest.id IN (
    			    SELECT ui.id.interest.id FROM UserInterests ui
    			    WHERE ui.id.user.id = :userId
    			)
    			AND a.business.status.name = 'ACTIVE'
    			AND a.business.isPublicProfile = true
    			""")
    		List<Activities> findAllByUserInterests(@Param("userId") Long userId);


    	@Query("SELECT a FROM Activities a " +
    		       "WHERE a.business.status.name = 'ACTIVE' " +
    		       "AND a.business.isPublicProfile = true")
    		List<Activities> findAllPublicActiveBusinessActivities();


    Activities findByActivityName(String activity);
} 
