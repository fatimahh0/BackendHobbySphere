package com.hobbySphere.repositories;

import com.hobbySphere.entities.PendingBusiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingBusinessRepository extends JpaRepository<PendingBusiness, Long> {

    boolean existsByEmail(String email);

    PendingBusiness findByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);
	
	

	PendingBusiness findByPhoneNumber(String phoneNumber);

}
