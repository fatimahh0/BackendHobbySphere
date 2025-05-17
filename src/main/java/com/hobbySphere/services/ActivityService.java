package com.hobbySphere.services;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.Businesses;

import com.hobbySphere.repositories.ActivitiesRepository;
import com.hobbySphere.repositories.ActivityBookingsRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    @Autowired
    private ActivitiesRepository activityRepository;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;

    // ✅ Create a new activity with image upload
    public Activities createActivityWithImage(
            String activityName,
            String activityType,
            String description,
            String location,
            int maxParticipants,
            BigDecimal price,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            String status,
            Long businessId,
            MultipartFile image
    ) throws IOException {

        // 🔹 Upload image and store relative URL
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            imageUrl = "/uploads/" + filename;
        }

        // 🔹 Fetch related business
        Businesses business = businessService.findById(businessId);
        if (business == null) {
            throw new IllegalArgumentException("Business with ID " + businessId + " not found.");
        }

        // 🔹 Create activity object
        Activities activity = new Activities();
        activity.setActivityName(activityName);
        activity.setActivityType(activityType);
        activity.setDescription(description);
        activity.setLocation(location);
        activity.setMaxParticipants(maxParticipants);
        activity.setPrice(price);
        activity.setStartDatetime(startDatetime);
        activity.setEndDatetime(endDatetime);
        activity.setStatus(status);
        activity.setImageUrl(imageUrl);
        activity.setBusiness(business);

        return activityRepository.save(activity);
    }

    // ✅ Find all activities for a business
    public List<Activities> findByBusinessId(Long businessId) {
        return activityRepository.findByBusinessId(businessId);
    }

    // ✅ Save or update a single activity
    public Activities save(Activities activity) {
        return activityRepository.save(activity);
    }

    // ✅ Find activity by ID
    public Activities findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    // ✅ Find all activities
    public List<Activities> findAllActivities() {
        return activityRepository.findAll();
    }

    // ✅ Delete activity by ID
    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }

    @Transactional
    public void updateStatusIfCanceled(Activities activity) {
        if ("Canceled".equalsIgnoreCase(activity.getStatus())) {
            activity.setStatus("Pending");
            activityRepository.save(activity);
        }
    }

    // ✅ Update an activity with image
    public Activities updateActivityWithImage(
            Long id,
            String activityName,
            String activityType,
            String description,
            String location,
            int maxParticipants,
            BigDecimal price,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            String status,
            Long businessId,
            MultipartFile image) throws IOException {

        Activities activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        activity.setActivityName(activityName);
        activity.setActivityType(activityType);
        activity.setDescription(description);
        activity.setLocation(location);
        activity.setMaxParticipants(maxParticipants);
        activity.setPrice(price);
        activity.setStartDatetime(startDatetime);
        activity.setEndDatetime(endDatetime);
        activity.setStatus(status);

        Businesses business = businessService.findById(businessId);
        if (business != null) {
            activity.setBusiness(business);
        } else {
            throw new IllegalArgumentException("Business with ID " + businessId + " not found.");
        }

        if (image != null && !image.isEmpty()) {
            String imageFileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
            Path imagePath = Paths.get("uploads/", imageFileName);
            Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            activity.setImageUrl("/uploads/" + imageFileName);
        }

        return activityRepository.save(activity);
    }

    // ✅ Reject an activity and delete its bookings
    @Transactional
    public void rejectActivity(Long activityId) {
        Activities activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with ID: " + activityId));

        activity.setStatus("Rejected");
        activityRepository.save(activity);

        activityBookingsRepository.deleteByActivity_Id(activityId);
    }

}
