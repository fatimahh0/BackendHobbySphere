package com.hobbySphere.repositories;

import com.hobbySphere.entities.Interests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestsRepository extends JpaRepository<Interests, Long> {
   
}
