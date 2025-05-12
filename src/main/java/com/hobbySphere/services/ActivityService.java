package com.hobbySphere.services;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.repositories.ActivitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}
