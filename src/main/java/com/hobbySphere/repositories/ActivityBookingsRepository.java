package com.hobbySphere.repositories;

import com.hobbySphere.entities.ActivityBookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityBookingsRepository extends JpaRepository<ActivityBookings, Long> {

    boolean existsByActivityIdAndUserId(Long activityId, Long userId);

    @Query("SELECT COALESCE(SUM(ab.numberOfParticipants), 0) FROM ActivityBookings ab WHERE ab.activity.id = :activityId")
    int sumParticipantsByActivityId(@Param("activityId") Long activityId);

    int countByActivityId(Long activityId);

    List<ActivityBookings> findByBookingStatusIn(List<String> statuses);
    List<ActivityBookings> findByUserId(Long userId);
    List<ActivityBookings> findByUser_Id(Long userId);

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.user.email = :userEmail")
    List<ActivityBookings> findByUserEmail(@Param("userEmail") String userEmail);

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.user.email = :userEmail AND ab.bookingStatus IN :statuses")
    List<ActivityBookings> findByUserEmailAndBookingStatusIn(@Param("userEmail") String userEmail, @Param("statuses") List<String> statuses);

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.id = :bookingId AND ab.bookingStatus = :status")
    ActivityBookings findByIdAndBookingStatus(@Param("bookingId") Long bookingId, @Param("status") String status);

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.bookingStatus = 'Pending'")
    List<ActivityBookings> findActiveBookings();

    @Query("SELECT ab FROM ActivityBookings ab WHERE ab.bookingStatus IN ('Completed', 'Canceled')")
    List<ActivityBookings> findBookingHistory();

    // ✅ Total revenue (JPQL)
    @Query("SELECT SUM(b.totalPrice) FROM ActivityBookings b WHERE b.activity.business.id = :businessId")
    Double sumRevenueByBusinessId(@Param("businessId") Long businessId);

    // ✅ Monthly booking count (PostgreSQL-native)
    @Query(value = "SELECT COUNT(*) FROM activity_bookings ab " +
                   "JOIN activities a ON ab.activity_id = a.activity_id " +
                   "WHERE a.business_id = :businessId " +
                   "AND EXTRACT(MONTH FROM ab.booking_datetime) = :month " +
                   "AND EXTRACT(YEAR FROM ab.booking_datetime) = :year",
           nativeQuery = true)
    int countBookingsByMonthAndYear(@Param("businessId") Long businessId,
                                     @Param("month") int month,
                                     @Param("year") int year);

    // ✅ Peak booking hours (PostgreSQL-native)
    @Query(value = "SELECT EXTRACT(HOUR FROM ab.booking_datetime) AS hour, COUNT(*) AS count " +
                   "FROM activity_bookings ab " +
                   "JOIN activities a ON ab.activity_id = a.activity_id " +
                   "WHERE a.business_id = :businessId " +
                   "GROUP BY hour ORDER BY count DESC",
           nativeQuery = true)
    List<Object[]> findPeakBookingHours(@Param("businessId") Long businessId);

    @Query("SELECT COUNT(DISTINCT b.user.id) FROM ActivityBookings b WHERE b.activity.business.id = :businessId")
    int countDistinctCustomers(@Param("businessId") Long businessId);

    @Query(value = "SELECT COUNT(*) FROM (" +
                   "SELECT user_id FROM activity_bookings ab " +
                   "JOIN activities a ON ab.activity_id = a.activity_id " +
                   "WHERE a.business_id = :businessId " +
                   "GROUP BY user_id HAVING COUNT(*) > 1) AS returning_customers",
           nativeQuery = true)
    int countReturningCustomers(@Param("businessId") Long businessId);

    boolean existsByActivityIdAndUserIdAndBookingStatusNot(Long activityId, Long userId, String excludedStatus);

    List<ActivityBookings> findByActivityIdAndUserId(Long activityId, Long userId);

    void deleteByActivity_Id(Long activityId);
    
}
