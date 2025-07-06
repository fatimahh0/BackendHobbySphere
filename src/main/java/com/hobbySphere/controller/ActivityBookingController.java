package com.hobbySphere.controller;

import com.hobbySphere.dto.BookingDTO;
import com.hobbySphere.dto.BookingPriceResponse;
import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.ActivityBookingService;
import com.hobbySphere.services.AdminUserService;
import com.hobbySphere.services.BusinessService;
import com.hobbySphere.services.UserService;
import com.hobbySphere.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Collections;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175"
})
@Tag(name = "Activity Bookings", description = "Operations related to activity reservations")
public class ActivityBookingController {

    @Autowired
private AdminUserService adminUserService;


    @Autowired
    private BusinessService businessService;

    @Autowired
    private ActivityBookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractUserIdentifier(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String jwtToken = token.substring(7);
        if (!jwtToken.contains(".")) {
            throw new IllegalArgumentException("Malformed JWT token");
        }
        return jwtUtil.extractUsername(jwtToken); // email or phone
    }

    @Operation(summary = "Get all bookings of the currently logged-in user")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/mybookings")
    public List<ActivityBookings> getMyBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserId(user.getId());
    }

    @Operation(summary = "Get bookings with status = Pending")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/mybookings/pending")
    public List<ActivityBookings> getPendingBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserIdAndStatuses(user.getId(), List.of("Pending"));
    }

    @Operation(summary = "Get bookings with status = Completed")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/mybookings/completed")
    public List<ActivityBookings> getCompletedBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserIdAndStatuses(user.getId(), List.of("Completed"));
    }

    @Operation(summary = "Get bookings with status = Canceled")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/mybookings/canceled")
    public List<ActivityBookings> getCanceledBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserIdAndStatuses(user.getId(), List.of("Canceled"));
    }

    @Operation(summary = "Cancel a booking by ID (only for current user)")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @PutMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId, @RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        bookingService.cancelBooking(bookingId, user.getId());
        return "Booking canceled successfully.";
    }

    @Operation(summary = "Set a booking to PENDING by ID (only for current user)")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @PutMapping("/pending/{bookingId}")
    public String pendingBooking(@PathVariable Long bookingId, @RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        bookingService.pendingBooking(bookingId, user.getId());
        return "Booking set to pending successfully.";
    }

    @Operation(summary = "Delete a canceled booking by ID (only if status = Canceled)")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @DeleteMapping("/delete/{bookingId}")
    public String deleteCanceledBooking(@PathVariable Long bookingId, @RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        boolean deleted = bookingService.deleteCanceledBookingByIdAndUserId(bookingId, user.getId());
        if (deleted) {
            return "Canceled booking deleted successfully.";
        } else {
            throw new IllegalStateException("Booking must be CANCELED and belong to you.");
        }
    }

    @PostMapping
    @Operation(summary = "Create a new booking for an activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<ActivityBookings> createBooking(
            @RequestBody ActivityBookings booking,
            @RequestHeader("Authorization") String token) {

        // Extract the user identifier (email or phone) from the token
        String identifier = extractUserIdentifier(token);

        // Load user entity
        Users user = userService.getUserByEmaill(identifier);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Associate the user with the booking
        booking.setUser(user);

        // Save booking
        ActivityBookings savedBooking = bookingService.saveBooking(booking);
        return ResponseEntity.ok(savedBooking);
    }

    @Operation(summary = "Get all bookings for the logged-in business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    @GetMapping("/mybusinessbookings")
    public ResponseEntity<List<ActivityBookings>> getBookingsByBusiness(@RequestHeader("Authorization") String authHeader) {
        // Validate header format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extract token string (after "Bearer ")
        String token = authHeader.substring(7);

        // Extract identifier (email or phone) from token using JwtUtil
        String identifier;
        try {
            identifier = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        Businesses business = businessService.findByEmail(identifier);
        if (business == null) {
            // Try as manager/admin user with business association
            Optional<AdminUsers> managerOpt = adminUserService.findByEmail(identifier);
            if (managerOpt.isPresent() && managerOpt.get().getBusiness() != null) {
                business = managerOpt.get().getBusiness();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
            }
        }

        // Fetch bookings for this business by email
        List<ActivityBookings> bookings = bookingService.getBookingsByBusinessEmail(business.getEmail());

        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/booking/reject/{bookingId}")
    @Operation(
        summary = "Reject a booking",
        description = "Mark a specific booking as 'Rejected'. Accessible to business accounts and managers (admin users)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<Map<String, String>> rejectBooking(
            @Parameter(description = "ID of the booking to reject") @PathVariable Long bookingId,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            // Check token format
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            // Extract token
            String token = tokenHeader.substring(7).trim();

            // Validate role: BUSINESS or MANAGER or SUPER_ADMIN
            boolean isBusiness = jwtUtil.isBusinessToken(token);
            boolean isManager = jwtUtil.isAdminToken(token); // will return true for MANAGER or SUPER_ADMIN

            if (!isBusiness && !isManager) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only business accounts or managers can perform this action"));
            }

            // Perform booking rejection
            bookingService.rejectBooking(bookingId);

            return ResponseEntity.ok(Map.of("message", "Booking rejected successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error occurred"));
        }
    }


    @PutMapping("/booking/unreject/{bookingId}")
    @Operation(summary = "Unreject a booking", description = "Mark a previously rejected booking as 'Pending'. Accessible to business accounts and managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking status set to pending"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
            @ApiResponse(responseCode = "404", description = "Booking not found or not rejected"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    public ResponseEntity<Map<String, String>> unrejectBooking(
            @Parameter(description = "ID of the booking to unreject") @PathVariable Long bookingId,
            @RequestHeader("Authorization") String tokenHeader) {

        try {
            // Validate the token format
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            // Extract the token value
            String token = tokenHeader.substring(7).trim();

            // Validate access (must be business or manager)
            boolean isBusiness = jwtUtil.isBusinessToken(token);
            boolean isManager = jwtUtil.isAdminToken(token); // covers SUPER_ADMIN and MANAGER

            if (!isBusiness && !isManager) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only business accounts or managers can perform this action"));
            }

            // Call the service to unreject the booking
            bookingService.unrejectBooking(bookingId);

            return ResponseEntity.ok(Map.of("message", "Booking status changed to pending"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error occurred"));
        }
    }

    }
