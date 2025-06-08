package com.hobbySphere.controller;

import com.hobbySphere.entities.Review;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.ReviewService;
import com.hobbySphere.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
@Tag(name = "Reviews API", description = "Endpoints for managing customer reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtUtil jwtUtil;

    private boolean isAuthorized(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;

        String jwt = token.substring(7);
        String role = jwtUtil.extractRole(jwt);
        return "BUSINESS".equals(role) || "SUPER_ADMIN".equals(role);
    }

    @GetMapping
    public ResponseEntity<?> getAllReviews(@RequestHeader("Authorization") String token) {
        if (!isAuthorized(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        List<Review> reviews = reviewService.getAllReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> getReviewsByActivity(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "ID of the activity to fetch reviews for") @PathVariable Long activityId) {

        if (!isAuthorized(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        List<Review> reviews = reviewService.getReviewsByActivity(activityId);
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> addReview(
        @RequestHeader("Authorization") String token,
        @RequestBody Review review
    ) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        String jwt = token.substring(7);
        String role = jwtUtil.extractRole(jwt);
        Long userId = jwtUtil.extractId(jwt);

        if (!"USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only users can submit reviews.");
        }

        Users user = new Users();
        user.setId(userId);
        review.setCustomer(user); // set the review creator

        // createdAt/updatedAt already handled by @PrePersist/@PreUpdate, but you can keep this too
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewService.saveReview(review);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }
}
