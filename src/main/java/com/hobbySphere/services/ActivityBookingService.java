package com.hobbySphere.services;
import com.hobbySphere.enums.*;

import com.hobbySphere.repositories.ActivityBookingsRepository;
import com.hobbySphere.repositories.AppSettingsRepository;
import com.hobbySphere.repositories.CurrencyRepository;
import com.hobbySphere.repositories.UsersRepository;

import jakarta.transaction.Transactional;

import com.hobbySphere.dto.BookingDTO;
import com.hobbySphere.dto.BookingPriceResponse;
import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.AppSettings;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.entities.Users;
import com.hobbySphere.enums.CurrencyType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ActivityBookingService {

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;

    @Autowired
    private UsersRepository userRepository;
    
    @Autowired
    private NotificationsService notificationsService;


    // Method to get bookings by user email
    
    public List<ActivityBookings> getBookingsByUserId(Long userId) {
        List<ActivityBookings> bookings = activityBookingsRepository.findByUserIdAndActiveBusiness(userId);
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
    public List<ActivityBookings> getBookingsByUserIdAndStatuses(Long userId, List<String> statuses) {
        List<ActivityBookings> bookings = activityBookingsRepository.findByUserIdAndStatusesAndActiveBusiness(userId, statuses);
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


    public void cancelBooking(Long bookingId, Long userId) {
        ActivityBookings booking = activityBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only cancel your own bookings");
        }

        booking.setBookingStatus("Canceled");
        activityBookingsRepository.save(booking);

        // ✅ Notify the business
        String message = booking.getUser().getFirstName() + " cancelled their booking for: " + booking.getActivity().getActivityName();
        notificationsService.notifyBusiness(
            booking.getActivity().getBusiness(),
            message,
            NotificationType.BOOKING_CANCELLED
        );

    }

    public void pendingBooking(Long bookingId, Long userId) {
        ActivityBookings booking = activityBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only set your own bookings to pending");
        }

        booking.setBookingStatus("Pending");
        activityBookingsRepository.save(booking);

        // ✅ Notify the business
        String message = booking.getUser().getFirstName() + " returned a booking to pending for: " + booking.getActivity().getActivityName();
        notificationsService.notifyBusiness(
            booking.getActivity().getBusiness(),
            message,
            NotificationType.BOOKING_PENDING
        );

    }

    public boolean deleteCanceledBookingByIdAndUserId(Long bookingId, Long userId) {
        ActivityBookings booking = activityBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: booking does not belong to user");
        }

        if (!"Canceled".equalsIgnoreCase(booking.getBookingStatus())) {
            return false;
        }

        activityBookingsRepository.delete(booking);
        return true;
    }

    // Method to save a new booking
    public ActivityBookings saveBooking(ActivityBookings booking) {
        ActivityBookings savedBooking = activityBookingsRepository.save(booking);

        Users user = savedBooking.getUser(); // the user who made the booking
        String userName = user.getFirstName(); // or use getUsername()

        String activityName = savedBooking.getActivity().getActivityName();
        String message = userName + " booked your activity: " + activityName;

        notificationsService.notifyBusiness(
            savedBooking.getActivity().getBusiness(),
            message,
            NotificationType.BOOKING_CREATED
        );

        return savedBooking;
    }


    // Count the number of participants for a specific activity
    public int countParticipantsByActivityId(Long activityId) {
        return activityBookingsRepository.sumParticipantsByActivityId(activityId);
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

	public void deleteByActivityId(Long activityId) {
		activityBookingsRepository.deleteByActivityId(activityId);
	}

	@Autowired
	private AppSettingsRepository appSettingsRepository;

	@Transactional
	public List<BookingPriceResponse> getBookingsWithCurrencySymbol(String userEmail) {
	    Currency selectedCurrency = appSettingsRepository.findById(1L)
	        .map(AppSettings::getCurrency)
	        .orElseThrow(() -> new RuntimeException("Currency not set in app settings"));

	    return activityBookingsRepository.findByUserEmailWithActivity(userEmail).stream()
	        .map(booking -> {
	            Activities activity = booking.getActivity();
	            if (activity == null) {
	                throw new RuntimeException("Activity not found for booking ID: " + booking.getId());
	            }

	            return new BookingPriceResponse(
	                booking.getId(),
	                activity.getActivityName(),
	                booking.getNumberOfParticipants(),
	                booking.getTotalPrice(),
	                selectedCurrency.getSymbol()
	            );
	        })
	        .collect(Collectors.toList());
	}

	public List<ActivityBookings> getAllBookings() {
	    return activityBookingsRepository.findAll(); // assuming JPA repository is injected
	}

	public List<BookingDTO> getAllBookingsAsDTO() {
	    Currency selectedCurrency = appSettingsRepository.findById(1L)
	        .map(AppSettings::getCurrency)
	        .orElseThrow(() -> new RuntimeException("Currency not set in app settings"));

	    return activityBookingsRepository.findAllWithActivityAndUser().stream()
	        .map(b -> new BookingDTO(
	        	    b.getId(),
	        	    b.getActivity().getActivityName(),
	        	    b.getUser().getEmail(),
	        	    b.getNumberOfParticipants(),
	        	    b.getTotalPrice().doubleValue(), 
	        	    selectedCurrency.getSymbol()// ✅ Fix here
	        	))
	        .collect(Collectors.toList());
	}

	public boolean updateVisibilityAndStatus(Long userId, boolean isPublicProfile, UserStatus newStatus) {
	    Users user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

	    user.setIsPublicProfile(isPublicProfile);
	    user.setStatus(newStatus);
	    user.setUpdatedAt(LocalDateTime.now());

	    userRepository.save(user);
	    return true;
	}

}
