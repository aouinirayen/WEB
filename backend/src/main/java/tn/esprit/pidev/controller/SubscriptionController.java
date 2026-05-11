package tn.esprit.pidev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.AutoRenewalUpdateRequest;
import tn.esprit.pidev.dto.SubscriptionRequest;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.SubscriptionStatus;
import tn.esprit.pidev.scheduler.AutoRenewalScheduler;
import tn.esprit.pidev.service.ISubscriptionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Subscription", description = "Subscription management — PASSENGER only")
public class SubscriptionController {

    private final ISubscriptionService    service;
    private final AutoRenewalScheduler    scheduler;

    public SubscriptionController(ISubscriptionService service,
                                  AutoRenewalScheduler scheduler) {
        this.service   = service;
        this.scheduler = scheduler;
    }

    @PostMapping("/passenger/{passengerId}")
    @Operation(summary = "Subscribe to a plan (PASSENGER) — loyalty points generated automatically")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @RequestBody SubscriptionRequest request,
            @PathVariable Long passengerId) {
        return new ResponseEntity<>(service.subscribe(request, passengerId), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a subscription by ID")
    public ResponseEntity<SubscriptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all subscriptions — ADMIN / OPERATOR")
    public ResponseEntity<List<SubscriptionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/operator/{operatorId}")
    @Operation(summary = "Subscriptions linked to an operator's plans")
    public ResponseEntity<List<SubscriptionResponse>> byOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(service.getByOperator(operatorId));
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Subscriptions of a passenger")
    public ResponseEntity<List<SubscriptionResponse>> byPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(service.getByPassenger(passengerId));
    }

    @GetMapping("/by-statut/{statut}")
    @Operation(summary = "Filter by status (ACTIVE / EXPIRED / CANCELLED)")
    public ResponseEntity<List<SubscriptionResponse>> byStatut(@PathVariable SubscriptionStatus statut) {
        return ResponseEntity.ok(service.getByStatut(statut));
    }

    @PutMapping("/{id}/cancel/passenger/{passengerId}")
    @Operation(summary = "Cancel a subscription (PASSENGER)")
    public ResponseEntity<SubscriptionResponse> cancel(
            @PathVariable Long id,
            @PathVariable Long passengerId) {
        return ResponseEntity.ok(service.cancel(id, passengerId));
    }

    @RequestMapping(
            value = "/{id}/auto-renewal/passenger/{passengerId}",
            method = {RequestMethod.PUT, RequestMethod.PATCH},
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Enable / disable auto-renewal (PASSENGER, active subscription only)")
    public ResponseEntity<SubscriptionResponse> updateAutoRenewal(
            @PathVariable Long id,
            @PathVariable Long passengerId,
            @RequestBody @Valid AutoRenewalUpdateRequest body) {
        if (body.getAutoRenewal() == null) {
            throw new RuntimeException("Field autoRenewal is required (true or false).");
        }
        return ResponseEntity.ok(service.updateAutoRenewal(id, passengerId, body.getAutoRenewal()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a subscription — ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── TEST ENDPOINT — run the scheduler manually for a given date ──────────
    // Usage: GET /PI/subscriptions/test-scheduler?date=2025-04-27
    // This simulates "today is <date>" — useful to test without waiting for 8am cron.
    // REMOVE this endpoint before going to production.
    @GetMapping("/test-scheduler")
    @Operation(summary = "DEV ONLY — trigger renewal scheduler for a given date")
    public ResponseEntity<Map<String, String>> testScheduler(
            @RequestParam(defaultValue = "") String date) {
        LocalDate ref = date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        scheduler.processRenewalsAndNotifications(ref);
        return ResponseEntity.ok(Map.of(
                "status", "executed",
                "referenceDate", ref.toString(),
                "message", "Scheduler ran for date " + ref + ". Check server logs and emails."
        ));
    }
}
