package com.hobbySphere.controller;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.services.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @Operation(summary = "Get a business by ID", description = "This API retrieves a business by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Business retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Business not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Businesses> getBusinessById(@PathVariable Long id) {
        Businesses business = businessService.findById(id);
        if (business != null) {
            return ResponseEntity.ok(business);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get all businesses", description = "This API retrieves all businesses.")
    @ApiResponse(responseCode = "200", description = "List of all businesses")
    @GetMapping
    public ResponseEntity<List<Businesses>> getAllBusinesses() {
        List<Businesses> businesses = businessService.findAll();
        return ResponseEntity.ok(businesses);
    }

    @Operation(summary = "Update an existing business", description = "This API allows the update of an existing business.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Business updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBusiness(@PathVariable Long id, @RequestBody Businesses business) {
        try {
            business.setId(id);
            Businesses updatedBusiness = businessService.save(business);
            return ResponseEntity.ok(updatedBusiness);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete a business by ID", description = "This API deletes a business by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Business deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Business not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        if (businessService.findById(id) != null) {
            businessService.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Register a business with images", description = "This API registers a new business along with a logo and banner image.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Business registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Error uploading files")
    })
    @PostMapping("/register")
    public ResponseEntity<Businesses> registerBusinessWithImages(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String description,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String websiteUrl,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) MultipartFile banner
    ) {
        try {
            Businesses business = businessService.registerBusiness(
                name, email, password, description, phoneNumber, websiteUrl, logo, banner
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(business);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update an existing business with images", description = "This API updates an existing business along with its logo and banner image.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Business updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Error uploading files")
    })
    @PutMapping("/update-with-images/{id}")
    public ResponseEntity<?> updateBusinessWithImages(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String description,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String websiteUrl,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) MultipartFile banner
    ) {
        try {
            Businesses updated = businessService.updateBusinessWithImages(
                    id, name, email, password, description, phoneNumber, websiteUrl, logo, banner
            );
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading files.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Update business logo and banner", description = "This API updates only the business logo and banner images.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logo and banner updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Business not found"),
        @ApiResponse(responseCode = "500", description = "Error uploading files")
    })
    @PutMapping("/update-logo-banner/{id}")
    public ResponseEntity<?> updateBusinessLogoAndBanner(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) MultipartFile banner
    ) {
        Businesses existingBusiness = businessService.findById(id);
        if (existingBusiness == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Business not found");
        }

        if (logo != null && !logo.isEmpty()) {
            existingBusiness.setBusinessLogoUrl(logo.getOriginalFilename());
        }

        if (banner != null && !banner.isEmpty()) {
            existingBusiness.setBusinessBannerUrl(banner.getOriginalFilename());
        }

        Businesses updated = businessService.save(existingBusiness);
        return ResponseEntity.ok(updated);
    }
}
