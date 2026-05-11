package tn.esprit.pidev.controller;

import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.service.DigitalSignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts/signature")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DigitalSignatureController {

    private final DigitalSignatureService signatureService;

    /**
     * Sign a contract digitally
     */
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<Map<String, Object>> signContract(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "Admin") String signedBy) {
        Map<String, Object> response = new HashMap<>();
        try {
            PartnerContract signed = signatureService.signContract(contractId, signedBy);
            response.put("status", "success");
            response.put("message", "Contract signed successfully");
            response.put("contractId", contractId);
            response.put("signatureHash", signed.getSignatureHash());
            response.put("contentHash", signed.getContentHash());
            response.put("signedAt", signed.getSignedAt());
            response.put("signedBy", signed.getSignedBy());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verify contract signature - Fraud Detection
     */
    @GetMapping("/{contractId}/verify")
    public ResponseEntity<Map<String, Object>> verifySignature(@PathVariable Long contractId) {
        Map<String, Object> response = new HashMap<>();
        try {
            DigitalSignatureService.FraudCheckResult result = signatureService.verifySignature(contractId);
            response.put("fraudDetected", result.fraudDetected());
            response.put("signatureValid", result.signatureValid());
            response.put("message", result.message());
            response.put("contractId", contractId);
            response.put("signedBy", result.contract().getSignedBy());
            response.put("signedAt", result.contract().getSignedAt());
            response.put("contentHash", result.contract().getContentHash());
            response.put("signatureHash", result.contract().getSignatureHash());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get signature status of a contract
     */
    @GetMapping("/{contractId}/status")
    public ResponseEntity<Map<String, Object>> getSignatureStatus(@PathVariable Long contractId) {
        Map<String, Object> response = new HashMap<>();
        try {
            DigitalSignatureService.FraudCheckResult result = signatureService.verifySignature(contractId);
            response.put("isSigned", result.contract().getIsSigned());
            response.put("signatureValid", result.contract().getSignatureValid());
            response.put("signedBy", result.contract().getSignedBy());
            response.put("signedAt", result.contract().getSignedAt());
            response.put("fraudDetected", result.fraudDetected());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("isSigned", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
