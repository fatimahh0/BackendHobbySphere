package com.hobbySphere.config;

import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.repositories.ActivityTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ActivityTypeSeeder {

    @Bean
    public CommandLineRunner seedActivityTypes(ActivityTypeRepository repository) {
        return args -> {
            List<String> defaultTypes = List.of(
                "Sports", "Music", "Art", "Cooking", "Tech", "Fitness", "Travel",
                "Photography", "Dance", "Language", "Crafts", "Gaming", "Theater",
                "Meditation", "DIY", "Hiking", "Writing", "Coding", "Volunteering",
                "Entrepreneurship", "Yoga", "Martial Arts", "Board Games", "Nature Walks",
                "Public Speaking", "Film Making", "Bird Watching", "Sculpting", "Knitting",
                "Calligraphy", "Makeup & Beauty", "Fishing", "Gardening", "Pet Training",
                "Podcasting", "Magic Tricks", "Stand-up Comedy", "Interior Design",
                "Productivity", "Storytelling", "Astronomy", "Science Experiments",
                "Investment & Finance", "Self-Defense", "Public Service", "Carpentry",
                "Music Production", "Cultural Events", "Horseback Riding", "3D Printing",
                "Robotics"
            );
            System.out.println("✅ Seeder running...");

            for (String type : defaultTypes) {
                if (!repository.existsByName(type)) {
                    repository.save(new ActivityType(null, type));
                    
                }
            }
        };
    }
}
