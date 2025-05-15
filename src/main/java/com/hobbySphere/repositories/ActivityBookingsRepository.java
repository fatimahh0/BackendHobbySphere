package com.hobbySphere.repositories;

import com.hobbySphere.entities.ActivityBookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityBookingsRepository extends JpaRepository<ActivityBookings, Long> {

	 
	 
    @Query("SELECT COALESCE(SUM(ab.numberOfParticipants), 0) FROM ActivityBookings ab WHERE ab.activity.id = :activityId")
    int sumParticipantsByActivityId(@Param("activityId") Long activityId);

    int countByActivityId(Long activityId);

	List<ActivityBookings> findByUserId(Long userId);
	
	
	List<ActivityBookings> findByUser_Id(Long userId);

	
	List<ActivityBookings> findByUserEmailAndBookingStatusIn(String userEmail, List<String> of);

    
    
}
