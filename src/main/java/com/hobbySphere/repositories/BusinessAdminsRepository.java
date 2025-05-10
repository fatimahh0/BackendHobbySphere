package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessAdmins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessAdminsRepository extends JpaRepository<BusinessAdmins, Long> {
   
}
