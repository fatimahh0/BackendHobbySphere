package com.hobbySphere.config;

import org.springframework.stereotype.Component;

import com.hobbySphere.entities.Interests;
import com.hobbySphere.enums.ActivityTypeEnum;
import com.hobbySphere.repositories.InterestsRepository;

import jakarta.annotation.PostConstruct;

@Component
public class InterestSeeder {

    private final InterestsRepository interestsRepository;

    public InterestSeeder(InterestsRepository interestsRepository) {
        this.interestsRepository = interestsRepository;
    }

    @PostConstruct
    public void seedInterests() {
        for (ActivityTypeEnum enumValue : ActivityTypeEnum.values()) {
            String interestName = enumValue.getInterest().name();

            if (interestsRepository.findByName(interestName).isEmpty()) {
                interestsRepository.save(new Interests(interestName));
            }
        }
    }
}
