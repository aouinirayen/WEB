package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.ReductionRequest;
import tn.esprit.pidev.dto.ReductionResponse;
import tn.esprit.pidev.service.IReductionService;

import java.util.List;

@RestController
@RequestMapping("/api/reductions")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Reduction", description = "Gestion des réductions — réservé aux OPERATOR")
public class ReductionController {

    private final IReductionService service;

    public ReductionController(IReductionService service) {
        this.service = service;
    }

    @PostMapping("/operator/{operatorId}")
    @Operation(summary = "Créer une réduction (OPERATOR)")
    public ResponseEntity<ReductionResponse> create(
            @RequestBody ReductionRequest request,
            @PathVariable Long operatorId) {
        return new ResponseEntity<>(service.create(request, operatorId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une réduction par ID")
    public ResponseEntity<ReductionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Récupérer une réduction par code promo")
    public ResponseEntity<ReductionResponse> byCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    @GetMapping
    @Operation(summary = "Toutes les réductions")
    public ResponseEntity<List<ReductionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réduction (OPERATOR)")
    public ResponseEntity<ReductionResponse> update(
            @PathVariable Long id,
            @RequestBody ReductionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une réduction")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/valides")
    @Operation(summary = "Réductions non expirées")
    public ResponseEntity<List<ReductionResponse>> valides() {
        return ResponseEntity.ok(service.getValides());
    }

    @GetMapping("/accessibles/{points}")
    @Operation(summary = "Réductions accessibles selon les points disponibles (PASSENGER)")
    public ResponseEntity<List<ReductionResponse>> accessibles(@PathVariable Integer points) {
        return ResponseEntity.ok(service.getAccessibles(points));
    }

    @GetMapping("/operator/{operatorId}")
    @Operation(summary = "Réductions créées par un opérateur")
    public ResponseEntity<List<ReductionResponse>> byOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(service.getByOperator(operatorId));
    }
}
