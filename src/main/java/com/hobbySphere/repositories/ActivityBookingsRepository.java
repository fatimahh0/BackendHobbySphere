package com.hobbySphere.repositories;

import com.hobbySphere.entities.ActivityBookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
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
        List<ActivityBookings> findByUserEmailAndBookingStatusIn(
                        @Param("userEmail") String userEmail,
                        @Param("statuses") List<String> statuses);

        @Query("SELECT ab FROM ActivityBookings ab WHERE ab.id = :bookingId AND ab.bookingStatus = :status")
        ActivityBookings findByIdAndBookingStatus(
                        @Param("bookingId") Long bookingId,
                        @Param("status") String status);

        @Query("SELECT ab FROM ActivityBookings ab WHERE ab.bookingStatus = 'Pending'")
        List<ActivityBookings> findActiveBookings();

        @Query("SELECT ab FROM ActivityBookings ab WHERE ab.bookingStatus IN ('Completed', 'Canceled')")
        List<ActivityBookings> findBookingHistory();

        @Query("SELECT SUM(b.totalPrice) FROM ActivityBookings b WHERE b.activity.business.id = :businessId AND b.bookingStatus IN ('Pending', 'Confirmed', 'Completed', 'Terminated')")
        Double sumRevenueByBusinessId(@Param("businessId") Long businessId);

        @Query(value = "SELECT COUNT(*) FROM activity_bookings ab " +
                        "JOIN activities a ON ab.activity_id = a.activity_id " +
                        "WHERE a.business_id = :businessId " +
                        "AND ab.booking_status IN ('Pending', 'Confirmed', 'Completed', 'Terminated') " +
                        "AND EXTRACT(MONTH FROM ab.booking_datetime) = :month " +
                        "AND EXTRACT(YEAR FROM ab.booking_datetime) = :year", nativeQuery = true)
        int countBookingsByMonthAndYear(@Param("businessId") Long businessId,
                        @Param("month") int month,
                        @Param("year") int year);

        @Query(value = "SELECT EXTRACT(HOUR FROM ab.booking_datetime) AS hour, COUNT(*) AS count " +
                        "FROM activity_bookings ab " +
                        "JOIN activities a ON ab.activity_id = a.activity_id " +
                        "WHERE a.business_id = :businessId " +
                        "AND ab.booking_status IN ('Pending', 'Confirmed', 'Completed', 'Terminated') " +
                        "GROUP BY hour ORDER BY count DESC", nativeQuery = true)
        List<Object[]> findPeakBookingHours(@Param("businessId") Long businessId);

        @Query("SELECT COUNT(DISTINCT b.user.id) FROM ActivityBookings b WHERE b.activity.business.id = :businessId AND b.bookingStatus IN ('Pending', 'Confirmed', 'Completed', 'Terminated')")
        int countDistinctCustomers(@Param("businessId") Long businessId);

        @Query(value = "SELECT COUNT(*) FROM (" +
                        "SELECT user_id FROM activity_bookings ab " +
                        "JOIN activities a ON ab.activity_id = a.activity_id " +
                        "WHERE a.business_id = :businessId " +
                        "AND ab.booking_status IN ('Pending', 'Confirmed', 'Completed', 'Terminated') " +
                        "GROUP BY user_id HAVING COUNT(*) > 1) AS returning_customers", nativeQuery = true)
        int countReturningCustomers(@Param("businessId") Long businessId);

        boolean existsByActivityIdAndUserIdAndBookingStatusNot(Long activityId, Long userId, String excludedStatus);

        List<ActivityBookings> findByActivityIdAndUserId(Long activityId, Long userId);

        void deleteByUserId(Long userId);

        void deleteByActivity_Id(Long activityId);

        long countByBookingDatetimeAfter(LocalDateTime date);

        @Query("SELECT b FROM ActivityBookings b WHERE b.activity.business.email = :email")
        List<ActivityBookings> findByActivityBusinessEmail(@Param("email") String email);

        @Modifying
        @Transactional
        @Query("DELETE FROM ActivityBookings ab WHERE ab.activity.id = :activityId")
        void deleteByActivityId(@Param("activityId") Long activityId);

        @Query("SELECT b FROM ActivityBookings b JOIN FETCH b.activity a WHERE b.user.email = :userEmail")
        List<ActivityBookings> findByUserEmailWithActivity(@Param("userEmail") String userEmail);

        @Query("SELECT b FROM ActivityBookings b JOIN FETCH b.activity JOIN FETCH b.user")
        List<ActivityBookings> findAllWithActivityAndUser();

        boolean existsByUserIdAndActivityIdAndBookingStatus(Long id, Long id2, String string);

        List<ActivityBookings> findByUserIdAndBookingStatusIn(Long userId, List<String> statuses);

        boolean existsByActivityIdAndUserIdAndBookingStatus(Long activityId, Long userId, String string);

        @Query("SELECT DISTINCT b.activity.id FROM ActivityBookings b WHERE b.user.id = :userId AND b.bookingStatus = 'Completed'")
        List<Long> findCompletedActivityIdsByUser(@Param("userId") Long userId);

        List<ActivityBookings> findByUserIdAndBookingStatus(Long userId, String bookingStatus);
        
    
        @Query("SELECT ab FROM ActivityBookings ab " +
               "WHERE ab.user.id = :userId " +
               "AND ab.activity.business.status = com.hobbySphere.enums.BusinessStatus.ACTIVE")
        List<ActivityBookings> findByUserIdAndActiveBusiness(@Param("userId") Long userId);

        @Query("SELECT ab FROM ActivityBookings ab " +
               "WHERE ab.user.id = :userId " +
               "AND ab.bookingStatus IN :statuses " +
               "AND ab.activity.business.status = com.hobbySphere.enums.BusinessStatus.ACTIVE")
        List<ActivityBookings> findByUserIdAndStatusesAndActiveBusiness(
                @Param("userId") Long userId,
                @Param("statuses") List<String> statuses);

        @Query("SELECT b FROM ActivityBookings b " +
               "JOIN FETCH b.activity a " +
               "WHERE b.user.email = :userEmail " +
               "AND a.business.status = com.hobbySphere.enums.BusinessStatus.ACTIVE")
        List<ActivityBookings> findByUserEmailWithActivityAndActiveBusiness(@Param("userEmail") String userEmail);

        @Query("SELECT b FROM ActivityBookings b " +
               "JOIN FETCH b.activity a " +
               "JOIN FETCH b.user u " +
               "WHERE a.business.status = com.hobbySphere.enums.BusinessStatus.ACTIVE")
        List<ActivityBookings> findAllWithActivityAndUserAndActiveBusiness();


}
