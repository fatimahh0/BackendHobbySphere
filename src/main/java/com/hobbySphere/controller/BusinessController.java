package com.hobbySphere.controller;

import com.hobbySphere.entities.Businesses;
import com.hobbySphere.enums.BusinessStatus;
import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/businesses")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
@Tag(name = "Business Management", description = "Operations for creating, reading, updating, and deleting businesses")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private JwtUtil jwtUtil;

    private boolean isAuthorized(String token, Long targetBusinessId) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        String jwt = token.substring(7);
        String email = jwtUtil.extractUsername(jwt);
        String role = jwtUtil.extractRole(jwt);
        if ("SUPER_ADMIN".equals(role)) return true;
        Businesses business = businessService.findByEmail(email).orElse(null);
        return business != null && business.getId().equals(targetBusinessId);
    }

    private boolean isBusinessToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;
        String jwt = token.substring(7);
        String email = jwtUtil.extractUsername(jwt);
        return businessService.findByEmail(email).isPresent();
    }

    @Operation(summary = "Get a business by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Business retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Business not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getBusinessById(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        if (!isAuthorized(authHeader, id)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied");
        Businesses business = businessService.findById(id);
        return business != null ? ResponseEntity.ok(business) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Operation(summary = "Get all businesses")
    @ApiResponse(responseCode = "200", description = "List of all businesses")
    @GetMapping
    public ResponseEntity<?> getAllBusinesses(@RequestHeader("Authorization") String authHeader) {
        String role = jwtUtil.extractRole(authHeader.replace("Bearer ", ""));
        if (!"SUPER_ADMIN".equals(role)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only SUPER_ADMIN can access this");
        return ResponseEntity.ok(businessService.findAll());
    }

    @Operation(summary = "Update a business")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBusiness(@PathVariable Long id, @RequestBody Businesses business, @RequestHeader("Authorization") String authHeader) {
        if (!isAuthorized(authHeader, id)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        business.setId(id);
        return ResponseEntity.ok(businessService.save(business));
    }

    @Operation(summary = "Delete a business with password")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBusinessWithPassword(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        if (!isAuthorized(authHeader, id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String password = request.get("password");
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        boolean deleted = businessService.deleteBusinessByIdWithPassword(id, password);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Business deleted successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Incorrect password or business not found");
        }
    }


    @Operation(summary = "Update business with images")
    @PutMapping("/update-with-images/{id}")
    public ResponseEntity<?> updateBusinessWithImages(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam String description,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String websiteUrl,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) MultipartFile banner) {
        if (!isAuthorized(authHeader, id)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        try {
            Businesses updated = businessService.updateBusinessWithImages(id, name, email, password, description, phoneNumber, websiteUrl, logo, banner);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload error");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Update logo and banner")
    @PutMapping("/update-logo-banner/{id}")
    public ResponseEntity<?> updateBusinessLogoAndBanner(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) MultipartFile banner) {
        if (!isAuthorized(authHeader, id)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        Businesses existing = businessService.findById(id);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Business not found");
        if (logo != null && !logo.isEmpty()) existing.setBusinessLogoUrl(logo.getOriginalFilename());
        if (banner != null && !banner.isEmpty()) existing.setBusinessBannerUrl(banner.getOriginalFilename());
        return ResponseEntity.ok(businessService.save(existing));
    }

    @Operation(summary = "Request password reset")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> sendBusinessResetCode(
            @RequestBody Map<String, String> request) {

        boolean success = businessService.resetPassword(request.get("email"));

        return success
                ? ResponseEntity.ok(Map.of("message", "Reset code sent"))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Business not found"));
    }


    @Operation(summary = "Verify reset code")
    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyBusinessResetCode(
            @RequestBody Map<String, String> request) {

        boolean verified = businessService.verifyResetCode(request.get("email"), request.get("code"));
        
        return verified
                ? ResponseEntity.ok(Map.of("message", "Code verified"))
                : ResponseEntity.badRequest().body(Map.of("message", "Invalid code"));
    }


    @Operation(summary = "Update business password")
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, String>> updateBusinessPassword(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing email or password"));
        }

        boolean updated = businessService.updatePasswordDirectly(email, newPassword);
        return updated
                ? ResponseEntity.ok(Map.of("message", "Password updated"))
                : ResponseEntity.badRequest().body(Map.of("message", "Business not found"));
    }
    
    @DeleteMapping("/delete-logo/{id}")
    public ResponseEntity<?> deleteLogo(@PathVariable Long id) {
        boolean deleted = businessService.deleteBusinessLogo(id);
        if (deleted) {
            return ResponseEntity.ok("Logo deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No logo found to delete.");
        }
    }

    @DeleteMapping("/delete-banner/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        boolean deleted = businessService.deleteBusinessBanner(id);
        if (deleted) {
            return ResponseEntity.ok("Banner deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No banner found to delete.");
        }
    }

    @Operation(summary = "Get all public and active businesses")
    @GetMapping("/public")
    public ResponseEntity<List<Businesses>> getPublicActiveBusinesses() {
        List<Businesses> businesses = businessService.getAllPublicActiveBusinesses();
        return ResponseEntity.ok(businesses);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBusinessStatus(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        if (!isAuthorized(authHeader, id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String newStatus = request.get("status");
        String password = request.get("password");

        if (newStatus == null) {
            return ResponseEntity.badRequest().body("Missing status");
        }

        if ("INACTIVE".equalsIgnoreCase(newStatus)) {
            if (password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body("Password is required to deactivate account.");
            }

            boolean isPasswordValid = businessService.verifyPassword(id, password);
            if (!isPasswordValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password. Status not changed.");
            }
        }

        try {
            Businesses business = businessService.findById(id);
            business.setStatus(BusinessStatus.valueOf(newStatus.toUpperCase()));
            return ResponseEntity.ok(businessService.save(business));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value");
        }
    }

    @Operation(summary = "Toggle public profile visibility")
    @PutMapping("/{id}/visibility")
    public ResponseEntity<?> updateBusinessVisibility(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Boolean> request) {

        if (!isAuthorized(authHeader, id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Boolean isPublic = request.get("isPublicProfile");
        if (isPublic == null) {
            return ResponseEntity.badRequest().body("Missing isPublicProfile value");
        }

        Businesses business = businessService.findById(id);
        business.setIsPublicProfile(isPublic);
        return ResponseEntity.ok(businessService.save(business));
    }



}
