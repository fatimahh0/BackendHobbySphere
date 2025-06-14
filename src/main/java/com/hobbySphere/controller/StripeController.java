package com.hobbySphere.controller;

import com.hobbySphere.services.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestBody Map<String, Object> request) throws Exception {
        int amount = (int) request.get("amount"); // amount in cents
        String currency = (String) request.get("currency");

        String clientSecret = stripeService.createPaymentIntent(amount, currency);
        return Map.of("clientSecret", clientSecret);
    }
}
