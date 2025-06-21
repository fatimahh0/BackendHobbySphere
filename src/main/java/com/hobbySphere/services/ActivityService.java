package com.hobbySphere.services;

import com.hobbySphere.entities.*;
import com.hobbySphere.enums.CurrencyType;
import com.hobbySphere.dto.ActivityPriceResponse;
import com.hobbySphere.dto.ActivitySummaryDTO;
import com.hobbySphere.repositories.*;

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
import java.util.stream.Collectors;

@Service
public class ActivityService {

    @Autowired
    private ActivitiesRepository activityRepository;

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ActivityBookingsRepository activityBookingsRepository;

    @Autowired
    private AppSettingsRepository appSettingsRepository;

    // Create a new activity with image upload
    public Activities createActivityWithImage(
            String activityName,
            Long activityTypeId,
            String description,
            String location,
            Double latitude,
            Double longitude,
            int maxParticipants,
            BigDecimal price,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            String status,
            Long businessId,
            MultipartFile image
    ) throws IOException {

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

        Businesses business = businessService.findById(businessId);
        if (business == null) {
            throw new IllegalArgumentException("Business with ID " + businessId + " not found.");
        }

        ActivityType type = activityTypeRepository.findById(activityTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activity type"));

        Currency defaultCurrency = currencyRepository.findByCurrencyType(CurrencyType.CAD).orElseThrow();

        Activities activity = new Activities();
        activity.setActivityName(activityName);
        activity.setActivityType(type);
        activity.setDescription(description);
        activity.setLocation(location);
        activity.setLatitude(latitude);
        activity.setLongitude(longitude);
        activity.setMaxParticipants(maxParticipants);
        activity.setPrice(price);
        activity.setStartDatetime(startDatetime);
        activity.setEndDatetime(endDatetime);
        activity.setStatus(status);
        activity.setImageUrl(imageUrl);
        activity.setBusiness(business);
        activity.setCurrency(defaultCurrency);

        return activityRepository.save(activity);
    }

    public Activities updateActivityWithImage(
            Long id,
            String activityName,
            Long activityTypeId,
            String description,
            String location,
            Double latitude,
            Double longitude,
            int maxParticipants,
            BigDecimal price,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            String status,
            Long businessId,
            MultipartFile image,
            boolean imageRemoved) throws IOException {

        Activities activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        ActivityType type = activityTypeRepository.findById(activityTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activity type"));

        activity.setActivityName(activityName);
        activity.setActivityType(type);
        activity.setDescription(description);
        activity.setLocation(location);
        activity.setLatitude(latitude);
        activity.setLongitude(longitude);
        activity.setMaxParticipants(maxParticipants);
        activity.setPrice(price);
        activity.setStartDatetime(startDatetime);
        activity.setEndDatetime(endDatetime);
        activity.setStatus(status);

        Businesses business = businessService.findById(businessId);
        if (business == null) {
            throw new IllegalArgumentException("Business with ID " + businessId + " not found.");
        }
        activity.setBusiness(business);

        if (imageRemoved && activity.getImageUrl() != null) {
            Path oldImagePath = Paths.get("uploads/", Paths.get(activity.getImageUrl()).getFileName().toString());
            Files.deleteIfExists(oldImagePath);
            activity.setImageUrl(null);
        }

        if (image != null && !image.isEmpty()) {
            String imageFileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
            Path imagePath = Paths.get("uploads/", imageFileName);
            Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            activity.setImageUrl("/uploads/" + imageFileName);
        }

        return activityRepository.save(activity);
    }

    public List<Activities> findByBusinessId(Long businessId) {
        return activityRepository.findByBusinessId(businessId);
    }

    public Activities save(Activities activity) {
        return activityRepository.save(activity);
    }

    public Activities findById(Long id) {
        return activityRepository.findById(id).orElse(null);
    }

    public List<Activities> findAllActivities() {
        return activityRepository.findAllPublicActiveBusinessActivities();
    }

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

    @Transactional
    public void rejectActivity(Long activityId) {
        Activities activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with ID: " + activityId));

        activity.setStatus("Rejected");
        activityRepository.save(activity);

        activityBookingsRepository.deleteByActivity_Id(activityId);
    }

    public List<ActivitySummaryDTO> getActivitySummariesByAdmin(AdminUsers admin) {
        Long businessId = admin.getRole().getName().equalsIgnoreCase("ADMIN")
                ? admin.getRole().getId()
                : null;

        if (businessId == null) {
            throw new RuntimeException("Admin user is not associated with a business.");
        }

        List<Activities> activities = activityRepository.findByBusinessId(businessId);

        return activities.stream().map(activity -> {
            String businessName = activity.getBusiness().getBusinessName();
            int participants = activityBookingsRepository.countByActivityId(activity.getId());
            return new ActivitySummaryDTO(
                    activity.getId(),
                    activity.getActivityName(),
                    businessName,
                    activity.getStartDatetime(),
                    participants
            );
        }).collect(Collectors.toList());
    }

    private Currency getDefaultCurrencyIfNull(Currency currency) {
        if (currency != null) return currency;
        return currencyRepository.findByCurrencyType(CurrencyType.CAD)
                .orElseThrow(() -> new RuntimeException("Default currency not found"));
    }

    public List<Activities> findActivitiesByUserInterests(Long userId) {
        return activityRepository.findAllByUserInterests(userId);
    }

    public List<ActivityPriceResponse> getActivitiesWithCurrencySymbol() {
        Currency selectedCurrency = appSettingsRepository.findById(1L)
            .map(AppSettings::getCurrency)
            .orElseThrow(() -> new RuntimeException("Currency not set in app settings"));

        return activityRepository.findAll().stream()
            .map(activity -> new ActivityPriceResponse(
                activity.getId(),
                activity.getActivityName(),
                activity.getPrice(),
                selectedCurrency.getSymbol()
            ))
            .collect(Collectors.toList());
    }

    public List<Activities> getAllVisibleActivitiesForUsers() {
        return activityRepository.findAllPublicActiveBusinessActivities();
    }
}