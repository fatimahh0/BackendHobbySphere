package com.hobbySphere.controller;

import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.repositories.ActivityTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
public class ActivityTypeController {

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

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

}
