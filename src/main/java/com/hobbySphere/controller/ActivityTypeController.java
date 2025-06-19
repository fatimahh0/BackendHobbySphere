package com.hobbySphere.controller;

import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.repositories.ActivityTypeRepository;
import com.hobbySphere.services.ActivityTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175"
})

public class ActivityTypeController {

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

    @Autowired
    private ActivityTypeService activityTypeService;

   
    @PostMapping
    public ActivityType create(@RequestBody ActivityType type) {
        return activityTypeRepository.save(type);
    }

    
    @GetMapping
    public List<ActivityType> getAll() {
        return activityTypeRepository.findAllByOrderByNameAsc();
    }

   
    @DeleteMapping("/{id}")
    
    public void delete(@PathVariable Long id) {
        activityTypeRepository.deleteById(id);
    }

   
    @PostMapping("/seed-defaults")
    public String seedDefaults() {
        activityTypeService.ensureActivityTypes();
        return "Default activity types and interests seeded successfully.";
    }
}
