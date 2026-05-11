package tn.esprit.pidev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.MaintenanceOrder;
import tn.esprit.pidev.service.MaintenanceService;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService service;

    @GetMapping
    public List<MaintenanceOrder> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceOrder> getById(
            @PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<MaintenanceOrder> getByVehicle(
            @PathVariable Long vehicleId) {
        return service.getByVehicle(vehicleId);
    }

    @GetMapping("/alerts")
    public List<MaintenanceOrder> getAlerts() {
        return service.getUpcomingAlerts();
    }

    @PostMapping
    public MaintenanceOrder create(
            @RequestBody MaintenanceOrder order) {
        return service.create(order);
    }

    @PutMapping("/{id}")
    public MaintenanceOrder update(@PathVariable Long id,
                                   @RequestBody MaintenanceOrder order) {
        return service.update(id, order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}