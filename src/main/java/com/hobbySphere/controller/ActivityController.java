package com.hobbySphere.controller;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.CurrencyRepository;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175"
})
@Tag(name = "Activities API", description = "Endpoints for managing activities")
public class ActivityController {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityBookingService bookingService;

    @Autowired
    private UserService userService;

    // ✅ Get all activities for a business (with auto status update)
    @Operation(summary = "Get activities by business ID", description = "Retrieve a list of activities associated with a specific business and auto-update expired ones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of activities for a given business"),
            @ApiResponse(responseCode = "404", description = "Business not found")
    })
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getActivitiesByBusiness(
            @Parameter(description = "ID of the business to fetch activities for") @PathVariable Long businessId) {

        List<Activities> activities = activityService.findByBusinessId(businessId);

        if (activities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No activities found for this business"));
        }

        LocalDateTime now = LocalDateTime.now();
        for (Activities activity : activities) {
            if (activity.getEndDatetime().isBefore(now) && !"Terminated".equalsIgnoreCase(activity.getStatus())) {
                activity.setStatus("Terminated");
                activityService.save(activity);
            }
        }

        return ResponseEntity.ok(activities);
    }

    // ✅ Get all activities
    @GetMapping
    @Operation(summary = "Get all activities", description = "Retrieve a list of all activities in the system and auto-update expired statuses")
    public List<Activities> getAllActivities() {
        List<Activities> allActivities = activityService.findAllActivities();
        LocalDateTime now = LocalDateTime.now();

        for (Activities activity : allActivities) {
            if (activity.getEndDatetime().isBefore(now) && !"Terminated".equalsIgnoreCase(activity.getStatus())) {
                activity.setStatus("Terminated");
                activityService.save(activity);
            }
        }

        return allActivities;
    }

    // ✅ Get upcoming activities
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming activities", description = "Retrieve all activities that are not yet expired")
    public List<Activities> getUpcomingActivities() {
        return activityService.findAllActivities().stream()
                .filter(a -> a.getEndDatetime().isAfter(LocalDateTime.now()))
                .toList();
    }

    // ✅ Get terminated activities
    @GetMapping("/terminated")
    @Operation(summary = "Get terminated activities", description = "Retrieve all activities that have ended")
    public List<Activities> getTerminatedActivities() {
        return activityService.findAllActivities().stream()
                .filter(a -> a.getEndDatetime().isBefore(LocalDateTime.now()))
                .peek(a -> {
                    if (!"Terminated".equalsIgnoreCase(a.getStatus())) {
                        a.setStatus("Terminated");
                        activityService.save(a);
                    }
                })
                .toList();
    }

    // ✅ Create activity with image
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    @Operation(summary = "Create a new activity with image upload", description = "Create a new activity and optionally upload an image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Activity created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<?> createActivityWithImage(
            @RequestParam("activityName") String activityName,
            @RequestParam("activityTypeId") Long activityTypeId,
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
                    activityName, activityTypeId, description, location, maxParticipants, price,
                    startDatetime, endDatetime, status, businessId, image
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Activity created successfully",
                    "activity", activity
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Failed to create activity",
                    "error", e.getMessage()
            ));
        }
    }

    // ✅ Update activity with image
    @PutMapping("/{id}/update-with-image")
    @Operation(summary = "Update an existing activity with an image", description = "This API updates an existing activity with new details and an optional image.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<?> updateActivityWithImage(
            @PathVariable Long id,
            @RequestParam("activityName") String activityName,
            @RequestParam("activityTypeId") Long activityTypeId,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("maxParticipants") int maxParticipants,
            @RequestParam("price") BigDecimal price,
            @RequestParam("startDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDatetime,
            @RequestParam("endDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDatetime,
            @RequestParam("status") String status,
            @RequestParam("businessId") Long businessId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "imageRemoved", required = false) Boolean imageRemoved
    ) {
        try {
        	Activities updatedActivity = activityService.updateActivityWithImage(
        		    id, activityName, activityTypeId, description, location, maxParticipants, price,
        		    startDatetime, endDatetime, status, businessId, image, imageRemoved != null && imageRemoved
        		);

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "Activity updated successfully",
                    "activity", updatedActivity
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Failed to update activity",
                    "error", e.getMessage()
            ));
        }
    }

    // ✅ Delete activity
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an activity by ID", description = "Delete an activity by providing its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Activity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        Activities activity = activityService.findById(id);
        if (activity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Activity not found"));
        }

        try {
            bookingService.deleteByActivityId(id); // delete bookings
            activityService.deleteActivity(id);     // then delete activity
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Failed to delete activity",
                    "error", e.getMessage()
            ));
        }
    }

    // ✅ Get activity by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get a single activity by ID", description = "Retrieve details of a specific activity by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    public ResponseEntity<Activities> getActivityById(@PathVariable Long id) {
        Activities activity = activityService.findById(id);
        return activity != null
                ? ResponseEntity.ok(activity)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ✅ Book an activity
    @PostMapping("/{activityId}/book")
    @Operation(summary = "Book an activity", description = "Book an activity for a user with a specified number of participants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> bookActivity(
            @PathVariable long activityId,
            @RequestBody BookingRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        Map<String, Object> response = new HashMap<>();
        Activities activity = activityService.findById(activityId);
        if (activity == null) {
            response.put("message", "Activity not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (activity.getEndDatetime().isBefore(LocalDateTime.now())) {
            response.put("message", "This activity has already ended. Booking is not allowed.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Users user = userService.findByEmail(principal.getName());
        activityService.updateStatusIfCanceled(activity);

        if (bookingService.hasUserAlreadyBooked(activity.getId(), user.getId())) {
            response.put("message", "You already booked this activity");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        int currentBooked = bookingService.countParticipantsByActivityId(activity.getId());
        if (currentBooked + request.getParticipants() > activity.getMaxParticipants()) {
            response.put("message", "Booking exceeds maximum allowed participants");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalPrice = activity.getPrice().multiply(BigDecimal.valueOf(request.getParticipants()));
        ActivityBookings booking = new ActivityBookings(activity, user, request.getParticipants(), totalPrice, request.getPaymentMethod());
        bookingService.saveBooking(booking);

        response.put("message", "Booking successful");
        response.put("bookingId", booking.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @GetMapping("/interest-based/{userId}")
    @Operation(summary = "Get activities by user's interests")
    public ResponseEntity<?> getActivitiesByUserInterests(@PathVariable Long userId) {
        List<Activities> activities = activityService.findActivitiesByUserInterests(userId);
        if (activities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("message", "No activities found for user's interests")
            );
        }
        return ResponseEntity.ok(activities);
    }

}
