package com.hobbySphere.services;
import com.hobbySphere.repositories.*;
import com.hobbySphere.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessAdminService {

    @Autowired
    private BusinessAdminsRepository businessAdminRepository;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private BusinessService businessService;

    public void assignAdminToBusiness(Long adminId, Long businessId) {
        AdminUsers admin = adminUserService.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        Businesses business = businessService.findById(businessId);
        if (business == null) {
            throw new RuntimeException("Business not found");
        }

        BusinessAdmins businessAdmin = new BusinessAdmins(business, admin);
        businessAdminRepository.save(businessAdmin);
    }
}
