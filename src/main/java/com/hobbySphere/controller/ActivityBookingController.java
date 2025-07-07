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
import com.hobbySphere.services.StripeService;
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
    private StripeService stripeService;


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

    @Operation(summary = "Delete a canceled or rejected booking by ID (with refund if paid)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    @DeleteMapping("/delete/{bookingId}")
    public ResponseEntity<?> deleteCanceledOrRejectedBooking(@PathVariable Long bookingId,
                                                             @RequestHeader("Authorization") String token) {
        try {
            String identifier = extractUserIdentifier(token);
            Users user = userService.getUserByEmaill(identifier);

            ActivityBookings booking = bookingService.getBookingById(bookingId);
            if (booking == null || !booking.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only delete your own booking"));
            }

            String status = booking.getBookingStatus();
            if (!"Canceled".equalsIgnoreCase(status) && !"Rejected".equalsIgnoreCase(status)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only canceled or rejected bookings can be deleted"));
            }

           
            if (booking.getWasPaid()) {
                bookingService.deleteBookingById(bookingId);
                return ResponseEntity.ok(Map.of(
                    "message", "Booking deleted and payment was previously completed. Pretend refund confirmed.",
                    "pretendRefund", true
                ));
            } else {
                bookingService.deleteBookingById(bookingId);
                return ResponseEntity.ok(Map.of(
                    "message", "Booking deleted (no payment to refund)",
                    "pretendRefund", false
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
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
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authHeader.substring(7);

        // ✅ Only allow BUSINESS or MANAGER tokens
        if (!(jwtUtil.isBusinessToken(token) || jwtUtil.isAdminToken(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        }

        String identifier;
        try {
            identifier = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        Businesses business = null;
        // USE findByEmailOptional FIRST!
        Optional<Businesses> businessOpt = businessService.findByEmailOptional(identifier);
        if (businessOpt.isPresent()) {
            business = businessOpt.get();
        } else {
            // Try manager
            Optional<AdminUsers> managerOpt = adminUserService.findByEmail(identifier);
            if (managerOpt.isPresent() && managerOpt.get().getBusiness() != null) {
                business = managerOpt.get().getBusiness();
            } else {
                // DO NOT THROW! Just return empty or error
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
            }
        }

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
