package com.hobbySphere.controller;

import com.hobbySphere.dto.ReviewDTO;
import com.hobbySphere.entities.Review;
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
        String role = jwtUtil.extractRole(jwt);  // This method must extract "role" claim from token
        return "BUSINESS".equals(role) || "SUPER_ADMIN".equals(role) || "MANAGER".equals(role);
    }

    @Operation(
        summary = "Get all reviews",
        description = "Retrieve a list of all customer reviews, sorted by date."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of all reviews retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllReviews(@RequestHeader("Authorization") String token) {
        if (!isAuthorized(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        List<Review> reviews = reviewService.getAllReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @Operation(
        summary = "Get reviews by activity ID",
        description = "Retrieve a list of reviews for a specific activity, sorted by date."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of reviews for the activity retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Activity not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> getReviewsByActivity(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "ID of the activity to fetch reviews for") @PathVariable Long activityId) {

    	String jwt = token.substring(7);
	    String role = jwtUtil.extractRole(jwt);
        
        
        if (!isAuthorized(token) && (!"USER".equals(role))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }
        

        List<Review> reviews = reviewService.getReviewsByActivity(activityId);
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    
    
    @Operation(
    	    summary = "Add a review",
    	    description = "Allows a customer to submit a review for an activity"
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "201", description = "Review submitted successfully"),
    	    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
    	    @ApiResponse(responseCode = "404", description = "Activity or user not found"),
    	    @ApiResponse(responseCode = "500", description = "Internal server error")
    	})
    	@PostMapping("addreviews")
    	public ResponseEntity<?> addReview(
    	        @RequestHeader("Authorization") String token,
    	        @RequestBody ReviewDTO dto) {
    	    
    	    if (token == null || !token.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
    	    }

    	    String jwt = token.substring(7);
    	    String role = jwtUtil.extractRole(jwt);
    	    if (!"USER".equals(role)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only User can submit reviews.");
    	    }

    	    try {
    	        Review review = reviewService.createReviewFromDTO(dto, token);
    	        return new ResponseEntity<>(review, HttpStatus.CREATED);
    	    } catch (RuntimeException e) {
    	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    	    }
    	}
    
    @Operation(
    	    summary = "Get all reviews for a business",
    	    description = "Retrieve all reviews for activities belonging to a specific business."
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
    	    @ApiResponse(responseCode = "404", description = "No reviews found for the business")
    	})
    	@GetMapping("/business/{businessId}")
    	public ResponseEntity<?> getReviewsByBusiness(
    	        @RequestHeader("Authorization") String token,
    	        @Parameter(description = "ID of the business") @PathVariable Long businessId) {

    	    if (!isAuthorized(token)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
    	    }

    	    List<Review> reviews = reviewService.getReviewsByBusiness(businessId);
    	    if (reviews.isEmpty()) {
    	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No reviews found for this business.");
    	    }

    	    return ResponseEntity.ok(reviews);
    	}

    
    @GetMapping("/check-completed/{activityId}")
    public ResponseEntity<?> hasCompletedActivity(
            @RequestHeader("Authorization") String token,
            @PathVariable Long activityId) {

        try {
            boolean hasCompleted = reviewService.hasUserCompletedActivity(activityId, token);
            return ResponseEntity.ok(hasCompleted);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    
    @GetMapping("/completed-activities")
    public ResponseEntity<?> getCompletedActivitiesForUser(@RequestHeader("Authorization") String token) {
        try {
            List<Long> activityIds = reviewService.getCompletedActivityIdsForUser(token);
            return ResponseEntity.ok(activityIds);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/should-show-modal/{activityId}")
    public ResponseEntity<?> shouldShowReviewModal(
            @RequestHeader("Authorization") String token,
            @PathVariable Long activityId) {
        try {
            boolean result = reviewService.shouldShowReviewModal(activityId, token);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    
    @GetMapping("/suggest")
    public ResponseEntity<Long> suggestReviewActivity(@RequestHeader("Authorization") String token) {
        Long activityId = reviewService.getFirstCompletedUnreviewedActivity(token);
        return ResponseEntity.ok(activityId);
        }
    @GetMapping("/business/{businessId}/check-rating")
    public ResponseEntity<?> checkRatingAndNotifyAdmins(
            @PathVariable Long businessId,
            @RequestHeader("Authorization") String token) {

        if (!token.startsWith("Bearer ") || !jwtUtil.extractRole(token.substring(7)).equals("SUPER_ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        double avgRating = reviewService.checkAndNotifyIfLowRating(businessId);

        if (avgRating == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No reviews found for this business.");
        }

        return ResponseEntity.ok("Average rating: " + avgRating);
    }



}
