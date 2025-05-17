package com.hobbySphere.services;

import com.hobbySphere.dto.BusinessAnalytics;
import com.hobbySphere.repositories.ActivityBookingsRepository;
import com.hobbySphere.repositories.ActivitiesRepository;
import com.hobbySphere.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BusinessAnalyticsService {

    @Autowired
    private ActivityBookingsRepository bookingRepo;

    @Autowired
    private ActivitiesRepository activityRepo;

    @Autowired
    private UsersRepository customerRepo;

    public BusinessAnalytics getAnalyticsForBusiness(Long businessId) {
        double totalRevenue = bookingRepo.sumRevenueByBusinessId(businessId);

        String topActivity = activityRepo.findTopActivityNameByBusinessId(businessId);
        if (topActivity == null) {
            topActivity = "No bookings yet";
        }

        double bookingGrowth = calculateBookingGrowth(businessId);
        String peakHours = findPeakHours(businessId);
        double retention = calculateCustomerRetention(businessId);

        return new BusinessAnalytics(
                totalRevenue,
                topActivity,
                bookingGrowth,
                peakHours,
                retention,
                LocalDate.now()
        );
    }

    private double calculateBookingGrowth(Long businessId) {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
        int year = now.getYear();
        int previousYear = currentMonth == 1 ? year - 1 : year;

        int currentBookings = bookingRepo.countBookingsByMonthAndYear(businessId, currentMonth, year);
        int previousBookings = bookingRepo.countBookingsByMonthAndYear(businessId, previousMonth, previousYear);

        if (previousBookings == 0) return currentBookings > 0 ? 100.0 : 0.0;

        return ((double)(currentBookings - previousBookings) / previousBookings) * 100.0;
    }

    private String findPeakHours(Long businessId) {
        List<Object[]> result = bookingRepo.findPeakBookingHours(businessId);
        if (result == null || result.isEmpty()) return "No data";

        // ✅ FIX: avoid ClassCastException from BigDecimal to Integer
        Number hourValue = (Number) result.get(0)[0]; // could be BigDecimal
        int peakHour = hourValue.intValue();

        return String.format("%d:00 - %d:00", peakHour, peakHour + 1);
    }

    private double calculateCustomerRetention(Long businessId) {
        int total = bookingRepo.countDistinctCustomers(businessId);
        if (total == 0) return 0.0;

        int returning = bookingRepo.countReturningCustomers(businessId);
        return (returning / (double) total) * 100.0;
    }
}
