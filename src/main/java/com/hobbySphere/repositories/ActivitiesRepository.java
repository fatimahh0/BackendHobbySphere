package com.hobbySphere.repositories;

import com.hobbySphere.entities.Activities;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activities, Long> {
	
	List<Activities> findByBusinessId(Long businessId); 
	
  
}