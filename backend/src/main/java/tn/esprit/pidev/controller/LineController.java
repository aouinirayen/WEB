package tn.esprit.pidev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.Line;
import tn.esprit.pidev.service.LineService;
import java.util.List;

@RestController
@RequestMapping("/api/lines")
@RequiredArgsConstructor
public class LineController {

    private final LineService service;

    @GetMapping
    public List<Line> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Line> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Line create(@RequestBody Line line) {
        return service.create(line);
    }

    @PutMapping("/{id}")
    public Line update(@PathVariable Long id,
                       @RequestBody Line line) {
        return service.update(id, line);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}