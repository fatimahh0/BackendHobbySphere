package com.hobbySphere.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ActivityBookings")
public class ActivityBookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "createdBy", "category"}) // adapte selon tes relations dans Activities
    private Activities activity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "roles", "password"}) // adapte selon Users
    private Users user;

    @Column(name = "number_of_participants")
    private int numberOfParticipants;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "booking_status", nullable = false)
    private String bookingStatus = "Pending";

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "booking_datetime", updatable = false)
    private LocalDateTime bookingDatetime;

    public ActivityBookings() {}

    public ActivityBookings(Activities activity, Users user, int numberOfParticipants,
                           BigDecimal totalPrice, String paymentMethod) {
        this.activity = activity;
        this.user = user;
        this.numberOfParticipants = numberOfParticipants;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.bookingStatus = "Pending";
    }

    @PrePersist
    protected void onCreate() {
        this.bookingDatetime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Activities getActivity() {
        return activity;
    }

    public void setActivity(Activities activity) {
        this.activity = activity;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public void setNumberOfParticipants(int numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getBookingDatetime() {
        return bookingDatetime;
    }

    public void setBookingDatetime(LocalDateTime bookingDatetime) {
        this.bookingDatetime = bookingDatetime;
    }
}
