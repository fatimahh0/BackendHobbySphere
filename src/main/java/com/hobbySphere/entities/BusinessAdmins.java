package com.hobbySphere.entities;

import jakarta.persistence.*;
import com.hobbySphere.entities.*;

@Entity
@Table(name = "BusinessAdmins")
public class BusinessAdmins {

    @EmbeddedId
    private BusinessAdminsId id;  // Composite key using @EmbeddedId

    @ManyToOne
    @MapsId("businessId")  // Maps the businessId field in the composite key
    @JoinColumn(name = "business_id", nullable = false)
    private Businesses business;

    @ManyToOne
    @MapsId("adminId")  // Maps the adminId field in the composite key
    @JoinColumn(name = "admin_id", nullable = false)
    private AdminUsers admin;

    // Constructors
    public BusinessAdmins() {}

    public BusinessAdmins(Businesses business, AdminUsers admin) {
        this.business = business;
        this.admin = admin;
        this.id = new BusinessAdminsId(business.getId(), admin.getAdminId());  // Initialize composite key
    }

    // Getters and Setters
    public BusinessAdminsId getId() {
        return id;
    }

    public void setId(BusinessAdminsId id) {
        this.id = id;
    }

    public Businesses getBusiness() {
        return business;
    }

    public void setBusiness(Businesses business) {
        this.business = business;
    }

    public AdminUsers getAdmin() {
        return admin;
    }

    public void setAdmin(AdminUsers admin) {
        this.admin = admin;
    }
}
