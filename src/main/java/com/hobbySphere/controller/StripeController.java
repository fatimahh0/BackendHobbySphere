package com.hobbySphere.controller;

import com.hobbySphere.security.JwtUtil;
import com.hobbySphere.services.StripeService;
import com.stripe.model.PaymentIntent;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private JwtUtil jwtUtil;

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful"),
        @ApiResponse(responseCode = "400", description = "Bad Request – Invalid or missing parameters or token"),
        @ApiResponse(responseCode = "401", description = "Unauthorized – Authentication credentials are missing or invalid"),
        @ApiResponse(responseCode = "402", description = "Payment Required – Payment is required to access this resource (reserved)"),
        @ApiResponse(responseCode = "403", description = "Forbidden – You do not have permission to perform this action"),
        @ApiResponse(responseCode = "404", description = "Not Found – The requested resource could not be found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error – An unexpected error occurred on the server")
    })
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> request,
                                                 @RequestHeader("Authorization") String authHeader) {
        // ✅ Validate token
        ResponseEntity<String> tokenCheck = validateUserToken(authHeader);
        if (tokenCheck != null) return tokenCheck;

        try {
            int amount = (int) request.get("amount");
            String currency = (String) request.get("currency");

            if (amount <= 0 || currency == null || currency.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount or currency"));
            }

            // ✅ Get the full PaymentIntent object
            Map<String, String> intentData = stripeService.createPaymentIntentWithTracking(amount, currency);

            return ResponseEntity.ok(Map.of(
            	    "clientSecret", intentData.get("clientSecret"),
            	    "paymentIntentId", intentData.get("paymentIntentId"),
            	    "customerId", intentData.get("customerId"),            // ✅ Needed for PaymentSheet
            	    "ephemeralKey", intentData.get("ephemeralKey")         // ✅ Needed for PaymentSheet
            	));


          

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    // ✅ Refund Endpoint
    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestBody Map<String, Object> request,
                                           @RequestHeader("Authorization") String authHeader) {
        ResponseEntity<String> tokenCheck = validateUserToken(authHeader);
        if (tokenCheck != null) return tokenCheck;

        try {
            String paymentIntentId = (String) request.get("paymentIntentId");

            if (paymentIntentId == null || paymentIntentId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "paymentIntentId is required"));
            }

            String refundId = stripeService.refundPayment(paymentIntentId);
            return ResponseEntity.ok(Map.of(
                "refundId", refundId,
                "message", "Refund successful"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<String> validateUserToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();
        String role = jwtUtil.extractRole(token);

        if (!"USER".equalsIgnoreCase(role) && !"BUSINESS".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized role");
        }

        return null;
    }
}
