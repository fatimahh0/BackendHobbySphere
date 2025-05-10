package com.hobbySphere.services;
import com.hobbySphere.repositories.ActivityBookingsRepository;
import com.hobbySphere.entities.ActivityBookings;
import com.hobbySphere.entities.Activities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityBookingService {

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;
    
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
}
