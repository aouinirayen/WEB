package tn.esprit.pidev.controller;

import tn.esprit.pidev.repository.*;
import tn.esprit.pidev.enums.*;
import tn.esprit.pidev.entity.PartnerContract;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final OrganizationRepository organizationRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerContractRepository contractRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Organizations stats
        long totalOrgs = organizationRepository.count();
        long activeOrgs = organizationRepository.findAll().stream()
            .filter(o -> o.getStatus() == OrgStatus.ACTIVE).count();
        stats.put("totalOrganizations", totalOrgs);
        stats.put("activeOrganizations", activeOrgs);

        // Partners stats
        long totalPartners = partnerRepository.count();
        long activePartners = partnerRepository.findAll().stream()
            .filter(p -> p.getStatus() == PartnerStatus.ACTIVE).count();
        stats.put("totalPartners", totalPartners);
        stats.put("activePartners", activePartners);

        // Contracts stats
        List<PartnerContract> allContracts = contractRepository.findAll();
        long totalContracts = allContracts.size();
        long activeContracts = allContracts.stream()
            .filter(c -> c.getStatus() == ContractStatus.ACTIVE).count();
        long signedContracts = allContracts.stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsSigned())).count();
        long fraudContracts = allContracts.stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsSigned()) && Boolean.FALSE.equals(c.getSignatureValid())).count();

        // Expiring contracts (within 30 days)
        Date now = new Date();
        Date in30Days = new Date(now.getTime() + TimeUnit.DAYS.toMillis(30));
        long expiringContracts = allContracts.stream()
            .filter(c -> c.getEndDate() != null && c.getEndDate().after(now) && c.getEndDate().before(in30Days))
            .count();

        stats.put("totalContracts", totalContracts);
        stats.put("activeContracts", activeContracts);
        stats.put("signedContracts", signedContracts);
        stats.put("fraudContracts", fraudContracts);
        stats.put("expiringContracts", expiringContracts);

        // Contracts by type
        Map<String, Long> contractsByType = new HashMap<>();
        for (ContractType type : ContractType.values()) {
            long count = allContracts.stream().filter(c -> c.getContractType() == type).count();
            contractsByType.put(type.name(), count);
        }
        stats.put("contractsByType", contractsByType);

        // Organizations by coverage
        Map<String, Long> orgsByCoverage = new HashMap<>();
        organizationRepository.findAll().forEach(o -> {
            if (o.getCoverageType() != null) {
                String key = o.getCoverageType().name();
                orgsByCoverage.merge(key, 1L, Long::sum);
            }
        });
        stats.put("organizationsByCoverage", orgsByCoverage);

        return ResponseEntity.ok(stats);
    }
}
