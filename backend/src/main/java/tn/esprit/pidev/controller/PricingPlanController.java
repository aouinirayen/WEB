package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.PricingPlanRequest;
import tn.esprit.pidev.dto.PricingPlanResponse;
import tn.esprit.pidev.entity.PricingType;
import tn.esprit.pidev.service.IPricingPlanService;

import java.util.List;

@RestController
@RequestMapping("/api/pricing-plans")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Pricing Plan", description = "Gestion des plans tarifaires — réservé aux OPERATOR")
public class PricingPlanController {

    private final IPricingPlanService service;

    public PricingPlanController(IPricingPlanService service) {
        this.service = service;
    }

    @PostMapping("/operator/{operatorId}")
    @Operation(summary = "Créer un plan tarifaire (OPERATOR)")
    public ResponseEntity<PricingPlanResponse> create(
            @RequestBody PricingPlanRequest request,
            @PathVariable Long operatorId) {
        return new ResponseEntity<>(service.create(request, operatorId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un plan par ID")
    public ResponseEntity<PricingPlanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les plans — accessible à tous")
    public ResponseEntity<List<PricingPlanResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}/operator/{operatorId}")
    @Operation(summary = "Modifier un plan tarifaire (OPERATOR)")
    public ResponseEntity<PricingPlanResponse> update(
            @PathVariable Long id,
            @RequestBody PricingPlanRequest request,
            @PathVariable Long operatorId) {
        return ResponseEntity.ok(service.update(id, request, operatorId));
    }

    @DeleteMapping("/{id}/operator/{operatorId}")
    @Operation(summary = "Supprimer un plan tarifaire (OPERATOR)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @PathVariable Long operatorId) {
        service.delete(id, operatorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-type/{type}")
    @Operation(summary = "Filtrer par type (FREE / BASIC / PREMIUM)")
    public ResponseEntity<List<PricingPlanResponse>> byType(@PathVariable PricingType type) {
        return ResponseEntity.ok(service.getByType(type));
    }

    @GetMapping("/by-max-price/{max}")
    @Operation(summary = "Filtrer par prix maximum")
    public ResponseEntity<List<PricingPlanResponse>> byMaxPrice(@PathVariable Double max) {
        return ResponseEntity.ok(service.getByMaxPrice(max));
    }

    @GetMapping("/operator/{operatorId}")
    @Operation(summary = "Plans créés par un opérateur donné")
    public ResponseEntity<List<PricingPlanResponse>> byOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(service.getByOperator(operatorId));
    }
}
