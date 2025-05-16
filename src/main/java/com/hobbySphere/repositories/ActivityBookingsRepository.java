package com.hobbySphere.repositories;

import com.hobbySphere.entities.ActivityBookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityBookingsRepository extends JpaRepository<ActivityBookings, Long> {

    // Check if user already booked the activity
    boolean existsByActivityIdAndUserId(Long activityId, Long userId);

    @Query("SELECT COALESCE(SUM(ab.numberOfParticipants), 0) FROM ActivityBookings ab WHERE ab.activity.id = :activityId")
    int sumParticipantsByActivityId(@Param("activityId") Long activityId);

    int countByActivityId(Long activityId);

    List<ActivityBookings> findByBookingStatusIn(List<String> statuses);
    List<ActivityBookings> findByUserId(Long userId);

    List<ActivityBookings> findByUser_Id(Long userId);

    // Method to find bookings by user email
    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.user.email = :userEmail")
    List<ActivityBookings> findByUserEmail(@Param("userEmail") String userEmail);

    // Method to find bookings by user email and booking statuses (e.g., Pending, Completed, Canceled)
    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.user.email = :userEmail AND ab.bookingStatus IN :statuses")
    List<ActivityBookings> findByUserEmailAndBookingStatusIn(@Param("userEmail") String userEmail, @Param("statuses") List<String> statuses);

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.id = :bookingId AND ab.bookingStatus = :status")
    ActivityBookings findByIdAndBookingStatus(@Param("bookingId") Long bookingId, @Param("status") String status);

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.bookingStatus = 'Pending'")
    List<ActivityBookings> findActiveBookings();

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.bookingStatus IN ('Completed', 'Canceled')")
    List<ActivityBookings> findBookingHistory();

    boolean existsByActivityIdAndUserIdAndBookingStatusNot(Long activityId, Long userId, String excludedStatus);

    List<ActivityBookings> findByActivityIdAndUserId(Long activityId, Long userId);
}
