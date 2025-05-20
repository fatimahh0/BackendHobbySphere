package com.hobbySphere.controller;

import com.hobbySphere.dto.ActivityDetailsDTO;
import com.hobbySphere.dto.ActivitySummaryDTO;
import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.services.ActivityService;
import com.hobbySphere.services.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hobbySphere.services.BusinessAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Controller", description = "Endpoints for managing activities by admin users")
public class AdminController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private AdminUserService adminUserService;
    
    @Autowired 
    private BusinessAdminService businessAdminService;

    @Operation(summary = "Get activity summaries for the logged-in admin")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of activity summaries retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Admin not found")
    })
    @GetMapping("/activities/summary")
    public ResponseEntity<List<ActivitySummaryDTO>> getActivitySummary(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        AdminUsers admin = adminUserService.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<ActivitySummaryDTO> summaries = activityService.getActivitySummariesByAdmin(admin);
        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "Delete an activity by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @DeleteMapping("/activities/{id}")
    public ResponseEntity<String> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok("Activity with ID " + id + " has been deleted successfully.");
    }

    @Operation(summary = "Get detailed information about an activity by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Activity details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Activity not found")
    })
    @GetMapping("/activities/{id}")
    public ResponseEntity<ActivityDetailsDTO> getActivityDetails(@PathVariable Long id) {
        Activities activity = activityService.findById(id);
        if (activity == null) {
            return ResponseEntity.notFound().build();
        }

        ActivityDetailsDTO dto = new ActivityDetailsDTO(
            activity.getId(),
            activity.getActivityName(),
            activity.getDescription(),
            activity.getActivityType(),
            activity.getLocation(),
            activity.getStartDatetime(),
            activity.getEndDatetime(),
            activity.getPrice(),
            activity.getMaxParticipants(),
            activity.getStatus(),
            activity.getImageUrl(),
            activity.getBusiness().getBusinessName()
        );

        return ResponseEntity.ok(dto);
    }
    
    @PostMapping("/assign-admin")
    public ResponseEntity<String> assignAdminToBusiness(
            @RequestParam Long adminId,
            @RequestParam Long businessId) {

        businessAdminService.assignAdminToBusiness(adminId, businessId);
        return ResponseEntity.ok("Admin with ID " + adminId + " has been assigned to Business with ID " + businessId);
    }

}
