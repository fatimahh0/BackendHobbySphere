package com.hobbySphere.repositories;

import com.hobbySphere.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByActivityIdOrderByDateDesc(Long activityId);

    List<Review> findAllByOrderByDateDesc();

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT r FROM Review r WHERE r.activity.business.id = :businessId ORDER BY r.createdAt DESC")
    List<Review> findByBusinessId(@Param("businessId") Long businessId);

    void deleteByActivity_Id(Long activityId); // ✅ for business deletion

   void deleteByCustomer_Id(Long customerId);
   
   @Query("SELECT r FROM Review r WHERE r.activity.business.id = :businessId ORDER BY r.createdAt DESC")
   List<Review> findReviewsByBusinessId(@Param("businessId") Long businessId);

   List<Review> findByCustomerUsernameOrderByDateDesc(String username);


}
