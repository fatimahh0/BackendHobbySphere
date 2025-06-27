package com.hobbySphere.controller;

import com.hobbySphere.dto.ThemeResponseDTO;
import com.hobbySphere.entities.Theme;
import com.hobbySphere.services.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/themes")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175"
})
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    // === SUPERADMIN: CRUD THEMES ===

    // Get all themes
    @GetMapping("/all")
    public ResponseEntity<?> getAllThemes() {
        try {
            List<Theme> themes = themeService.getAllThemes();
            List<ThemeResponseDTO> dtos = themes.stream()
                    .map(ThemeResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error fetching themes: " + e.getMessage()));
        }
    }

    // Create a new theme
    @PostMapping("/create")
    public ResponseEntity<?> createTheme(@RequestBody Theme theme) {
        try {
            if (themeService.existsByName(theme.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Collections.singletonMap("message", "A theme with this name already exists."));
            }
            Theme savedTheme = themeService.saveTheme(theme);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Theme created successfully.");
            response.put("theme", new ThemeResponseDTO(savedTheme));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Error creating theme: " + e.getMessage()));
        }
    }

    // Delete a theme by ID
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteTheme(@PathVariable Long id) {
        try {
            Optional<Theme> themeOpt = themeService.getThemeById(id);
            if (themeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "Theme not found."));
            }
            themeService.deleteTheme(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Theme deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error deleting theme: " + e.getMessage()));
        }
    }

    // Get a single theme by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getThemeById(@PathVariable Long id) {
        try {
            Optional<Theme> theme = themeService.getThemeById(id);
            if (theme.isPresent()) {
                return ResponseEntity.ok(new ThemeResponseDTO(theme.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "Theme not found."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error fetching theme: " + e.getMessage()));
        }
    }

    // === BUSINESS THEME ASSIGNMENT ===

    // Get assigned theme by business ID
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getThemeByBusiness(@PathVariable Long businessId) {
        try {
            Theme theme = themeService.getThemeByBusinessId(businessId);
            if (theme != null) {
                return ResponseEntity.ok(new ThemeResponseDTO(theme));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No theme found for this business."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error fetching business theme: " + e.getMessage()));
        }
    }

    // Assign theme to business (expects { "themeId": 3 } in JSON)
    @PostMapping("/business/{businessId}")
    public ResponseEntity<?> assignThemeToBusiness(@PathVariable Long businessId,
            @RequestBody Map<String, Object> payload) {
        try {
            Long themeId = Long.valueOf(payload.get("themeId").toString());
            themeService.assignThemeToBusiness(businessId, themeId);
            return ResponseEntity.ok(Collections.singletonMap("message", "Theme assigned to business."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Error assigning theme to business: " + e.getMessage()));
        }
    }

    // === USER THEME ASSIGNMENT ===

    // Get assigned theme by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getThemeByUser(@PathVariable Long userId) {
        try {
            Theme theme = themeService.getThemeByUserId(userId);
            if (theme != null) {
                return ResponseEntity.ok(new ThemeResponseDTO(theme));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No theme found for this user."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error fetching user theme: " + e.getMessage()));
        }
    }

    // Assign theme to user (expects { "themeId": 3 } in JSON)
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> assignThemeToUser(@PathVariable Long userId, @RequestBody Map<String, Object> payload) {
        try {
            Long themeId = Long.valueOf(payload.get("themeId").toString());
            themeService.assignThemeToUser(userId, themeId);
            return ResponseEntity.ok(Collections.singletonMap("message", "Theme assigned to user."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Error assigning theme to user: " + e.getMessage()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", "Server error: " + e.getMessage()));
}
}
