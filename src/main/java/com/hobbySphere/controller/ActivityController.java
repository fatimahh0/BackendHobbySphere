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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import com.hobbySphere.dto.BookingRequest;

import org.springframework.transaction.annotation.Transactional;




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

    @Operation(summary = "Get activities by business ID", description = "Retrieve a list of activities associated with a specific business")
    @ApiResponse(responseCode = "200", description = "List of activities for a given business")
    @GetMapping("/business/{businessId}")
    public List<Activities> getActivitiesByBusiness(
            @Parameter(description = "ID of the business to fetch activities for") @PathVariable Long businessId) {
        return activityService.findByBusinessId(businessId);
    }

    @Operation(summary = "Get all activities", description = "Retrieve a list of all activities in the system")
    @ApiResponse(responseCode = "200", description = "Returns all activities")
    @GetMapping
    public List<Activities> getAllActivities() {
        return activityService.findAllActivities();
    }

    @Operation(summary = "Create a new activity with image upload", description = "Create a new activity and optionally upload an image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Activity created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<?> createActivityWithImage(
            @RequestParam("activityName") String activityName,
            @RequestParam("activityType") String activityType,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("maxParticipants") int maxParticipants,
            @RequestParam("price") BigDecimal price,
            @RequestParam("startDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDatetime,
            @RequestParam("endDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDatetime,
            @RequestParam("status") String status,
            @RequestParam("businessId") Long businessId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            Activities activity = activityService.createActivityWithImage(
                    activityName,
                    activityType,
                    description,
                    location,
                    maxParticipants,
                    price,
                    startDatetime,
                    endDatetime,
                    status,
                    businessId,
                    image
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Activity created successfully",
                    "activity", activity
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Failed to create activity",
                    "error", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Update an existing activity with an image", description = "This API updates an existing activity with new details and an optional image.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @PutMapping("/{id}/update-with-image")
    public ResponseEntity<?> updateActivityWithImage(
            @Parameter(description = "ID of the activity to update") @PathVariable Long id,
            @RequestParam("activityName") String activityName,
            @RequestParam("activityType") String activityType,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("maxParticipants") int maxParticipants,
            @RequestParam("price") BigDecimal price,
            @RequestParam("startDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDatetime,
            @RequestParam("endDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDatetime,
            @RequestParam("status") String status,
            @RequestParam("businessId") Long businessId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            Activities updatedActivity = activityService.updateActivityWithImage(
                    id, activityName, activityType, description, location, maxParticipants, price,
                    startDatetime, endDatetime, status, businessId, image
            );
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "Activity updated successfully",
                    "activity", updatedActivity
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Failed to update activity",
                    "error", e.getMessage()
            ));
        }
    }


    @Operation(summary = "Delete an activity by ID", description = "Delete an activity by providing its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Activity deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@Parameter(description = "ID of the activity to delete") @PathVariable Long id) {
        Optional<Activities> existingActivity = Optional.ofNullable(activityService.findById(id));
        if (existingActivity.isPresent()) {
            activityService.deleteActivity(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Get a single activity by ID", description = "Retrieve details of a specific activity by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Activities> getActivityById(@Parameter(description = "ID of the activity to retrieve") @PathVariable Long id) {
        Optional<Activities> activity = Optional.ofNullable(activityService.findById(id));
        if (activity.isPresent()) {
            return new ResponseEntity<>(activity.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }




    @PostMapping("/{activityId}/book")
    public ResponseEntity<Map<String, Object>> bookActivity(
            @PathVariable long activityId,
            @RequestBody BookingRequest request,
            Principal principal) {

    @Operation(summary = "Book an activity", description = "Allows a user to book an activity and receive booking details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking successful"),
        @ApiResponse(responseCode = "400", description = "Invalid booking request"),
        @ApiResponse(responseCode = "404", description = "Activity not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{activityId}/book")
    public ResponseEntity<?> bookActivity(
        @Parameter(description = "ID of the activity to book") @PathVariable long activityId,
        @RequestBody BookingRequest request,
        Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }


        Map<String, Object> response = new HashMap<>();

        // 
        Activities activity = activityService.findById(activityId);
        if (activity == null) {
            response.put("message", "Activity not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        // 
        Users user = userService.findByEmail(principal.getName());
        
        

        // 
        activityService.updateStatusIfCanceled(activity);

        // 
        if (bookingService.hasUserAlreadyBookedd(activity.getId(), user.getId())) {
            response.put("message", "You already booked this activity");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // 
        int currentBooked = bookingService.countParticipantsByActivityId(activity.getId());
        if (currentBooked + request.getParticipants() > activity.getMaxParticipants()) {
            response.put("message", "Booking exceeds maximum allowed participants");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // 
        BigDecimal totalPrice = activity.getPrice().multiply(BigDecimal.valueOf(request.getParticipants()));
        ActivityBookings booking = new ActivityBookings(activity, user, request.getParticipants(), totalPrice, request.getPaymentMethod());
        bookingService.save(booking);

        // response
        response.put("message", "Booking successful");
        response.put("bookingId", booking.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
