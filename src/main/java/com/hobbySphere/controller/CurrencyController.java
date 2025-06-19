package com.hobbySphere.controller;

import com.hobbySphere.dto.CurrencyRequest;
import com.hobbySphere.services.CurrencyService;
import com.hobbySphere.entities.AppSettings;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.CurrencyType;
import com.hobbySphere.enums.DefaultCurrencies;
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
@GetMapping("/current")
public ResponseEntity<?> getCurrentCurrency() {
	currencyService.ensureDefaultCurrencies();
    AppSettings settings = appSettingsRepository.findById(1L).orElse(null);
    if (settings == null || settings.getCurrency() == null) {
        return ResponseEntity.ok("CAD"); // or your default
    }
    return ResponseEntity.ok(settings.getCurrency().getCurrencyType().name());
}



    @PostMapping("/chooseCurrency")
    public ResponseEntity<?> chooseCurrency(
            @RequestHeader("Authorization") String token,
            @RequestBody CurrencyRequest request) {

        if (!isAuthorized(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }
        
        currencyService.ensureDefaultCurrencies();


        String type = request.getCurrencyType() != null ? request.getCurrencyType().toUpperCase() : "CAD";

        try {
            CurrencyType currencyType = CurrencyType.valueOf(type);
            Optional<Currency> selectedCurrency = currencyRepository.findByCurrencyType(currencyType);

            if (selectedCurrency.isPresent()) {
                // ✅ Update global setting
                AppSettings settings = appSettingsRepository.findById(1L)
                    .orElse(new AppSettings());
                settings.setCurrency(selectedCurrency.get());
                appSettingsRepository.save(settings);

                return ResponseEntity.ok(selectedCurrency.get());
            } else {
                return ResponseEntity.badRequest().body("Currency type not found in the database.");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid currency type. Choose: DOLLAR, EURO, or CAD.");
        }
    }

}