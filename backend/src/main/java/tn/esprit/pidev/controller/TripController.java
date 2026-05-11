package tn.esprit.pidev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.Trip;
import tn.esprit.pidev.service.TripService;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService service;

    @GetMapping
    public List<Trip> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/schedule/{scheduleId}")
    public List<Trip> getBySchedule(
            @PathVariable Long scheduleId) {
        return service.getBySchedule(scheduleId);
    }

    @PostMapping
    public Trip create(@RequestBody Trip trip) {
        return service.create(trip);
    }

    @PutMapping("/{id}")
    public Trip update(@PathVariable Long id,
                       @RequestBody Trip trip) {
        return service.update(id, trip);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}