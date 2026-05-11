package tn.esprit.pidev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.service.WeeklyReportService;
import tn.esprit.pidev.service.ExpirationAlertService;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class WeeklyReportController {

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private ExpirationAlertService expirationAlertService;

    @PostMapping("/trigger-weekly")
    public ResponseEntity<?> triggerWeeklyReport() {
        try {
            weeklyReportService.triggerReport();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Weekly report triggered successfully. Check your email in a few seconds."
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", ex.getMessage()
            ));
        }
    }

    @PostMapping("/trigger-expiration-alert")
    public ResponseEntity<?> triggerExpirationAlert() {
        try {
            expirationAlertService.triggerAlert();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Expiration alert triggered. Check your email if contracts expire within 3 days."
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", ex.getMessage()
            ));
        }
    }
}