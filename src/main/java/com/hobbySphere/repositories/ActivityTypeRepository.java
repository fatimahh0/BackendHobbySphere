package com.hobbySphere.repositories;

import com.hobbySphere.entities.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ActivityTypeRepository extends JpaRepository<ActivityType, Long> {

	boolean existsByName(String type);
	
	List<ActivityType> findAllByOrderByNameAsc();

	

}
