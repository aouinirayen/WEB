package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.dto.PaymentInitRequest;
import tn.esprit.pidev.dto.PaymentInitMeRequest;
import tn.esprit.pidev.dto.PaymentInitResponse;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.UserRepository;
import tn.esprit.pidev.service.IStripePaymentService;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Paiement Stripe",
        description = "Initiation et confirmation des paiements via Stripe")
public class PaymentController {

    private final IStripePaymentService paymentService;
    private final UserRepository userRepo;

    public PaymentController(IStripePaymentService paymentService, UserRepository userRepo) {
        this.paymentService = paymentService;
        this.userRepo = userRepo;
    }

    @PostMapping("/initiate")
    @Operation(summary = "Initier un paiement Stripe — retourne l'URL checkout")
    public ResponseEntity<PaymentInitResponse> initiate(
            @Valid @RequestBody PaymentInitRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @PostMapping("/initiate/me")
    @Operation(summary = "Initier un paiement Stripe pour l'utilisateur courant (PASSENGER)")
    public ResponseEntity<PaymentInitResponse> initiateMe(@Valid @RequestBody PaymentInitMeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("Utilisateur non authentifié.");
        }
        String username = auth.getName();

        User passenger = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + username));
        if (passenger.getRole() != RoleEnum.PASSENGER) {
            throw new RuntimeException("Accès refusé : seuls les PASSENGER peuvent initier un paiement.");
        }

        PaymentInitRequest req = new PaymentInitRequest();
        req.setPassengerId(passenger.getId());
        req.setPricingPlanId(request.getPricingPlanId());
        req.setCodeReduction(request.getCodeReduction());
        req.setAutoRenewal(request.getAutoRenewal());
        req.setPaymentMode(request.getPaymentMode());
        req.setPointsToUse(request.getPointsToUse());

        return ResponseEntity.ok(paymentService.initiatePayment(req));
    }

    @GetMapping("/success")
    @Operation(summary = "Callback Stripe après paiement réussi — abonnement actif")
    public ResponseEntity<SubscriptionResponse> success(
            @RequestParam String session_id) {
        return ResponseEntity.ok(paymentService.confirmPayment(session_id));
    }

    @GetMapping("/cancel")
    @Operation(summary = "Stripe callback after cancellation")
    public ResponseEntity<Map<String, String>> cancel() {
        return ResponseEntity.ok(Map.of(
                "status", "CANCELLED",
                "message", "Payment canceled by user."
        ));
    }
}