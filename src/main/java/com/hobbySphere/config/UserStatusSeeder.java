package com.hobbySphere.config;

import com.hobbySphere.entities.UserStatus;
import com.hobbySphere.repositories.UserStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UserStatusSeeder {

    @Bean
    public CommandLineRunner seedUserStatuses(UserStatusRepository userStatusRepository) {
        return args -> {
            System.out.println("✅ UserStatus Seeder running...");

            List<String> statuses = List.of("ACTIVE", "INACTIVE", "DELETED", "PENDING");

            for (String name : statuses) {
                boolean exists = userStatusRepository.findByName(name).isPresent();
                if (!exists) {
                    userStatusRepository.save(new UserStatus(name));
                    System.out.println("➕ Inserted UserStatus: " + name);
                }
            }
        };
    }
}
