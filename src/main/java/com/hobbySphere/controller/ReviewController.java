package com.hobbySphere.controller;

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

        String jwt = token.substring(7).trim();

        // Check for AdminUser roles
        String adminRole = jwtUtil.extractRole(jwt); // returns SUPER_ADMIN or MANAGER if Admin token
        if ("SUPER_ADMIN".equals(adminRole) || "MANAGER".equals(adminRole)) return true;

        // Check for valid business or user token
        String email = jwtUtil.extractUsername(jwt);
        return jwtUtil.isBusinessToken(jwt) || jwtUtil.isUserToken(jwt);
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

        if (!isAuthorized(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        List<Review> reviews = reviewService.getReviewsByActivity(activityId);
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(reviews, HttpStatus.OK);
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

}
