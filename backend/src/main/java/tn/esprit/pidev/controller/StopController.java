package tn.esprit.pidev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.Stop;
import tn.esprit.pidev.service.StopService;
import java.util.List;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class StopController {

    private final StopService service;

    @GetMapping
    public List<Stop> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stop> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/line/{lineId}")
    public List<Stop> getByLine(@PathVariable Long lineId) {
        return service.getByLine(lineId);
    }

    @PostMapping
    public Stop create(@RequestBody Stop stop) {
        return service.create(stop);
    }

    @PutMapping("/{id}")
    public Stop update(@PathVariable Long id,
                       @RequestBody Stop stop) {
        return service.update(id, stop);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}