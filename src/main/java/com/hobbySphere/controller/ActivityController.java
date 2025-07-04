package com.hobbySphere.controller;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.Users;
import com.hobbySphere.repositories.CurrencyRepository;
import com.hobbySphere.security.JwtUtil;
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

import com.hobbySphere.dto.ActivityPriceResponse;
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
	private JwtUtil jwtUtil;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityBookingService bookingService;

    @Autowired
    private UserService userService;

    // ✅ Get all activities for a business (with auto status update)
    
    @Operation(summary = "Get activities by business ID", description = "Retrieve a list of activities associated with a specific business and auto-update expired ones. Accessible to the business owner or admin users.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> getActivitiesByBusiness(
            @Parameter(description = "ID of the business to fetch activities for") @PathVariable Long businessId,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            // Validate token format
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = tokenHeader.substring(7).trim();

            // Check role
            boolean isBusiness = jwtUtil.isBusinessToken(token);
            boolean isAdmin = jwtUtil.isAdminToken(token);

            if (!isBusiness && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Only business owners or managers can access this resource."));
            }

            // If business, ensure the business ID matches
            if (isBusiness) {
                Long tokenBusinessId = jwtUtil.extractId(token);
                if (!tokenBusinessId.equals(businessId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You do not have permission to view activities of another business."));
                }
            }

            // Fetch activities
            List<Activities> activities = activityService.findByBusinessId(businessId);
            if (activities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No activities found for this business"));
            }

            // Auto-update status if expired
            LocalDateTime now = LocalDateTime.now();
            for (Activities activity : activities) {
                if (activity.getEndDatetime().isBefore(now) && !"Terminated".equalsIgnoreCase(activity.getStatus())) {
                    activity.setStatus("Terminated");
                    activityService.save(activity);
                }
            }

            return ResponseEntity.ok(activities);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error occurred"));
        }
    }

    // ✅ Get all activities
    @GetMapping
    @Operation(summary = "Get all activities", description = "Retrieve a list of all activities in the system and auto-update expired statuses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
            @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> getAllActivities() {

        List<Activities> allActivities = activityService.findAllActivities();
        LocalDateTime now = LocalDateTime.now();

        for (Activities activity : allActivities) {
            if (activity.getEndDatetime().isBefore(now) && !"Terminated".equalsIgnoreCase(activity.getStatus())) {
                activity.setStatus("Terminated");
                activityService.save(activity);
            }
        }

        return ResponseEntity.ok(allActivities);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming activities", description = "Retrieve all activities that are not yet expired")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> getUpcomingActivities() {
        try {
            List<Activities> upcoming = activityService.findAllActivities().stream()
                    .filter(a -> a.getEndDatetime().isAfter(LocalDateTime.now()))
                    .toList();

            return ResponseEntity.ok(upcoming);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch upcoming activities"));
        }
    }


 // ✅ Get terminated activities
    @GetMapping("/terminated")
    @Operation(summary = "Get terminated activities", description = "Retrieve all activities that have ended")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
            @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> getTerminatedActivities(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing token"));
        }

        String token = tokenHeader.substring(7).trim();
        String role = jwtUtil.extractRole(token);

        if (!List.of("USER", "BUSINESS", "MANAGER", "SUPER_ADMIN").contains(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
        }

        List<Activities> terminated = activityService.findAllActivities().stream()
                .filter(a -> a.getEndDatetime().isBefore(LocalDateTime.now()))
                .peek(a -> {
                    if (!"Terminated".equalsIgnoreCase(a.getStatus())) {
                        a.setStatus("Terminated");
                        activityService.save(a);
                    }
                }).toList();

        return ResponseEntity.ok(terminated);
    }


    @PostMapping(value = "/create", consumes = "multipart/form-data")
    @Operation(summary = "Create a new activity with image upload", description = "Create a new activity and optionally upload an image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
            @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> createActivityWithImage(
            @RequestHeader("Authorization") String token,  // 👈 add this line
            @RequestParam("activityName") String activityName,
            @RequestParam("activityTypeId") Long activityTypeId,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("maxParticipants") int maxParticipants,
            @RequestParam("price") BigDecimal price,
            @RequestParam("startDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDatetime,
            @RequestParam("endDatetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDatetime,
            @RequestParam("status") String status,
            @RequestParam("businessId") Long businessId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            // ✅ Strip "Bearer " and validate token role
            token = token.replace("Bearer ", "").trim();
            String role = jwtUtil.extractRole(token);
            boolean isAllowed = "BUSINESS".equals(role) || "MANAGER".equals(role);
            if (!isAllowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied. Business or Manager role required."));
            }

            // ✅ Continue with existing logic
            Activities activity = activityService.createActivityWithImage(
                    activityName, activityTypeId, description, location, latitude, longitude, maxParticipants, price,
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
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
            @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> updateActivityWithImage(
            @RequestHeader("Authorization") String token, // 👈 added
            @PathVariable Long id,
            @RequestParam("activityName") String activityName,
            @RequestParam("activityTypeId") Long activityTypeId,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
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
            // ✅ Token validation for BUSINESS or MANAGER
            token = token.replace("Bearer ", "").trim();
            String role = jwtUtil.extractRole(token);
            boolean isAllowed = "BUSINESS".equals(role) || "MANAGER".equals(role);
            if (!isAllowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied. Business or Manager role required."));
            }

            // ✅ Continue with existing logic
            Activities updatedActivity = activityService.updateActivityWithImage(
                    id, activityName, activityTypeId, description, location, latitude, longitude, maxParticipants, price,
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



    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an activity by ID", description = "Delete an activity by providing its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
            @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> deleteActivity(
            @RequestHeader("Authorization") String token,  // 👈 Added
            @PathVariable Long id
    ) {
        try {
            // ✅ Token validation for BUSINESS role
            token = token.replace("Bearer ", "").trim();
            String role = jwtUtil.extractRole(token);
            if (!"BUSINESS".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied. Only Business users can delete activities."));
            }

            Activities activity = activityService.findById(id);
            if (activity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Activity not found"));
            }

            bookingService.deleteByActivityId(id); // delete bookings
            activityService.deleteActivity(id);    // then delete activity
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Failed to delete activity",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single activity by ID", description = "Retrieve full details of a specific activity including location info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> getActivityById(@PathVariable Long id) {
        try {
            Activities activity = activityService.findById(id);
            if (activity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Activity not found"));
            }

            // Return selected activity fields
            Map<String, Object> response = new HashMap<>();
            response.put("id", activity.getId());
            response.put("name", activity.getActivityName());
            response.put("description", activity.getDescription());
            response.put("activityType", activity.getActivityType().getName());
            response.put("location", activity.getLocation());
            response.put("latitude", activity.getLatitude());
            response.put("longitude", activity.getLongitude());
            response.put("startDatetime", activity.getStartDatetime());
            response.put("endDatetime", activity.getEndDatetime());
            response.put("price", activity.getPrice());
            response.put("status", activity.getStatus());
            response.put("imageUrl", activity.getImageUrl());
            response.put("maxParticipants", activity.getMaxParticipants());
            response.put("businessName", activity.getBusiness().getBusinessName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected server error"));
        }
    }

    // ✅ Book an activity
    @PostMapping("/{activityId}/book")
    @Operation(summary = "Book an activity", description = "Book an activity for a user with a specified number of participants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
            @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> bookActivity(
            @RequestHeader("Authorization") String token, // 👈 Accept user token from header
            @PathVariable long activityId,
            @RequestBody BookingRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 👇 Strip "Bearer " and validate token
            token = token.replace("Bearer ", "").trim();
            if (!jwtUtil.isUserToken(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied. User token required."));
            }

            String userEmail = jwtUtil.extractUsername(token); // 👈 Get email from token
            Users user = userService.getUserByEmaill(userEmail); // 👈 Lookup user by email

            Activities activity = activityService.findById(activityId);
            if (activity == null) {
                response.put("message", "Activity not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if (activity.getEndDatetime().isBefore(LocalDateTime.now())) {
                response.put("message", "This activity has already ended. Booking is not allowed.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

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

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Invalid or missing token",
                    "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/interest-based/{userId}")
    @Operation(summary = "Get pending activities by user's interests", description = "Returns only upcoming (not ended or terminated) activities based on user's interests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
            @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<?> getActivitiesByUserInterests(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId
    ) {
        try {
            // ✅ Validate token and role
            token = token.replace("Bearer ", "").trim();
            if (!jwtUtil.isUserToken(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied. User token required."));
            }

            String emailFromToken = jwtUtil.extractUsername(token);
            Users user = userService.getUserByEmaill(emailFromToken);

            // ✅ Check that the token user matches the requested userId
            if (!user.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Token does not match requested user ID")
                );
            }

            List<Activities> activities = activityService.findActivitiesByUserInterests(userId);
            LocalDateTime now = LocalDateTime.now();

            List<Activities> pendingActivities = activities.stream()
                .filter(a -> a.getEndDatetime().isAfter(now)) // Not ended
                .filter(a -> !"Terminated".equalsIgnoreCase(a.getStatus())) // Not terminated
                .toList();

            if (pendingActivities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "No pending activities found for user's interests")
                );
            }

            return ResponseEntity.ok(pendingActivities);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Invalid or missing token",
                    "error", e.getMessage()
            ));
        }
    }


}
