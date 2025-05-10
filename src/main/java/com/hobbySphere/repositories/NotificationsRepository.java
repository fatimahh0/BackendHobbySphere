package com.hobbySphere.repositories;

import com.hobbySphere.entities.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {
    // Custom query methods can be added here if needed
}
