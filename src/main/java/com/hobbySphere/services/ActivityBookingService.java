package com.hobbySphere.services;

import com.hobbySphere.repositories.ActivityBookingsRepository;
import com.hobbySphere.repositories.UsersRepository;
import com.hobbySphere.entities.ActivityBookings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityBookingService {

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;

    @Autowired
    private UsersRepository userRepository;

    // Method to get bookings by user email
    public List<ActivityBookings> getBookingsByEmail(String userEmail) {
        return activityBookingsRepository.findByUserEmail(userEmail);
    }

    public boolean hasUserAlreadyBooked(Long activityId, Long userId) {
        return activityBookingsRepository.existsByActivityIdAndUserId(activityId, userId);
    }

    // Method to get bookings by user email and statuses (e.g., Pending, Completed, Canceled)
    public List<ActivityBookings> getBookingsByEmailAndStatuses(String userEmail, List<String> statuses) {
        return activityBookingsRepository.findByUserEmailAndBookingStatusIn(userEmail, statuses);
    }

    // Method to cancel a booking by ID (only for the current user)
    public void cancelBooking(Long bookingId, String userEmail) {
        ActivityBookings booking = activityBookingsRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only cancel your own bookings");
        }
        
        booking.setBookingStatus("Canceled");
        activityBookingsRepository.save(booking);
    }

    // Method to set a booking to Pending (only for the current user)
    public void pendingBooking(Long bookingId, String userEmail) {
        ActivityBookings booking = activityBookingsRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only set your own bookings to pending");
        }
        
        booking.setBookingStatus("Pending");
        activityBookingsRepository.save(booking);
    }

    // Method to save a new booking
    public ActivityBookings saveBooking(ActivityBookings booking) {
        return activityBookingsRepository.save(booking);  // Persist the booking to the database
    }

    // Count the number of participants for a specific activity
    public int countParticipantsByActivityId(Long activityId) {
        return activityBookingsRepository.sumParticipantsByActivityId(activityId);
    }
}
