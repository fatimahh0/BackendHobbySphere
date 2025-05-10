package com.hobbySphere.repositories;

import com.hobbySphere.entities.UserInterests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInterestsRepository extends JpaRepository<UserInterests, Long> {
   
}
