package com.hobbySphere.services;

import com.hobbySphere.repositories.UsersRepository;
import com.hobbySphere.repositories.ActivitiesRepository;
import com.hobbySphere.repositories.ActivityBookingsRepository;
import com.hobbySphere.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminStatsService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ActivitiesRepository activitiesRepository;

    @Autowired
    private ActivityBookingsRepository bookingsRepository;

    @Autowired(required = false) // Optional if feedback is not implemented
    private ReviewRepository reviewRepository;

    public Map<String, Long> getStats(String period) {
        LocalDateTime fromDate = switch (period.toLowerCase()) {
            case "week" -> LocalDateTime.now().minusWeeks(1);
            case "month" -> LocalDateTime.now().minusMonths(1);
            default -> LocalDateTime.now().toLocalDate().atStartOfDay(); // today
        };

        Map<String, Long> stats = new HashMap<>();
        stats.put("users", usersRepository.countByCreatedAtAfter(fromDate));
        stats.put("activities", activitiesRepository.countByCreatedAtAfter(fromDate));
        stats.put("bookings", bookingsRepository.countByBookingDatetimeAfter(fromDate));

        if (reviewRepository != null) {
            stats.put("feedback", reviewRepository.countByCreatedAtAfter(fromDate));
        }

        return stats;
    }
    
    public Map<String, Long> getMonthlyRegistrations() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Object[]> result = usersRepository.countMonthlyRegistrations(sixMonthsAgo);

        Map<String, Long> registrations = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            registrations.put(ym.toString(), 0L); // Default to 0
        }

        for (Object[] row : result) {
            String monthStr = (String) row[0]; // "2025-05"
            Long count = ((Number) row[1]).longValue();
            registrations.put(monthStr, count);
        }

        return registrations;
    }

    
    public List<Map<String, Object>> getPopularActivities() {
        List<Object[]> result = activitiesRepository.findPopularActivities();

        List<Map<String, Object>> popularActivities = new ArrayList<>();
        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("bookings", row[1]);
            map.put("views", row[2]);
            popularActivities.add(map);
        }

        return popularActivities;
    }

}
