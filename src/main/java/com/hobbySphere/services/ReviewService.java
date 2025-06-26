package com.hobbySphere.services;

import com.hobbySphere.dto.ReviewDTO;
import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.Review;
import com.hobbySphere.entities.Users;
import com.hobbySphere.enums.NotificationType;
import com.hobbySphere.repositories.ActivitiesRepository;
import com.hobbySphere.repositories.ActivityBookingsRepository;
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
    private ActivityBookingsRepository activityBookingsRepository;
    
    @Autowired
    private NotificationsService notificationsService;



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
        String email = jwtUtil.extractUsername(jwt);

        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Activities activity = activityRepository.findById(dto.getActivityId())
            .orElseThrow(() -> new RuntimeException("Activity not found"));

        // ✅ Check if the user completed this activity before reviewing
        boolean hasCompletedBooking = activityBookingsRepository
        	    .existsByUserIdAndActivityIdAndBookingStatus(user.getId(), activity.getId(), "Completed");


        if (!hasCompletedBooking) {
            throw new RuntimeException("You can only review this activity after completing a booking.");
        }

        // Save review
        Review review = new Review();
        review.setCustomer(user);
        review.setActivity(activity);
        review.setRating(dto.getRating());
        review.setFeedback(dto.getFeedback());
        review.setDate(LocalDateTime.now());

        // ✅ Save the review
        Review savedReview = reviewRepository.save(review);

        // ✅ Notify the business
        String message = user.getFirstName() + " reviewed your activity: " + activity.getActivityName();
        notificationsService.notifyBusiness(
            activity.getBusiness(),
            message,
            NotificationType.NEW_REVIEW
        );

        return savedReview;
    }
   
    public boolean hasUserCompletedActivity(Long activityId, String token) {
        String jwt = token.substring(7); // clean "Bearer " prefix
        String email = jwtUtil.extractUsername(jwt);
        Users user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Long userId = user.getId();

        System.out.println("🔍 Checking if user " + userId + " completed activity " + activityId);

        return activityBookingsRepository
            .existsByActivityIdAndUserIdAndBookingStatus(activityId, userId, "Completed");
    }
    
    public List<Long> getCompletedActivityIdsForUser(String token) {
        String jwt = token.substring(7); // remove "Bearer "
        String email = jwtUtil.extractUsername(jwt);
        Users user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return activityBookingsRepository.findCompletedActivityIdsByUser(user.getId());
    }
    
    public boolean shouldShowReviewModal(Long activityId, String token) {
        String jwt = token.substring(7);
        String email = jwtUtil.extractUsername(jwt);
        Users user = userRepository.findByEmail(email);

        if (user == null) throw new RuntimeException("User not found");

        boolean completed = activityBookingsRepository
            .existsByActivityIdAndUserIdAndBookingStatus(activityId, user.getId(), "Completed");

        boolean alreadyReviewed = reviewRepository
            .existsByActivityIdAndCustomerId(activityId, user.getId());

        return completed && !alreadyReviewed;
    }
    
    public Long getFirstCompletedUnreviewedActivity(String token) {
        String jwt = token.substring(7);
        String email = jwtUtil.extractUsername(jwt);
        Users user = userRepository.findByEmail(email);

        if (user == null) throw new RuntimeException("User not found");

        List<Long> completedActivityIds = activityBookingsRepository.findCompletedActivityIdsByUser(user.getId());

        for (Long activityId : completedActivityIds) {
            boolean alreadyReviewed = reviewRepository.existsByActivityIdAndCustomerId(activityId, user.getId());

            if (!alreadyReviewed) {
                return activityId; 
            }
        }

        return null; 
    }





}




