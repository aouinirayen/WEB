package tn.esprit.pidev.controller;

import tn.esprit.pidev.entity.FuelLog;
import tn.esprit.pidev.service.FuelLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel-logs")
@CrossOrigin(origins = "*")
public class FuelLogController {

    private final FuelLogService service;

    public FuelLogController(FuelLogService service) {
        this.service = service;
    }

    @GetMapping
    public List<FuelLog> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public FuelLog getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<FuelLog> getByVehicle(@PathVariable Long vehicleId) {
        return service.getByVehicle(vehicleId);
    }

    @PostMapping
    public ResponseEntity<FuelLog> create(@RequestBody FuelLog log) {
        return ResponseEntity.ok(service.create(log));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuelLog> update(@PathVariable Long id, @RequestBody FuelLog log) {
        return ResponseEntity.ok(service.update(id, log));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
