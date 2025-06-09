package com.hobbySphere.config;

import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.entities.Interests;
import com.hobbySphere.repositories.ActivityTypeRepository;
import com.hobbySphere.repositories.InterestsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class ActivityTypeSeeder {

    @Bean
    public CommandLineRunner seedActivityTypes(
            ActivityTypeRepository activityTypeRepo,
            InterestsRepository interestsRepo
    ) {
        return args -> {
            System.out.println("✅ ActivityType Seeder running...");

            Map<String, List<String>> categorizedTypes = new HashMap<>();

            categorizedTypes.put("Sports", List.of("Football", "Yoga", "Martial Arts", "Hiking", "Horseback Riding", "Fishing"));
            categorizedTypes.put("Music", List.of("Music", "Dance", "Music Production"));
            categorizedTypes.put("Art", List.of("Art", "Sculpting", "Knitting", "Calligraphy"));
            categorizedTypes.put("Tech", List.of("Coding", "Robotics", "3D Printing", "Science Experiments"));
            categorizedTypes.put("Fitness", List.of("Fitness", "Self-Defense", "Meditation"));
            categorizedTypes.put("Cooking", List.of("Cooking"));
            categorizedTypes.put("Travel", List.of("Travel", "Nature Walks"));
            categorizedTypes.put("Gaming", List.of("Gaming", "Board Games"));
            categorizedTypes.put("Theater", List.of("Theater", "Stand-up Comedy", "Storytelling"));
            categorizedTypes.put("Language", List.of("Language", "Public Speaking", "Writing"));
            categorizedTypes.put("Photography", List.of("Photography", "Film Making"));
            categorizedTypes.put("DIY", List.of("DIY", "Carpentry", "Interior Design"));
            categorizedTypes.put("Beauty", List.of("Makeup & Beauty"));
            categorizedTypes.put("Finance", List.of("Investment & Finance", "Entrepreneurship"));
            categorizedTypes.put("Other", List.of("Pet Training", "Podcasting", "Magic Tricks", "Astronomy", "Public Service", "Productivity", "Bird Watching", "Cultural Events"));

            for (Map.Entry<String, List<String>> entry : categorizedTypes.entrySet()) {
                String interestName = entry.getKey();

                // ✅ Create interest if it doesn't exist
                Interests interest = interestsRepo.findByName(interestName)
                        .orElseGet(() -> {
                            Interests newInterest = new Interests(interestName);
                            System.out.println("➕ Creating missing interest: " + interestName);
                            return interestsRepo.save(newInterest);
                        });

                for (String type : entry.getValue()) {
                    if (!activityTypeRepo.existsByName(type)) {
                        ActivityType newType = new ActivityType(null, type, interest);
                        activityTypeRepo.save(newType);
                    }
                }
            }
        };
    }
}
