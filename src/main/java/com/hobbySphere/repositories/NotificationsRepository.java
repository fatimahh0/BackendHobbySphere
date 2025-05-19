package com.hobbySphere.repositories;

import com.hobbySphere.entities.Notifications;
import com.hobbySphere.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {

    
   

	List<Notifications> findByUserOrderByCreatedAtDesc(Users user);

	List<Notifications> findByUserAndIsReadFalse(Users user);
}
