package com.hobbySphere.controller;

import com.hobbySphere.dto.CurrencyRequest;

import com.hobbySphere.services.CurrencyService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.hobbySphere.entities.AppSettings;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.repositories.AppSettingsRepository;
import com.hobbySphere.repositories.CurrencyRepository;
import com.hobbySphere.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/currencies")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
public class CurrencyController {

    @Autowired
    private CurrencyRepository currencyRepository;
    
    @Autowired
    private AppSettingsRepository appSettingsRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CurrencyService currencyService;


    private boolean isAuthorized(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;

        String jwt = token.substring(7).trim();

        // ✅ Business token check
        if (jwtUtil.isBusinessToken(jwt)) return true;

        // ✅ Admin role check
        String role = jwtUtil.extractRole(jwt);
        if ("SUPER_ADMIN".equals(role) || "MANAGER".equals(role)) return true;

        // ✅ User token check
        return jwtUtil.isUserToken(jwt);
    }


//ADDED
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successful"),
    	    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
    	    @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
    	    @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
    	    @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
    	    @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
    	    @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    	})
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentCurrency(@RequestHeader(value = "Authorization", required = false) String token) {
        // Add token validation for user, business, or admin tokens
        if (token == null || !isAuthorized(token)) {
            // If no valid token, reject access
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied. Valid token required.");
        }

        // Existing code unchanged
        currencyService.ensureDefaultCurrencies();
        AppSettings settings = appSettingsRepository.findById(1L).orElse(null);
        if (settings == null || settings.getCurrency() == null) {
            return ResponseEntity.ok("CAD"); // or your default
        }
        return ResponseEntity.ok(settings.getCurrency().getCurrencyType());
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
    @PostMapping("/chooseCurrency")
    public ResponseEntity<?> chooseCurrency(
            @RequestHeader("Authorization") String token,
            @RequestBody CurrencyRequest request) {

        // Token check already present in main code, so no change needed here
        if (!isAuthorized(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        // Existing code unchanged
        currencyService.ensureDefaultCurrencies();

        String type = request.getCurrencyType() != null ? request.getCurrencyType().toUpperCase() : "CAD";

        Optional<Currency> selectedCurrency = currencyRepository.findByCurrencyType(type);

        if (selectedCurrency.isPresent()) {
            AppSettings settings = appSettingsRepository.findById(1L)
                    .orElse(new AppSettings());

            settings.setCurrency(selectedCurrency.get());
            appSettingsRepository.save(settings);

            return ResponseEntity.ok(selectedCurrency.get());
        } else {
            return ResponseEntity.badRequest().body("Currency type not found in the database.");
        }
    }

}