
package com.example.nailshop.demo.controller;

import com.example.nailshop.demo.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

@RestController
public class StripeWebhookController {

    private final OrderService orderService;
    private final String webhookSecret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StripeWebhookController(
            OrderService orderService,
            @Value("${stripe.webhook-secret}") String webhookSecret) {

        this.orderService = orderService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        System.out.println("STRIPE: Webhook received");

        // 1. Verify that Stripe sent this webhook.
        Event event;

        try {
            event = Webhook.constructEvent(
                    payload,
                    signature,
                    webhookSecret
            );
        } catch (SignatureVerificationException
                 | IllegalArgumentException exception) {

            System.err.println("STRIPE: Invalid signature");

            return ResponseEntity.badRequest()
                    .body("Invalid webhook signature.");
        }

        String eventType = event.getType();

        System.out.println("STRIPE: Event = " + eventType);

        // 2. Process only relevant Checkout events.
        if (!"checkout.session.completed".equals(eventType)
                && !"checkout.session.async_payment_succeeded"
                        .equals(eventType)) {

            return ResponseEntity.ok("Event ignored.");
        }

        // 3. Read the Session ID from the verified event.
        String sessionId;

        try {
            JsonNode json = objectMapper.readTree(payload);

            JsonNode idNode = json.path("data")
                    .path("object")
                    .path("id");

            sessionId = idNode.isTextual()
                    ? idNode.asText()
                    : null;

            if (sessionId == null
                    || !sessionId.startsWith("cs_")) {

                return ResponseEntity.badRequest()
                        .body("Invalid Checkout Session ID.");
            }

        } catch (JacksonException exception) {

            System.err.println("STRIPE: Invalid event JSON");

            return ResponseEntity.badRequest()
                    .body("Invalid event JSON.");
        }

        // 4. Retrieve current payment information from Stripe.
        Session session;

        try {
            session = Session.retrieve(sessionId);

        } catch (StripeException exception) {

            System.err.println(
                    "STRIPE: Session retrieval failed"
            );
            exception.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Unable to retrieve Stripe session.");
        }

        System.out.println(
                "STRIPE: Session = " + session.getId()
        );

        System.out.println(
                "STRIPE: Payment status = "
                        + session.getPaymentStatus()
        );

        // 5. Update the order in our database.
        try {
            orderService.confirmStripePayment(session);

            System.out.println(
                    "STRIPE: Order processing completed"
            );

        } catch (RuntimeException exception) {

            System.err.println(
                    "STRIPE: Order processing failed"
            );
            exception.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Order processing failed.");
        }

        return ResponseEntity.ok("Webhook received.");
    }
}
