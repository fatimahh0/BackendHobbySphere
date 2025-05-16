package com.hobbySphere.controller;

import com.hobbySphere.entities.BusinessAnalytics;
import com.hobbySphere.services.BusinessAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*") 
@Tag(name = "Business Analytics API", description = "Endpoints for managing business analytics data")
public class BusinessAnalyticsController {

    @Autowired
    private BusinessAnalyticsService analyticsService;

    @Operation(summary = "Get all analytics", description = "Retrieve all business analytics records")
    @GetMapping
    public List<BusinessAnalytics> getAllAnalytics() {
        return analyticsService.getAllAnalytics();
    }

    @Operation(summary = "Get analytics for a specific business", 
               description = "Retrieve business analytics for a specific business by its ID")
    @GetMapping("/business/{businessId}")
    public List<BusinessAnalytics> getAnalyticsByBusiness(
            @Parameter(description = "ID of the business to retrieve analytics for") 
            @PathVariable Long businessId) {
        return analyticsService.getAnalyticsByBusinessId(businessId);
    }

    @Operation(summary = "Add new analytics data", 
               description = "Create a new business analytics record")
    @PostMapping
    public BusinessAnalytics createAnalytics(@RequestBody BusinessAnalytics analytics) {
        return analyticsService.saveAnalytics(analytics);
    }

    @Operation(summary = "Get latest analytics for a business", 
               description = "Retrieve the latest analytics entry for a specific business by its ID")
    @GetMapping("/business/{businessId}/latest")
    public BusinessAnalytics getLatestAnalytics(
            @Parameter(description = "ID of the business to retrieve the latest analytics for") 
            @PathVariable Long businessId) {
        return analyticsService.getLatestAnalytics(businessId);
    }
}
