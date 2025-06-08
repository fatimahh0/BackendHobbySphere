package com.hobbySphere.services;

import com.hobbySphere.dto.ReviewDTO;
import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.Review;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.ActivitiesRepository;
import com.hobbySphere.repositories.ReviewRepository;
import com.hobbySphere.repositories.UsersRepository;
import com.hobbySphere.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ActivitiesRepository activityRepository;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByDateDesc();
    }

    public List<Review> getReviewsByActivity(Long activityId) {
        return reviewRepository.findByActivityIdOrderByDateDesc(activityId);
    }

    public List<Review> getReviewsByBusiness(Long businessId) {
        return reviewRepository.findReviewsByBusinessId(businessId);
    }

    public Review createReviewFromDTO(ReviewDTO dto, String token) {
        String jwt = token.substring(7);
        String email = jwtUtil.extractUsername(jwt); // subject = email

        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Activities activity = activityRepository.findById(dto.getActivityId()).orElse(null);
        if (activity == null) {
            throw new RuntimeException("Activity not found");
        }

        Review review = new Review();
        review.setCustomer(user);
        review.setActivity(activity);
        review.setRating(dto.getRating());
        review.setFeedback(dto.getFeedback());
        review.setDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

}
