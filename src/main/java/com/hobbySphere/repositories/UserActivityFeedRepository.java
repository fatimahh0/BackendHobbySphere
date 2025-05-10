package com.hobbySphere.repositories;

import com.hobbySphere.entities.UserActivityFeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityFeedRepository extends JpaRepository<UserActivityFeed, Long> {
   
}
