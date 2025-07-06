package com.hobbySphere.config;

import com.hobbySphere.entities.BusinessStatus;
import com.hobbySphere.repositories.BusinessStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BusinessStatusSeeder {

    @Bean
    public CommandLineRunner seedBusinessStatuses(BusinessStatusRepository businessStatusRepository) {
        return args -> {
            System.out.println("✅ BusinessStatus Seeder running...");

            List<String> statuses = List.of("ACTIVE", "INACTIVE", "DELETED","INACTIVEBYADMIN");

            for (String name : statuses) {
                boolean exists = businessStatusRepository.findByName(name).isPresent();
                if (!exists) {
                	businessStatusRepository.save(new BusinessStatus(name));

                    System.out.println("➕ Inserted BusinessStatus: " + name);
                }
            }
        };
    }
}
