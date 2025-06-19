package com.hobbySphere.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.entities.Interests;
import com.hobbySphere.enums.ActivityTypeEnum;
import com.hobbySphere.enums.InterestEnum;
import com.hobbySphere.repositories.ActivityTypeRepository;
import com.hobbySphere.repositories.InterestsRepository;

@Service
public class ActivityTypeService {

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

    @Autowired
    private InterestsRepository interestsRepository;

    public void ensureActivityTypes() {
        Map<InterestEnum, Interests> interestMap = new HashMap<>();

        for (InterestEnum interestEnum : InterestEnum.values()) {
            Interests interest = interestsRepository.findByName(interestEnum.name())
                .orElseGet(() -> interestsRepository.save(new Interests(interestEnum.name())));
            interestMap.put(interestEnum, interest);
        }

        for (ActivityTypeEnum typeEnum : ActivityTypeEnum.values()) {
            String name = typeEnum.name().replace("_", " ");
            if (!activityTypeRepository.existsByName(name)) {
                ActivityType type = new ActivityType();
                type.setName(name);
                type.setInterest(interestMap.get(typeEnum.getInterest()));
                activityTypeRepository.save(type);
            }
        }
    }
}
