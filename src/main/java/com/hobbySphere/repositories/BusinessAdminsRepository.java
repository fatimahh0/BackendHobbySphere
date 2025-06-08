package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessAdmins;
import com.hobbySphere.entities.*;
import com.hobbySphere.entities.BusinessAdminsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BusinessAdminsRepository extends JpaRepository<BusinessAdmins, BusinessAdminsId> {
    Optional<BusinessAdmins> findByAdmin_AdminId(Long adminId);
    
    boolean existsByBusinessAndAdmin(Businesses business, AdminUsers admin);

    @Modifying
    @Transactional
    @Query("DELETE FROM BusinessAdmins ba WHERE ba.business.id = :businessId")
    void deleteByBusinessId(@Param("businessId") Long businessId);
}
