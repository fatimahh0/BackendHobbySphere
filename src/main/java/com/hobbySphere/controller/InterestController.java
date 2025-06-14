package com.hobbySphere.controller;

import com.hobbySphere.entities.Interests;
import com.hobbySphere.repositories.InterestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity-types")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
public class InterestController {

    private final InterestRepository interestRepository;

    public InterestController(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    // ✅ Get all interests
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllInterests() {
        List<Map<String, Object>> interests = interestRepository.findAll()
            .stream()
            .map(i -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", i.getId());
                map.put("name", i.getName());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(interests);
    }


    // ✅ Get categorized interests
    @GetMapping("/categorized")
    public ResponseEntity<Map<String, List<String>>> getCategorizedInterests() {
        Map<String, List<String>> categorizedTypes = new LinkedHashMap<>();

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

        return ResponseEntity.ok(categorizedTypes);
    }

    // ✅ Add new interest
    @PostMapping("/create")
    public ResponseEntity<String> createInterest(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("Interest name is required");
        }

        if (interestRepository.existsByNameIgnoreCase(name)) {
            return ResponseEntity.badRequest().body("Interest already exists");
        }

        Interests interest = new Interests(name);
        interestRepository.save(interest);

        return ResponseEntity.ok("Interest created successfully");
    }
}
