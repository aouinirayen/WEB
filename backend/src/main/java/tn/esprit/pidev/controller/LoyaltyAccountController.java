package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.LoyaltyAccountResponse;
import tn.esprit.pidev.dto.RedeemRequest;
import tn.esprit.pidev.entity.LoyaltyTier;
import tn.esprit.pidev.service.ILoyaltyAccountService;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty-accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Loyalty Account", description = "Programme de fidélité des passagers")
public class LoyaltyAccountController {

    private final ILoyaltyAccountService service;

    public LoyaltyAccountController(ILoyaltyAccountService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un compte fidélité par ID")
    public ResponseEntity<LoyaltyAccountResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Récupérer le compte fidélité d'un passager")
    public ResponseEntity<LoyaltyAccountResponse> byPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.getByPassenger(passengerId));
    }

    @GetMapping
    @Operation(summary = "Tous les comptes fidélité — ADMIN / OPERATOR")
    public ResponseEntity<List<LoyaltyAccountResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/by-tier/{tier}")
    @Operation(summary = "Filtrer par niveau (BRONZE / SILVER / GOLD)")
    public ResponseEntity<List<LoyaltyAccountResponse>> byTier(@PathVariable LoyaltyTier tier) {
        return ResponseEntity.ok(service.getByTier(tier));
    }

    @PostMapping("/redeem/passenger/{passengerId}")
    @Operation(summary = "Utiliser des points pour obtenir une réduction (PASSENGER)")
    public ResponseEntity<LoyaltyAccountResponse> redeem(
            @PathVariable Long passengerId,
            @RequestBody RedeemRequest request) {
        return ResponseEntity.ok(service.redeemPoints(passengerId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un compte fidélité — ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
