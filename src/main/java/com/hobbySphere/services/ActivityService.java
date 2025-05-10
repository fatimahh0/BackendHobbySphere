package com.hobbySphere.services;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.repositories.ActivitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    @Autowired
    private ActivitiesRepository activityRepository;

    // Method to get all activities by business ID
    public List<Activities> findByBusinessId(Long businessId) {
        return activityRepository.findByBusinessId(businessId);
    }

    // Method to save a new activity or update an existing activity
    public Activities save(Activities activity) {
        return activityRepository.save(activity);
    }

    // Method to find an activity by ID
    public Activities findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    // Method to get all activities
    public List<Activities> findAllActivities() {
        return activityRepository.findAll();
    }

    // Method to delete an activity by ID
    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }

    // other methods like save(), etc.
}
