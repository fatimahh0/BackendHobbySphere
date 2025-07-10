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
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
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
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> getReviewsByActivity(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "ID of the activity to fetch reviews for") @PathVariable Long activityId) {

        String jwt = token.substring(7);
        String role = jwtUtil.extractRole(jwt);

        if (!isAuthorized(token) && !"USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        try {
            List<Review> reviews = reviewService.getReviewsByActivity(activityId);
            // ✅ Always return 200, even if the list is empty
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

    
    @Operation(
    	    summary = "Add a review",
    	    description = "Allows a customer to submit a review for an activity. The review must include at least a rating or feedback."
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "201", description = "Review submitted successfully"),
    	    @ApiResponse(responseCode = "400", description = "Invalid request – rating and feedback cannot both be empty"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Only users can submit reviews"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – Activity or user not found"),
    	    @ApiResponse(responseCode = "500", description = "Internal server error")
    	})
    	@PostMapping("/addreviews")
    	public ResponseEntity<?> addReview(
    	        @RequestHeader("Authorization") String token,
    	        @RequestBody ReviewDTO dto) {

    	    if (token == null || !token.startsWith("Bearer ")) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
    	    }

    	    String jwt = token.substring(7);
    	    String role = jwtUtil.extractRole(jwt);

    	    if (!"USER".equals(role)) {
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only users can submit reviews.");
    	    }

    	    try {
    	        Review review = reviewService.createReviewFromDTO(dto, token);
    	        return new ResponseEntity<>(review, HttpStatus.CREATED);
    	    } catch (RuntimeException e) {
    	        // Handle known validation (like empty rating & feedback)
    	        String msg = e.getMessage();
    	        if (msg != null && msg.toLowerCase().contains("at least")) {
    	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    	        }
    	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    	    }
    	}

    
    @Operation(
    	    summary = "Get all reviews for a business",
    	    description = "Retrieve all reviews for activities belonging to a specific business."
    	)
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
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

    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
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
    
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/completed-activities")
    public ResponseEntity<?> getCompletedActivitiesForUser(@RequestHeader("Authorization") String token) {
        try {
            List<Long> activityIds = reviewService.getCompletedActivityIdsForUser(token);
            return ResponseEntity.ok(activityIds);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
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
    
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/suggest")
    public ResponseEntity<?> suggestReviewActivity(@RequestHeader("Authorization") String token) {
        try {
            Long activityId = reviewService.getFirstCompletedUnreviewedActivity(token);

            if (activityId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No suggested activity to review.");
            }

            return ResponseEntity.ok(activityId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
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
