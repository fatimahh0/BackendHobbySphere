package com.hobbySphere.repositories;

import com.hobbySphere.entities.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {
    // No additional methods needed since it's a singleton (ID = 1)
}
