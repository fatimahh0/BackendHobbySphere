package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "BusinessAnalytics")
public class BusinessAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double totalRevenue;

    @Column(nullable = false)
    private String topActivity;

    @Column(nullable = false)
    private double bookingGrowth;

    @Column(nullable = false)
    private String peakHours;

    @Column(nullable = false)
    private double customerRetention;

    private LocalDate analyticsDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Businesses business;

    public BusinessAnalytics() {
    }

    public BusinessAnalytics(Long id, double totalRevenue, String topActivity, double bookingGrowth,
                              String peakHours, double customerRetention, LocalDate analyticsDate, Businesses business) {
        this.id = id;
        this.totalRevenue = totalRevenue;
        this.topActivity = topActivity;
        this.bookingGrowth = bookingGrowth;
        this.peakHours = peakHours;
        this.customerRetention = customerRetention;
        this.analyticsDate = analyticsDate;
        this.business = business;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Businesses getBusiness() {
        return business;
    }

    public void setBusiness(Businesses business) {
        this.business = business;
    }
}
