package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.PartSuggestionDTO;
import tn.esprit.pidev.dto.PartUsageRequest;
import tn.esprit.pidev.entity.MaintenancePartUsage;
import tn.esprit.pidev.entity.SparePart;
import tn.esprit.pidev.service.SparePartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class SparePartController {

    private final SparePartService service;

    public SparePartController(SparePartService service) {
        this.service = service;
    }

    // ── Spare Parts CRUD ──────────────────────────────────────────

    @GetMapping("/api/spare-parts")
    public List<SparePart> getAll() { return service.getAll(); }

    @GetMapping("/api/spare-parts/{id}")
    public SparePart getById(@PathVariable Long id) { return service.getById(id); }

    @GetMapping("/api/spare-parts/low-stock")
    public List<SparePart> getLowStock() { return service.getLowStock(); }

    @PostMapping("/api/spare-parts")
    public ResponseEntity<SparePart> create(@RequestBody SparePart part) {
        return ResponseEntity.ok(service.create(part));
    }

    @PutMapping("/api/spare-parts/{id}")
    public ResponseEntity<SparePart> update(@PathVariable Long id, @RequestBody SparePart part) {
        return ResponseEntity.ok(service.update(id, part));
    }

    @DeleteMapping("/api/spare-parts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Auto-suggest ──────────────────────────────────────────────

    /** Returns parts suggested for a maintenance order based on its type */
    @GetMapping("/api/spare-parts/suggest/{maintenanceOrderId}")
    public List<PartSuggestionDTO> suggest(@PathVariable Long maintenanceOrderId) {
        return service.suggestForOrder(maintenanceOrderId);
    }

    // ── Part Usage ────────────────────────────────────────────────

    /** Record parts used → auto-deducts stock */
    @PostMapping("/api/part-usages")
    public ResponseEntity<MaintenancePartUsage> recordUsage(@RequestBody PartUsageRequest req) {
        return ResponseEntity.ok(service.recordUsage(req));
    }

    @GetMapping("/api/part-usages/by-order/{orderId}")
    public List<MaintenancePartUsage> byOrder(@PathVariable Long orderId) {
        return service.getUsageByOrder(orderId);
    }

    @GetMapping("/api/part-usages/by-part/{partId}")
    public List<MaintenancePartUsage> byPart(@PathVariable Long partId) {
        return service.getUsageByPart(partId);
    }

    @GetMapping("/api/part-usages/cost/{orderId}")
    public Double totalCostForOrder(@PathVariable Long orderId) {
        return service.getTotalCostForOrder(orderId);
    }

    /** Delete usage → automatically restores stock */
    @DeleteMapping("/api/part-usages/{id}")
    public ResponseEntity<Void> deleteUsage(@PathVariable Long id) {
        service.deleteUsage(id);
        return ResponseEntity.noContent().build();
    }
}
