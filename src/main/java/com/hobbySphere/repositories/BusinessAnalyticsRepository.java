package com.hobbySphere.repositories;

import com.hobbySphere.dto.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessAnalyticsRepository extends JpaRepository<BusinessAnalytics, Long> {

    // Fetch all analytics for a specific business
    List<BusinessAnalytics> findByBusinessId(Long businessId);

    // Fetch the latest analytics for a specific business (based on analyticsDate)
    Optional<BusinessAnalytics> findTopByBusinessIdOrderByAnalyticsDateDesc(Long businessId);
}
