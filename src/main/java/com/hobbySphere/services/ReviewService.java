package com.hobbySphere.services;

import com.hobbySphere.entities.Review;
import com.hobbySphere.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByDateDesc();  // Get all reviews sorted by date
    }

    public List<Review> getReviewsByActivity(Long activityId) {
        return reviewRepository.findByActivityIdOrderByDateDesc(activityId);  // Get reviews for a specific activity
    }
    
    public List<Review> getReviewsByBusiness(Long businessId) {
        return reviewRepository.findReviewsByBusinessId(businessId);
    }

}
