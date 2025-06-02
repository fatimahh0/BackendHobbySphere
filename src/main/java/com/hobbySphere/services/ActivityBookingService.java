package com.hobbySphere.services;
import com.hobbySphere.enums.*;

import com.hobbySphere.repositories.ActivityBookingsRepository;
import com.hobbySphere.repositories.CurrencyRepository;
import com.hobbySphere.repositories.UsersRepository;

import jakarta.transaction.Transactional;

import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.CurrencyType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class ActivityBookingService {

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;

    @Autowired
    private UsersRepository userRepository;
    
    @Autowired
    private NotificationsService notificationsService;


    // Method to get bookings by user email
    
    public List<ActivityBookings> getBookingsByEmail(String userEmail) {
        List<ActivityBookings> bookings = activityBookingsRepository.findByUserEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        for (ActivityBookings booking : bookings) {
            if ("Pending".equalsIgnoreCase(booking.getBookingStatus())
                    && booking.getActivity().getEndDatetime().isBefore(now)) {
                booking.setBookingStatus("Completed");
                activityBookingsRepository.save(booking);
            }
        }

        return bookings;
    }


    public boolean hasUserAlreadyBooked(Long activityId, Long userId) {
        return activityBookingsRepository.existsByActivityIdAndUserId(activityId, userId);
    }

    // Method to get bookings by user email and statuses (e.g., Pending, Completed, Canceled)
    public List<ActivityBookings> getBookingsByEmailAndStatuses(String userEmail, List<String> statuses) {
        List<ActivityBookings> bookings = activityBookingsRepository.findByUserEmailAndBookingStatusIn(userEmail, statuses);
        LocalDateTime now = LocalDateTime.now();

        for (ActivityBookings booking : bookings) {
            if ("Pending".equalsIgnoreCase(booking.getBookingStatus())
                    && booking.getActivity().getEndDatetime().isBefore(now)) {
                booking.setBookingStatus("Completed");
                activityBookingsRepository.save(booking);
            }
        }

        return bookings;
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

    public boolean deleteCanceledBookingByIdAndEmail(Long bookingId, String userEmail) {
        ActivityBookings booking = activityBookingsRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: booking does not belong to user");
        }

        if (!"Canceled".equalsIgnoreCase(booking.getBookingStatus())) {
            return false; // Only allow deletion if status is Canceled
        }

        activityBookingsRepository.delete(booking);
        return true;
    }

    public void rejectBooking(Long bookingId) {
        ActivityBookings booking = activityBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        booking.setBookingStatus("Rejected");
        activityBookingsRepository.save(booking);

        // ✅ Send notification to user
        notificationsService.createNotification(
        	    booking.getUser(),
        	    "Your booking for activity '" + booking.getActivity().getActivityName() + "' has been rejected.",
        	    NotificationType.ACTIVITY_UPDATE
        	);

    }



	public List<ActivityBookings> getBookingsByBusinessEmail(String email) {
	    return activityBookingsRepository.findByActivityBusinessEmail(email);
	}

	public void unrejectBooking(Long bookingId) {
	    ActivityBookings booking = activityBookingsRepository.findById(bookingId)
	            .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

	    if (!"Rejected".equalsIgnoreCase(booking.getBookingStatus())) {
	        throw new IllegalArgumentException("Only bookings with status 'Rejected' can be unrejected.");
	    }

	    booking.setBookingStatus("Pending");
	    activityBookingsRepository.save(booking);

	    // ✅ Send notification to user
	    notificationsService.createNotification(
	        booking.getUser(),
	        "Your booking for activity '" + booking.getActivity().getActivityName() + "' is now pending again.",
	        NotificationType.ACTIVITY_UPDATE
	    );
	}
	
	@Autowired
	private CurrencyRepository currencyRepository;

	private Currency getDefaultCurrencyIfNull(Currency currency) {
	    if (currency != null) return currency;
	    return currencyRepository.findByCurrencyType(CurrencyType.CAD)
	            .orElseThrow(() -> new RuntimeException("Default currency not found"));
	}


}
