package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.OperatorZoneDTO;
import tn.esprit.pidev.entity.OperatorZone;
import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.enums.Region;
import tn.esprit.pidev.repository.OperatorZoneRepository;
import tn.esprit.pidev.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OperatorZoneController {

    private final OperatorZoneRepository zoneRepository;
    private final OrganizationRepository organizationRepository;

    @PostMapping
    public ResponseEntity<OperatorZoneDTO> create(@RequestBody OperatorZoneDTO dto) {
        Organization org = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        OperatorZone zone = OperatorZone.builder()
                .governorate(dto.getGovernorate())
                .region(dto.getRegion())
                .isHeadquarter(dto.getIsHeadquarter())
                .coverageType(dto.getCoverageType())
                .organization(org)
                .build();
        OperatorZone saved = zoneRepository.save(zone);
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<OperatorZoneDTO>> getAll() {
        List<OperatorZoneDTO> zones = zoneRepository.findAll()
                .stream().map(z -> OperatorZoneDTO.builder()
                        .id(z.getId())
                        .governorate(z.getGovernorate())
                        .region(z.getRegion())
                        .isHeadquarter(z.getIsHeadquarter())
                        .coverageType(z.getCoverageType())
                        .organizationId(z.getOrganization().getId())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperatorZoneDTO> getById(@PathVariable Long id) {
        return zoneRepository.findById(id)
                .map(z -> OperatorZoneDTO.builder()
                        .id(z.getId())
                        .governorate(z.getGovernorate())
                        .region(z.getRegion())
                        .isHeadquarter(z.getIsHeadquarter())
                        .coverageType(z.getCoverageType())
                        .organizationId(z.getOrganization().getId())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<OperatorZoneDTO>> getByOrganization(
            @PathVariable Long organizationId) {
        List<OperatorZoneDTO> zones = zoneRepository
                .findByOrganizationId(organizationId)
                .stream().map(z -> OperatorZoneDTO.builder()
                        .id(z.getId())
                        .governorate(z.getGovernorate())
                        .region(z.getRegion())
                        .isHeadquarter(z.getIsHeadquarter())
                        .coverageType(z.getCoverageType())
                        .organizationId(z.getOrganization().getId())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(zones);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperatorZoneDTO> update(@PathVariable Long id,
                                                  @RequestBody OperatorZoneDTO dto) {
        OperatorZone existing = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        existing.setGovernorate(dto.getGovernorate());
        existing.setRegion(dto.getRegion());
        existing.setIsHeadquarter(dto.getIsHeadquarter());
        existing.setCoverageType(dto.getCoverageType());
        zoneRepository.save(existing);
        dto.setId(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        zoneRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
