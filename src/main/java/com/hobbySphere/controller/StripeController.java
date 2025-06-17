package com.hobbySphere.controller;

import com.hobbySphere.entities.Activities;
import com.hobbySphere.entities.Users;
import com.hobbySphere.services.ActivityBookingService;
import com.hobbySphere.services.ActivityService;
import com.hobbySphere.services.StripeService;
import com.hobbySphere.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class StripeController {

    @Autowired
    private StripeService stripeService;
    
   

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            int amount = (int) request.get("amount");
            String currency = (String) request.get("currency");

            String clientSecret = stripeService.createPaymentIntent(amount, currency);
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


}
