package com.hobbySphere.repositories;

import com.hobbySphere.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByActivityIdOrderByDateDesc(Long activityId);  
    List<Review> findAllByOrderByDateDesc();
}
