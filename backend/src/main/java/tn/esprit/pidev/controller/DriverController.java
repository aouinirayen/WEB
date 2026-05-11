package tn.esprit.pidev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.Driver;
import tn.esprit.pidev.service.DriverService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pidev.service.LicenseValidationService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService service;

    @GetMapping
    public List<Driver> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public List<Driver> getPending() {
        return service.getPendingValidations();
    }

    @PostMapping
    public Driver create(@RequestBody Driver driver) {
        return service.create(driver);
    }

    @PutMapping("/{id}")
    public Driver update(@PathVariable Long id,
                         @RequestBody Driver driver) {
        return service.update(id, driver);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Métier Avancé 2 — approve license
    @PutMapping("/{id}/approve")
    public Driver approve(@PathVariable Long id) {
        return service.approveLicense(id);
    }

    // Métier Avancé 2 — reject license
    @PutMapping("/{id}/reject")
    public Driver reject(@PathVariable Long id,
                         @RequestBody Map<String, String> body) {
        return service.rejectLicense(id, body.get("reason"));
    }

    // Métier Avancé 1 — auto assign
    @PostMapping("/assign/{vehicleId}")
    public Driver autoAssign(@PathVariable Long vehicleId) {
        return service.autoAssignDriver(vehicleId);
    }

    // Unassign
    @PutMapping("/{id}/unassign")
    public Driver unassign(@PathVariable Long id) {
        return service.unassignDriver(id);
    }
    @Autowired
    private LicenseValidationService licenseValidationService;
    // Upload and AI-validate license
    @PostMapping("/{id}/upload-license")
    public ResponseEntity<?> uploadLicense(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Driver driver = licenseValidationService
                    .uploadAndValidateLicense(id, file);
            return ResponseEntity.ok(driver);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Serve the license image
    @GetMapping("/license-image/{filename}")
    public ResponseEntity<Resource> getLicenseImage(
            @PathVariable String filename) throws IOException {
        Path filePath = Paths.get("uploads/licenses")
                .resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }

}