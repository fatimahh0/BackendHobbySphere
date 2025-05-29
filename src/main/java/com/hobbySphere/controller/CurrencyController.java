package com.hobbySphere.controller;

import com.hobbySphere.dto.CurrencyRequest;
import com.hobbySphere.entities.Currency;
import com.hobbySphere.enums.CurrencyType;
import com.hobbySphere.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyRepository currencyRepository;

    @PostMapping("/chooseCurrency")
    public ResponseEntity<?> chooseCurrency(@RequestBody CurrencyRequest request) {
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
