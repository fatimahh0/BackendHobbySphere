package com.hobbySphere.services;
import com.hobbySphere.repositories.ActivityBookingsRepository;
import com.hobbySphere.repositories.UsersRepository;
import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.Users;
import com.hobbySphere.entities.Activities;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityBookingService {

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;
    

    @Autowired
    private UsersRepository userRepository; 
    
    public int countParticipantsByActivityId(Long activityId) {
        return activityBookingsRepository.countByActivityId(activityId);
    }

    public void validateBooking(Activities activity, int numberOfParticipants) {
        // Get the current number of participants already booked for the activity
        int totalParticipants = activityBookingsRepository.sumParticipantsByActivityId(activity.getId());

        // Check if the new booking exceeds the maximum participants
        if (totalParticipants + numberOfParticipants > activity.getMaxParticipants()) {
            throw new IllegalArgumentException("Cannot book more participants than the maximum allowed for this activity.");
        }
    }

    public void save(ActivityBookings booking) {
        activityBookingsRepository.save(booking);
    }
    
   

  
    
    public ActivityBookings saveBooking(ActivityBookings booking) {
        return activityBookingsRepository.save(booking);
    }

	
	public List<ActivityBookings> getBookingsByUserId(Long userId) {
		return activityBookingsRepository.findByUser_Id(userId);
		}

	
	public List<ActivityBookings> getBookingsByEmail(String email) {
	    Users user = userRepository.findByEmail(email);
	    if (user == null) {
	        throw new RuntimeException("User not found");
	    }
	    return activityBookingsRepository.findByUserId(user.getId());
	}

	
	
	
public List<ActivityBookings> getBookingsByEmailAndStatuses(String userEmail, List<String> statuses) {
    List<ActivityBookings> bookings = activityBookingsRepository.findByUserEmailAndBookingStatusIn(userEmail, statuses);

    LocalDateTime now = LocalDateTime.now();

    for (ActivityBookings booking : bookings) {
        if ("pending".equals(booking.getBookingStatus()) &&
            booking.getActivity().getEndDatetime().isBefore(now)) {
            booking.setBookingStatus("completed");
            activityBookingsRepository.save(booking); // update status in DB
        }
    }

    // Fetch all updated bookings with the given statuses (including Completed ones now)
    return activityBookingsRepository.findByUserEmailAndBookingStatusIn(userEmail, statuses);
}

public void cancelBooking(Long bookingId, String userEmail) {
    ActivityBookings booking = activityBookingsRepository.findById(bookingId)
        .orElseThrow(() -> new RuntimeException("Booking not found"));

    if (!booking.getUser().getEmail().equals(userEmail)) {
        throw new RuntimeException("Unauthorized to cancel this booking");
    }

    if ("Canceled".equals(booking.getBookingStatus())) {
        throw new RuntimeException("Booking is already canceled");
    }

    booking.setBookingStatus("Canceled");
    activityBookingsRepository.save(booking);
}



public boolean existsByUserAndActivity(Long id, long activityId) {
	return activityBookingsRepository.existsByUserIdAndActivityId(id, activityId);
}


	

	


    

    
}
