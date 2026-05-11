package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.service.IStripePaymentService;

@RestController
@RequestMapping("/webhooks")
@CrossOrigin(origins = "*", maxAge = 3600)
@Hidden
public class StripeWebhookController {

    private final IStripePaymentService paymentService;

    public StripeWebhookController(IStripePaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint public (sans JWT) : configurez l'URL dans Stripe Dashboard + signing secret.
     * Événement principal : {@code checkout.session.completed} — idempotent avec GET /payment/success.
     */
    @PostMapping(value = "/stripe", consumes = "application/json")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature) {
        if (stripeSignature == null || stripeSignature.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature");
        }
        try {
            paymentService.processStripeWebhook(payload, stripeSignature);
            return ResponseEntity.ok("ok");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
