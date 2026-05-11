package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.ExpiringDocumentsResponse;
import tn.esprit.pidev.dto.ExpiryStatsDTO;
import tn.esprit.pidev.service.DocumentExpiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for document expiry endpoints
 * Handles document expiration alerts and statistics
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentExpiryController {

    private final DocumentExpiryService documentExpiryService;

    /**
     * GET /api/documents/expiring-soon
     * Returns documents expiring within X days + expiryStats
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<ExpiringDocumentsResponse> getExpiringDocuments(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(documentExpiryService.getExpiringDocuments(days, pageable));
    }

    /**
     * GET /api/documents/expired
     * Returns all expired documents
     */
    @GetMapping("/expired")
    public ResponseEntity<ExpiringDocumentsResponse> getExpiredDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(documentExpiryService.getExpiredDocuments(pageable));
    }

    /**
     * GET /api/documents/expiring-soon/by-user
     * Returns user-specific expiring documents
     */
    @GetMapping("/expiring-soon/by-user")
    public ResponseEntity<ExpiringDocumentsResponse> getUserExpiringDocuments(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(documentExpiryService.getUserExpiringDocuments(userId, days, pageable));
    }

    /**
     * GET /api/documents/expiring-soon/by-type
     * Returns document type-specific expiring documents
     */
    @GetMapping("/expiring-soon/by-type")
    public ResponseEntity<ExpiringDocumentsResponse> getExpiringDocumentsByType(
            @RequestParam Long documentTypeId,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(documentExpiryService.getExpiringDocumentsByType(documentTypeId, days, pageable));
    }

    /**
     * GET /api/documents/expiry-statistics
     * Returns expiry statistics
     */
    @GetMapping("/expiry-statistics")
    public ResponseEntity<ExpiryStatsDTO> getExpiryStatistics() {
        return ResponseEntity.ok(documentExpiryService.getExpiryStatistics());
    }
}

