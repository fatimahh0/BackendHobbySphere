package com.hobbySphere.controller;

import com.hobbySphere.entities.Interests;
import com.hobbySphere.repositories.InterestRepository;
import com.hobbySphere.security.JwtUtil;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    
    @Autowired
    private JwtUtil jwtUtil;

    private boolean isAuthorized(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        String jwt = token.substring(7).trim();
        return jwtUtil.isUserToken(jwt) || jwtUtil.isBusinessToken(jwt)
                || "SUPER_ADMIN".equals(jwtUtil.extractRole(jwt))
                || "MANAGER".equals(jwtUtil.extractRole(jwt));
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllInterests(
            @RequestHeader("Authorization") String authHeader) {

        if (!isAuthorized(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

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

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    @GetMapping("/categorized")
    public ResponseEntity<Map<String, List<String>>> getCategorizedInterests(
            @RequestHeader("Authorization") String authHeader) {

        if (!isAuthorized(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

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

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    @PostMapping("/create")
    public ResponseEntity<String> createInterest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        if (!isAuthorized(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

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
