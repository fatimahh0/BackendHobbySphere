package com.hobbySphere.entities;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.hobbySphere.entities.Role;
import com.hobbySphere.repositories.RoleRepository;

@Configuration
public class DataInitializer {

	 @Bean
	    public CommandLineRunner initRoles(RoleRepository roleRepository) {
	        return args -> {
	            if (roleRepository.findByName("SUPER_ADMIN").isEmpty()) {
	                roleRepository.save(new Role("SUPER_ADMIN"));
	            }
	            if (roleRepository.findByName("MANAGER").isEmpty()) {
	                roleRepository.save(new Role("MANAGER"));
	            }
	        };
	    }
}
