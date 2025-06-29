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
            @ApiResponse(responseCode = "200", description = "List of bookings for the current user"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings")
    public List<ActivityBookings> getMyBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserId(user.getId());
    }

    @Operation(summary = "Get bookings with status = Pending")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of pending bookings"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings/pending")
    public List<ActivityBookings> getPendingBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserIdAndStatuses(user.getId(), List.of("Pending"));
    }

    @Operation(summary = "Get bookings with status = Completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of completed bookings"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings/completed")
    public List<ActivityBookings> getCompletedBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserIdAndStatuses(user.getId(), List.of("Completed"));
    }

    @Operation(summary = "Get bookings with status = Canceled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of canceled bookings"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings/canceled")
    public List<ActivityBookings> getCanceledBookings(@RequestHeader("Authorization") String token) {
        String identifier = extractUserIdentifier(token);
        Users user = userService.getUserByEmaill(identifier);
        return bookingService.getBookingsByUserIdAndStatuses(user.getId(), List.of("Canceled"));
    }

    @Operation(summary = "Cancel a booking by ID (only for current user)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking canceled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
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
            @ApiResponse(responseCode = "200", description = "Booking set to pending successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
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
            @ApiResponse(responseCode = "200", description = "Canceled booking deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token, or booking not canceled"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
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

    @Operation(summary = "Create a new booking for an activity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking data")
    })
    @PostMapping
    public ActivityBookings createBooking(@RequestBody ActivityBookings booking) {
        return bookingService.saveBooking(booking);
    }

    @Operation(summary = "Get all bookings for the logged-in business")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of bookings for business activities"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })

@GetMapping("/mybusinessbookings")
public ResponseEntity<List<ActivityBookings>> getBookingsByBusiness(@RequestHeader("Authorization") String token) {
    String identifier = extractUserIdentifier(token); // usually email

    Businesses business = null;

    // Option 1: Try to get as business (works if login is business)
    try {
        business = businessService.getByEmailOrThrow(identifier);
    } catch (RuntimeException ex) {
        // Option 2: Try as manager (if not found as business)
        // You need a Manager/ AdminUsers service for this!
        Optional<AdminUsers> managerOpt = adminUserService.findByEmail(identifier);
        if (managerOpt.isPresent() && managerOpt.get().getBusiness() != null) {
            business = managerOpt.get().getBusiness();
        }
    }

    // Still no business? Error!
    if (business == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null); // Or: .body(List.of());
    }

    // Now you have the business, fetch all bookings for this business
    List<ActivityBookings> bookings = bookingService.getBookingsByBusinessEmail(business.getEmail());
    return ResponseEntity.ok(bookings);
}


    @PutMapping("/booking/reject/{bookingId}")
    @Operation(summary = "Reject a booking", description = "Mark a specific booking as 'Rejected'. Only accessible to business accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking rejected successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Map<String, String>> rejectBooking(
            @Parameter(description = "ID of the booking to reject") @PathVariable Long bookingId,
            @RequestHeader("Authorization") String tokenHeader) {
        try {
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = tokenHeader.substring(7).trim();
            if (!jwtUtil.isBusinessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Only business accounts can perform this action"));
            }

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
    @Operation(summary = "Unreject a booking", description = "Mark a previously rejected booking as 'Pending'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking status set to pending"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token"),
            @ApiResponse(responseCode = "404", description = "Booking not found or not rejected")
    })
    public ResponseEntity<Map<String, String>> unrejectBooking(
            @Parameter(description = "ID of the booking to unreject") @PathVariable Long bookingId,
            @RequestHeader("Authorization") String tokenHeader) {
        try {
            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid Authorization header"));
            }

            String token = tokenHeader.substring(7).trim();
            if (!jwtUtil.isBusinessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Only business accounts can perform this action"));
            }

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
