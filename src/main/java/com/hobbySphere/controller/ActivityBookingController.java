package com.hobbySphere.controller;

import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.services.ActivityBookingService;
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
    private ActivityBookingService bookingService;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractUserEmail(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }

        String jwtToken = token.substring(7);
        if (!jwtToken.contains(".")) {
            throw new IllegalArgumentException("Malformed JWT token");
        }

        return jwtUtil.extractUsername(jwtToken);
    }

    @Operation(summary = "Get all bookings of the currently logged-in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of bookings for the current user"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings")
    public List<ActivityBookings> getMyBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmail(userEmail);
    }
    
    @Operation(summary = "Get all bookings for the logged-in business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of bookings for business activities"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybusinessbookings")
    public ResponseEntity<List<ActivityBookings>> getBookingsByBusiness(
            @RequestHeader("Authorization") String token) {
        String businessEmail = extractUserEmail(token);
        List<ActivityBookings> bookings = bookingService.getBookingsByBusinessEmail(businessEmail);
        return ResponseEntity.ok(bookings);
    }


    @Operation(summary = "Get bookings with status = Pending")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of pending bookings"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings/pending")
    public List<ActivityBookings> getPendingBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmailAndStatuses(userEmail, List.of("Pending"));
    }

    @Operation(summary = "Get bookings with status = Completed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of completed bookings"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings/completed")
    public List<ActivityBookings> getCompletedBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmailAndStatuses(userEmail, List.of("Completed"));
    }

    @Operation(summary = "Get bookings with status = Canceled")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of canceled bookings"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    @GetMapping("/mybookings/canceled")
    public List<ActivityBookings> getCanceledBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmailAndStatuses(userEmail, List.of("Canceled"));
    }

    @Operation(summary = "Cancel a booking by ID (only for current user)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking canceled successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId,
                                @RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        bookingService.cancelBooking(bookingId, userEmail);
        return "Booking canceled successfully.";
    }
    
    ///
    @PutMapping("/booking/reject/{bookingId}")
    @Operation(summary = "Reject a booking", description = "Mark a specific booking as 'Rejected'")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking rejected successfully"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Map<String, String>> rejectBooking(
            @Parameter(description = "ID of the booking to reject") @PathVariable Long bookingId) {
        try {
            bookingService.rejectBooking(bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking rejected successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error occurred"));
        }
    }
    
    @PutMapping("/booking/unreject/{bookingId}")
    @Operation(summary = "Unreject a booking", description = "Mark a previously rejected booking as 'Pending'")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking status set to pending"),
        @ApiResponse(responseCode = "404", description = "Booking not found or not rejected")
    })
    public ResponseEntity<Map<String, String>> unrejectBooking(
            @PathVariable Long bookingId) {
        try {
            bookingService.unrejectBooking(bookingId); // ✅ service method must exist
            return ResponseEntity.ok(Map.of("message", "Booking status changed to pending"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error occurred"));
        }
    }


    @Operation(summary = "Set a booking to PENDING by ID (only for current user)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking set to pending successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/pending/{bookingId}")
    public String pendingBooking(@PathVariable Long bookingId,
                                 @RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        bookingService.pendingBooking(bookingId, userEmail);
        return "Booking set to pending successfully.";
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
    
    @Operation(summary = "Delete a canceled booking by ID (only if status = Canceled)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Canceled booking deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token, or booking not canceled"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @DeleteMapping("/delete/{bookingId}")
    public String deleteCanceledBooking(@PathVariable Long bookingId,
                                        @RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        boolean deleted = bookingService.deleteCanceledBookingByIdAndEmail(bookingId, userEmail);
        if (deleted) {
            return "Canceled booking deleted successfully.";
        } else {
            throw new IllegalStateException("Booking must be CANCELED and belong to you.");
        }
    }

}
