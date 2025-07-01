package com.hobbySphere.config;

import com.hobbySphere.entities.NotificationTypeEntity;
import com.hobbySphere.repositories.NotificationTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class NotificationTypeSeeder {

    @Bean
    public CommandLineRunner seedNotificationTypes(NotificationTypeRepository repository) {
        return args -> {
            System.out.println("✅ NotificationType Seeder running...");

            Map<String, String> types = Map.ofEntries(
                Map.entry("ACTIVITY_UPDATE", "Activity Update"),
                Map.entry("MESSAGE", "Message"),
                Map.entry("BOOKING_REMINDER", "Booking Reminder"),
                Map.entry("EVENT_REMINDER", "Event Reminder"),
                Map.entry("BOOKING_CREATED", "Booking Created"),
                Map.entry("BOOKING_CANCELLED", "Booking Cancelled"),
                Map.entry("BOOKING_PENDING", "Booking Returned to Pending"),
                Map.entry("NEW_REVIEW", "New Review"),
                Map.entry("FRIEND_REQUEST_SENT", "Friend Request Sent"),
                Map.entry("FRIEND_REQUEST_ACCEPTED", "Friend Request Accepted"),
                Map.entry("FRIEND_REQUEST_REJECTED", "Friend Request Rejected"),
                Map.entry("FRIEND_REMOVED", "Friend Removed"),
                Map.entry("FRIEND_REQUEST_CANCELLED", "Friend Request Cancelled"),
                Map.entry("FRIEND_BLOCKED", "Friend Blocked")
            );

            for (Map.Entry<String, String> entry : types.entrySet()) {
                String code = entry.getKey();
                String description = entry.getValue();

                boolean exists = repository.findByCode(code).isPresent();
                if (!exists) {
                    NotificationTypeEntity type = new NotificationTypeEntity();
                    type.setCode(code);
                    type.setDescription(description);
                    repository.save(type);
                    System.out.println("➕ Inserted NotificationType: " + code);
                }
            }
        };
    }
}
