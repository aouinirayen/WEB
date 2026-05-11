package tn.esprit.pidev.controller;

import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.repository.PartnerContractRepository;
import tn.esprit.pidev.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contracts/reminders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContractReminderController {

    private final ScheduledJobService scheduledJobService;
    private final PartnerContractRepository contractRepository;

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerReminderCheck() {
        Map<String, Object> response = new HashMap<>();
        try {
            scheduledJobService.checkExpiringContractsJob();
            response.put("status", "success");
            response.put("message", "Contract expiry check triggered successfully");
            response.put("timestamp", new Date());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringContracts(
            @RequestParam(defaultValue = "30") int days) {
        try {
            Date now = new Date();
            Date deadline = new Date(now.getTime() + (long) days * 24 * 60 * 60 * 1000);

            List<Map<String, Object>> expiring = contractRepository.findAll().stream()
                .filter(c -> c.getEndDate() != null && c.getEndDate().before(deadline) && c.getEndDate().after(now))
                .map(c -> {
                    long daysLeft = TimeUnit.MILLISECONDS.toDays(c.getEndDate().getTime() - now.getTime());
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", c.getId());
                    item.put("contractType", c.getContractType());
                    item.put("status", c.getStatus());
                    item.put("endDate", c.getEndDate());
                    item.put("daysLeft", daysLeft);
                    item.put("partnerName", c.getPartner() != null ? c.getPartner().getName() : "N/A");
                    item.put("partnerEmail", c.getPartner() != null ? c.getPartner().getEmail() : "N/A");
                    item.put("organizationName", c.getOrganization() != null ? c.getOrganization().getName() : "N/A");
                    item.put("urgency", daysLeft <= 7 ? "HIGH" : daysLeft <= 15 ? "MEDIUM" : "LOW");
                    return item;
                })
                .sorted(Comparator.comparingLong(m -> (Long) m.get("daysLeft")))
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", expiring.size());
            response.put("daysChecked", days);
            response.put("contracts", expiring);
            response.put("high", expiring.stream().filter(m -> "HIGH".equals(m.get("urgency"))).count());
            response.put("medium", expiring.stream().filter(m -> "MEDIUM".equals(m.get("urgency"))).count());
            response.put("low", expiring.stream().filter(m -> "LOW".equals(m.get("urgency"))).count());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
