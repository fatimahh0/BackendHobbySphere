package com.hobbySphere.controller;

import com.hobbySphere.repositories.*;
import com.hobbySphere.entities.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.hobbySphere.dto.UserDto;
import com.hobbySphere.entities.Users;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private static final String CLIENT_ID = "851915342014-j24igdgk6pvfqh4hu6pbs65jtp6a1r0k.apps.googleusercontent.com";

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserStatusRepository userStatusRepository;


    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String idTokenString = request.get("idToken");

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance()
            )
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token.");
            }

            Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            AtomicBoolean wasInactive = new AtomicBoolean(false);
            Users user = userService.handleGoogleUser(email, fullName, pictureUrl, wasInactive);

            // 🔍 Handle status checks like in AuthController
            String currentStatus = user.getStatus().getName();

            if ("DELETED".equalsIgnoreCase(currentStatus)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "This account has been deleted and cannot be accessed."));
            }

            if ("INACTIVE".equalsIgnoreCase(currentStatus)) {
                String tempToken = jwtUtil.generateToken(user);

                Map<String, Object> inactiveUserData = Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "username", user.getUsername(),
                        "profilePictureUrl", user.getProfilePictureUrl()
                );

                return ResponseEntity.ok(Map.of(
                        "wasInactive", true,
                        "message", "Your account is inactive. Confirm reactivation.",
                        "token", tempToken,
                        "user", inactiveUserData
                ));
            }

            // ✅ Normal login
            user.setLastLogin(java.time.LocalDateTime.now());
            userService.save(user);

            String token = jwtUtil.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "wasInactive", false,
                    "user", Map.of(
                            "id", user.getId(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "email", user.getEmail(),
                            "profileImageUrl", user.getProfilePictureUrl(),
                            "username", user.getUsername(),
                            "status", user.getStatus().getName(),
                            "lastLogin", user.getLastLogin(),
                            "publicProfile", user.isPublicProfile()
                    )
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google login failed.");
        }
    }

}
