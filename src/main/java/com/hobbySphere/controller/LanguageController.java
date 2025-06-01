package com.hobbySphere.controller;

import com.hobbySphere.entities.*;
import com.hobbySphere.enums.LanguageType;
import com.hobbySphere.repositories.*;
import com.hobbySphere.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/languages")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
@Tag(name = "Language Management", description = "Operations for viewing and selecting languages")
public class LanguageController {

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BusinessesRepository businessesRepository;

    @Autowired
    private AdminUsersRepository adminUsersRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private boolean hasAccess(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
                String role = jwtUtil.extractRole(token);
                return jwtUtil.isBusinessToken(token) ||
                        role != null && role.equalsIgnoreCase("SUPER_ADMIN") ||
                        role == null;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Operation(summary = "Get all available languages")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Languages retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<?> getAllLanguages(
        @Parameter(description = "Bearer token") @RequestHeader("Authorization") String token) {
        if (!hasAccess(token)) return ResponseEntity.status(401).body("Unauthorized");
        List<Languages> languages = languageRepository.findAll();
        return ResponseEntity.ok(languages);
    }

    @Operation(summary = "Set preferred language for the current user/business/admin")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Language preference updated"),
        @ApiResponse(responseCode = "400", description = "Invalid language selected"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User or entity not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/select")
    public ResponseEntity<?> selectPreferredLanguage(
        @Parameter(description = "Selected language (ARABIC, ENGLISH, FRENCH)") @RequestParam String selected,
        @Parameter(description = "Bearer token") @RequestHeader("Authorization") String token) {

        if (!hasAccess(token)) return ResponseEntity.status(401).body("Unauthorized");

        try {
            token = token.substring(7).trim();
            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            LanguageType selectedLang = LanguageType.valueOf(selected.toUpperCase());

            if (jwtUtil.isBusinessToken(token)) {
                Businesses business = businessesRepository.findByEmail(email).orElse(null);
                if (business != null) {
                    business.setPreferredLanguage(selectedLang);
                    businessesRepository.save(business);
                    return ResponseEntity.ok("Business language updated to: " + selected);
                }
            } else if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
                AdminUsers admin = adminUsersRepository.findByEmail(email).orElse(null);
                if (admin != null) {
                    admin.setPreferredLanguage(selectedLang);
                    adminUsersRepository.save(admin);
                    return ResponseEntity.ok("Admin language updated to: " + selected);
                }
            } else {
                Users user = usersRepository.findByEmail(email);
                if (user != null) {
                    user.setPreferredLanguage(selectedLang);
                    usersRepository.save(user);
                    return ResponseEntity.ok("User language updated to: " + selected);
                }
            }

            return ResponseEntity.status(404).body("Entity not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid language selected");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }
}
