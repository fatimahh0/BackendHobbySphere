package com.hobbySphere.controller;

import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.ActivityBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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


    @Operation(summary = "Get bookings of the currently logged-in user")
    @GetMapping("/mybookings")
    public List<ActivityBookings> getMyBookings(@RequestHeader("Authorization") String token) {
        // Step 1: Remove "Bearer " prefix from the token
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        // Step 2: Extract email from JWT token
        String userEmail = jwtUtil.extractUsername(jwtToken);

        // Step 3: Call the service to get bookings by user email
        return bookingService.getBookingsByEmail(userEmail);
    }





    @Operation(summary = "Create a new booking for an activity")
    @PostMapping
    public ActivityBookings createBooking(@RequestBody ActivityBookings booking) {
        return bookingService.saveBooking(booking);
    }
}
