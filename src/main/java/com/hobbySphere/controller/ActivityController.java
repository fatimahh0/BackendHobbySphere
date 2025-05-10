package com.hobbySphere.controller;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.ActivityService;
import com.hobbySphere.services.ActivityBookingService;
import com.hobbySphere.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/activities")
@Tag(name = "Activities API", description = "Endpoints for managing activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityBookingService bookingService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get activities by business ID")
    @ApiResponse(responseCode = "200", description = "List of activities for a given business")
    @GetMapping("/business/{businessId}")
    public List<Activities> getActivitiesByBusiness(@PathVariable Long businessId) {
        return activityService.findByBusinessId(businessId);
    }

    @Operation(summary = "Get all activities")
    @ApiResponse(responseCode = "200", description = "Returns all activities")
    @GetMapping
    public List<Activities> getAllActivities() {
        return activityService.findAllActivities();
    }

    @Operation(summary = "Create a new activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Activity created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Activities> createActivity(@RequestBody Activities activity) {
        Activities savedActivity = activityService.save(activity);
        return new ResponseEntity<>(savedActivity, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing activity by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity updated successfully"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Activities> updateActivity(@PathVariable Long id, @RequestBody Activities activity) {
        Optional<Activities> existingActivity = Optional.ofNullable(activityService.findById(id));
        if (existingActivity.isPresent()) {
            activity.setId(id);
            Activities updatedActivity = activityService.save(activity);
            return new ResponseEntity<>(updatedActivity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete an activity by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Activity deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        Optional<Activities> existingActivity = Optional.ofNullable(activityService.findById(id));
        if (existingActivity.isPresent()) {
            activityService.deleteActivity(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Get a single activity by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Activities> getActivityById(@PathVariable Long id) {
        Optional<Activities> activity = Optional.ofNullable(activityService.findById(id));
        if (activity.isPresent()) {
            return new ResponseEntity<>(activity.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Book an activity
    @PostMapping("/{activityId}/book")
    public ResponseEntity<Map<String, Object>> bookActivity(
            @PathVariable long activityId,
            @RequestParam int participants,
            @RequestParam String paymentMethod,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        // Fetch the activity
        Activities activity = activityService.findById(activityId);
        if (activity == null) {
            response.put("message", "Activity not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        // Get current number of booked participants
        int currentBooked = bookingService.countParticipantsByActivityId(activityId);
        int maxCapacity = activity.getMaxParticipants();  // Use maxParticipants from activity entity

        // Check if booking exceeds max participants
        if (currentBooked + participants > maxCapacity) {
            response.put("message", "Booking exceeds maximum allowed participants");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Fetch the user making the booking
        Users user = userService.findByEmail(principal.getName());

        // Calculate the total price based on the participants
        BigDecimal totalPrice = activity.getPrice().multiply(BigDecimal.valueOf(participants));

        // Create the booking
        ActivityBookings booking = new ActivityBookings(activity, user, participants, totalPrice, paymentMethod);
        bookingService.save(booking);

        // Response message with booking ID
        response.put("message", "Booking successful");
        response.put("bookingId", booking.getId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
