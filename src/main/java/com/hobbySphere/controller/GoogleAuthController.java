package com.hobbySphere.controller;

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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private static final String CLIENT_ID = "851915342014-j24igdgk6pvfqh4hu6pbs65jtp6a1r0k.apps.googleusercontent.com";

    @Autowired
    private UserService userService;

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

            // Handle user creation or login
            Users user = userService.handleGoogleUser(email, fullName, pictureUrl);
            String token = jwtUtil.generateToken(user);
            UserDto userDto = new UserDto(user);

            return ResponseEntity.ok(Map.of(
            	    "token", token,
            	    "user", Map.of(
            	        "id", user.getId(),
            	        "firstName", user.getFirstName(),
            	        "lastName", user.getLastName(),
            	        "email", user.getEmail(),
            	        "profileImageUrl", user.getProfilePictureUrl(),
            	        "username", user.getUsername(),
            	        "status", user.getStatus().name(),
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
