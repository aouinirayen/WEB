package tn.esprit.pidev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.Schedule;
import tn.esprit.pidev.service.ScheduleService;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService service;

    @GetMapping
    public List<Schedule> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/line/{lineId}")
    public List<Schedule> getByLine(@PathVariable Long lineId) {
        return service.getByLine(lineId);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<Schedule> getByVehicle(
            @PathVariable Long vehicleId) {
        return service.getByVehicle(vehicleId);
    }

    @PostMapping
    public Schedule create(@RequestBody Schedule schedule) {
        return service.create(schedule);
    }

    @PutMapping("/{id}")
    public Schedule update(@PathVariable Long id,
                           @RequestBody Schedule schedule) {
        return service.update(id, schedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}