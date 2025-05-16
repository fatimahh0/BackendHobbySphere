package com.hobbySphere.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class SecurityConfig implements WebMvcConfigurer{

    // Define the PasswordEncoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Using BCrypt for password encoding
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // Public access to auth routes (login, register)
                        .anyRequest().authenticated() // All other routes require authentication
                );
        return http.build();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps: http://localhost:9090/uploads/** 
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPath); // Must end with "/"
    }
}
