package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessInterests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessInterestsRepository extends JpaRepository<BusinessInterests, Long> {
   
}
