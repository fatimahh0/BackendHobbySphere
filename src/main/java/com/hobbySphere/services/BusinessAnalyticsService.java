package com.hobbySphere.services;

import com.hobbySphere.entities.BusinessAnalytics;
import com.hobbySphere.repositories.BusinessAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessAnalyticsService {

    @Autowired
    private BusinessAnalyticsRepository analyticsRepository;

    // Get all analytics
    public List<BusinessAnalytics> getAllAnalytics() {
        return analyticsRepository.findAll();
    }

    // Get analytics by business ID
    public List<BusinessAnalytics> getAnalyticsByBusinessId(Long businessId) {
        return analyticsRepository.findByBusinessId(businessId);
    }

    // Get the latest analytics entry for a business
    public BusinessAnalytics getLatestAnalytics(Long businessId) {
        Optional<BusinessAnalytics> latestAnalytics = analyticsRepository.findTopByBusinessIdOrderByAnalyticsDateDesc(businessId);
        return latestAnalytics.orElse(null); // Return null if not found
    }

    // Save a new analytics entry
    public BusinessAnalytics saveAnalytics(BusinessAnalytics analytics) {
        return analyticsRepository.save(analytics);
    }
}
