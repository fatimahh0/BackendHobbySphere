package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessAdmins;
import com.hobbySphere.entities.*;
import com.hobbySphere.entities.BusinessAdminsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessAdminsRepository extends JpaRepository<BusinessAdmins, BusinessAdminsId> {
    Optional<BusinessAdmins> findByAdmin_AdminId(Long adminId);
    
    boolean existsByBusinessAndAdmin(Businesses business, AdminUsers admin);

}
