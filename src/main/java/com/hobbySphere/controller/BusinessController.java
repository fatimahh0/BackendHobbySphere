package com.hobbySphere.controller;

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
@Tag(name = "Business Management", description = "Operations for creating, reading, updating, and deleting businesses")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @Operation(summary = "Create a new business")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Business created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Businesses> createBusiness(@RequestBody Businesses business) {
        Businesses savedBusiness = businessService.save(business);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBusiness);
    }

    @Operation(summary = "Get a business by ID")
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

    @Operation(summary = "Get all businesses")
    @ApiResponse(responseCode = "200", description = "List of all businesses")
    @GetMapping
    public ResponseEntity<List<Businesses>> getAllBusinesses() {
        List<Businesses> businesses = businessService.findAll();
        return ResponseEntity.ok(businesses);
    }

    @Operation(summary = "Update an existing business")
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

    @Operation(summary = "Delete a business by ID")
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
}
