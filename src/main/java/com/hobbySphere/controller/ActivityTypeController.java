package com.hobbySphere.controller;

import com.hobbySphere.dto.ActivityTypeDTO;
import com.hobbySphere.entities.ActivityType;
import com.hobbySphere.repositories.ActivityTypeRepository;
import com.hobbySphere.services.ActivityTypeService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175"
})

public class ActivityTypeController {

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

    @Autowired
    private ActivityTypeService activityTypeService;

    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    
    @GetMapping
    public List<ActivityType> getAll() {
        return activityTypeRepository.findAllByOrderByNameAsc();
        
    }


    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @DeleteMapping("/{id}")
    
    public void delete(@PathVariable Long id) {
        activityTypeRepository.deleteById(id);
    }


    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @PostMapping("/seed-defaults")
    public String seedDefaults() {
        activityTypeService.ensureActivityTypes();
        return "Default activity types and interests seeded successfully.";
    }
    
    @GetMapping("/guest")
    public List<ActivityTypeDTO> getAllActivityTypes() {
        return activityTypeRepository.findAllByOrderByNameAsc()
            .stream()
            .map(type -> {
                String rawName = type.getName();
                String displayName = Arrays.stream(com.hobbySphere.enums.ActivityTypeEnum.values())
                        .filter(e -> e.name().equals(rawName))
                        .findFirst()
                        .map(com.hobbySphere.enums.ActivityTypeEnum::getDisplayName)
                        .orElse(rawName); // fallback

                return new ActivityTypeDTO(
                    type.getId(),
                    rawName,
                    displayName, // ✅ Pass display name here
                    type.getIcon() != null ? type.getIcon().name() : null,
                    type.getIconLib() != null ? type.getIconLib().name() : null
                );
            })
            .toList();
    }

 

}