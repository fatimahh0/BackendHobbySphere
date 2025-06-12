package com.hobbySphere;

import org.springframework.boot.CommandLineRunner;
import com.hobbySphere.entities.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hobbySphere.enums.LanguageType;
import com.hobbySphere.repositories.LanguageRepository;
import com.hobbySphere.repositories.RoleRepository;

@SpringBootApplication
public class HobbySphereApplication {

	public static void main(String[] args) {
		SpringApplication.run(HobbySphereApplication.class, args);
	}
	
	 @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

	 @Bean
	    public CommandLineRunner seedLanguages(LanguageRepository repo) {
	        return args -> {
	            if (repo.count() == 0) {
	                for (LanguageType type : LanguageType.values()) {
	                    repo.save(new Languages(type));
	                }
	            }
	        };
	    }
	 
	 
}
