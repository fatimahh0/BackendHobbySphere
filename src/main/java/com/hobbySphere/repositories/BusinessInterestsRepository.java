package com.hobbySphere.repositories;

import com.hobbySphere.entities.BusinessInterests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hobbySphere.entities.BusinessInterests.BusinessInterestsId;

@Repository
public interface BusinessInterestsRepository extends JpaRepository<BusinessInterests, BusinessInterestsId> {
}

