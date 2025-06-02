package com.hobbySphere.controller;

import com.hobbySphere.dto.CurrencyRequest;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.CurrencyType;
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
    private JwtUtil jwtUtil;

    private boolean isAuthorized(String token) {
        if (token == null || !token.startsWith("Bearer ")) return false;

        String jwt = token.substring(7);
        String role = jwtUtil.extractRole(jwt);  // You must have this method in JwtUtil
        return "BUSINESS".equals(role) || "SUPER_ADMIN".equals(role);
    }

    @PostMapping("/chooseCurrency")
    public ResponseEntity<?> chooseCurrency(
            @RequestHeader("Authorization") String token,
            @RequestBody CurrencyRequest request) {

        if (!isAuthorized(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied.");
        }

        String type = request.getCurrencyType() != null ? request.getCurrencyType().toUpperCase() : "CAD";

        try {
            CurrencyType currencyType = CurrencyType.valueOf(type);

            Optional<Currency> selectedCurrency = currencyRepository.findByCurrencyType(currencyType);
            if (selectedCurrency.isPresent()) {
                return ResponseEntity.ok(selectedCurrency.get());
            } else {
                return ResponseEntity.badRequest().body("Currency type not found in the database.");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid currency type. Choose: DOLLAR, EURO, or CAD.");
        }
    }
}
