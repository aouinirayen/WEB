package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.PartnerContractDTO;
import tn.esprit.pidev.enums.ContractStatus;
import tn.esprit.pidev.service.PartnerContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartnerContractController {

    private final PartnerContractService contractService;

    @PostMapping
    public ResponseEntity<PartnerContractDTO> create(@Valid @RequestBody PartnerContractDTO dto) {
        return ResponseEntity.ok(contractService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartnerContractDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody PartnerContractDTO dto) {
        return ResponseEntity.ok(contractService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerContractDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PartnerContractDTO>> getAll() {
        return ResponseEntity.ok(contractService.getAll());
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<PartnerContractDTO>> getByOrganization(
            @PathVariable Long organizationId) {
        return ResponseEntity.ok(contractService.getByOrganizationId(organizationId));
    }

    @GetMapping("/partner/{partnerId}")
    public ResponseEntity<List<PartnerContractDTO>> getByPartner(
            @PathVariable Long partnerId) {
        return ResponseEntity.ok(contractService.getByPartnerId(partnerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PartnerContractDTO>> getByStatus(
            @PathVariable ContractStatus status) {
        return ResponseEntity.ok(contractService.getByStatus(status));
    }
}
