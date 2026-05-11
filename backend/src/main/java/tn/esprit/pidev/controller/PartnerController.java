package tn.esprit.pidev.controller;
import tn.esprit.pidev.dto.PartnerDTO;
import tn.esprit.pidev.enums.PartnerStatus;
import tn.esprit.pidev.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartnerController {
    private final PartnerService partnerService;
    @PostMapping
    public ResponseEntity<PartnerDTO> create(@Valid @RequestBody PartnerDTO dto) {
        return ResponseEntity.ok(partnerService.create(dto));
    }
    @PutMapping("/{id}")
    public ResponseEntity<PartnerDTO> update(@PathVariable Long id, @Valid @RequestBody PartnerDTO dto) {
        return ResponseEntity.ok(partnerService.update(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        partnerService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<PartnerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(partnerService.getById(id));
    }
    @GetMapping
    public ResponseEntity<List<PartnerDTO>> getAll() {
        return ResponseEntity.ok(partnerService.getAll());
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PartnerDTO>> getByStatus(@PathVariable PartnerStatus status) {
        return ResponseEntity.ok(partnerService.getByStatus(status));
    }
    @GetMapping("/by-organization/{orgId}")
    public ResponseEntity<List<PartnerDTO>> getByOrganization(@PathVariable Long orgId) {
        return ResponseEntity.ok(partnerService.getByOrganizationId(orgId));
    }
}
