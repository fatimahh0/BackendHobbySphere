package com.hobbySphere.config;

import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.entities.Interests;
import com.hobbySphere.enums.ActivityTypeEnum;
import com.hobbySphere.repositories.ActivityTypeRepository;
import com.hobbySphere.repositories.InterestsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ActivityTypeSeeder {

    private final ActivityTypeRepository activityTypeRepository;
    private final InterestsRepository interestsRepository;

    public ActivityTypeSeeder(ActivityTypeRepository activityTypeRepository,
                              InterestsRepository interestsRepository) {
        this.activityTypeRepository = activityTypeRepository;
        this.interestsRepository = interestsRepository;
    }

    @PostConstruct
    public void seedActivityTypes() {
        for (ActivityTypeEnum enumValue : ActivityTypeEnum.values()) {
            String activityTypeName = enumValue.name();

            // Only insert ActivityType if not already in DB
            if (activityTypeRepository.findByName(activityTypeName).isEmpty()) {

                // Get the corresponding Interest name from the enum
                String interestName = enumValue.getInterest().name();

                // Find or create the Interest entity
                Interests interestEntity = interestsRepository.findByName(interestName)
                        .orElseGet(() -> interestsRepository.save(new Interests(interestName)));

                // Create and save the new ActivityType
                ActivityType newType = new ActivityType(activityTypeName, interestEntity);
                activityTypeRepository.save(newType);
            }
        }
    }
}
