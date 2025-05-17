package com.hobbySphere.dto;

import java.time.LocalDate;

public class BusinessAnalytics {

    private double totalRevenue;
    private String topActivity;
    private double bookingGrowth;
    private String peakHours;
    private double customerRetention;
    private LocalDate analyticsDate;

    public BusinessAnalytics() {}

    public BusinessAnalytics(double totalRevenue, String topActivity, double bookingGrowth,
                             String peakHours, double customerRetention, LocalDate analyticsDate) {
        this.totalRevenue = totalRevenue;
        this.topActivity = topActivity;
        this.bookingGrowth = bookingGrowth;
        this.peakHours = peakHours;
        this.customerRetention = customerRetention;
        this.analyticsDate = analyticsDate;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getTopActivity() {
        return topActivity;
    }

    public void setTopActivity(String topActivity) {
        this.topActivity = topActivity;
    }

    public double getBookingGrowth() {
        return bookingGrowth;
    }

    public void setBookingGrowth(double bookingGrowth) {
        this.bookingGrowth = bookingGrowth;
    }

    public String getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(String peakHours) {
        this.peakHours = peakHours;
    }

    public double getCustomerRetention() {
        return customerRetention;
    }

    public void setCustomerRetention(double customerRetention) {
        this.customerRetention = customerRetention;
    }

    public LocalDate getAnalyticsDate() {
        return analyticsDate;
    }

    public void setAnalyticsDate(LocalDate analyticsDate) {
        this.analyticsDate = analyticsDate;
    }
}
