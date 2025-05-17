package com.hobbySphere.controller;

import com.hobbySphere.dto.BusinessAnalytics;
import com.hobbySphere.services.BusinessAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
@Tag(name = "Business Analytics API", description = "Endpoints for generating business analytics insights")
public class BusinessAnalyticsController {

    @Autowired
    private BusinessAnalyticsService analyticsService;

    @Operation(
        summary = "Get analytics for a specific business",
        description = "Dynamically generate business analytics insights for a specific business ID"
    )
    @GetMapping("/business/{businessId}/insights")
    public ResponseEntity<BusinessAnalytics> getBusinessInsights(
        @Parameter(description = "ID of the business to retrieve analytics for")
        @PathVariable Long businessId) {

        BusinessAnalytics analytics = analyticsService.getAnalyticsForBusiness(businessId);
        if (analytics == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analytics);
    }
}
