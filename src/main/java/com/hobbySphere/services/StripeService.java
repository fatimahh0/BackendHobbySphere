package com.hobbySphere.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.EphemeralKey;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.EphemeralKeyCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Map<String, String> createPaymentIntentWithTracking(int amount, String currency) throws StripeException {
        try {
            // 1. Create Customer
            CustomerCreateParams customerParams = CustomerCreateParams.builder().build();
            Customer customer = Customer.create(customerParams);

            // 2. Create PaymentIntent
            PaymentIntentCreateParams intentParams = PaymentIntentCreateParams.builder()
                    .setAmount((long) amount)
                    .setCurrency(currency)
                    .setCustomer(customer.getId())
                    .addPaymentMethodType("card")
                    .build();

            PaymentIntent intent = PaymentIntent.create(intentParams);

            // 3. Create EphemeralKey
            EphemeralKeyCreateParams ephemeralKeyParams = EphemeralKeyCreateParams.builder()
                    .setCustomer(customer.getId())
                    .setStripeVersion("2023-08-16")
                    .build();

            EphemeralKey ephemeralKey = EphemeralKey.create(ephemeralKeyParams);

            // 4. Return to frontend
            Map<String, String> result = new HashMap<>();
            result.put("clientSecret", intent.getClientSecret());
            result.put("paymentIntentId", intent.getId());
            result.put("customerId", customer.getId());
            result.put("ephemeralKey", ephemeralKey.getSecret());

            return result;

        } catch (StripeException e) {
            e.printStackTrace(); // Show error in logs
            throw e; // ✅ Rethrow the original exception
        }
    }


    public String refundPayment(String paymentIntentId) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();

        Refund refund = Refund.create(params);
        return refund.getId();
    }
}
