package com.hobbySphere.controller;

import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.services.ActivityBookingService;
import com.hobbySphere.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
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
    @GetMapping("/mybookings")
    public List<ActivityBookings> getMyBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmail(userEmail);
    }

    @Operation(summary = "Get bookings with status = Pending")
    @GetMapping("/mybookings/pending")
    public List<ActivityBookings> getPendingBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmailAndStatuses(userEmail, List.of("Pending"));
    }

    @Operation(summary = "Get bookings with status = Completed")
    @GetMapping("/mybookings/completed")
    public List<ActivityBookings> getCompletedBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmailAndStatuses(userEmail, List.of("Completed"));
    }

    @Operation(summary = "Get bookings with status = Canceled")
    @GetMapping("/mybookings/canceled")
    public List<ActivityBookings> getCanceledBookings(@RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        return bookingService.getBookingsByEmailAndStatuses(userEmail, List.of("Canceled"));
    }
    
    @Operation(summary = "Cancel a booking by ID (only for current user)")
    @PutMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId,
                                @RequestHeader("Authorization") String token) {
        String userEmail = extractUserEmail(token);
        bookingService.cancelBooking(bookingId, userEmail);
        return "Booking canceled successfully.";
    }

    
   


    @Operation(summary = "Create a new booking for an activity")
    @PostMapping
    public ActivityBookings createBooking(@RequestBody ActivityBookings booking) {
        return bookingService.saveBooking(booking);
    }
}
