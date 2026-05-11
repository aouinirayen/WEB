package tn.esprit.pidev.controller.admin;

import tn.esprit.pidev.dto.Documents.DocumentApprovalRequest;
import tn.esprit.pidev.dto.Documents.LegalDocumentResponse;
import tn.esprit.pidev.dto.Documents.DocumentSearchCriteria;
import tn.esprit.pidev.entity.LegalDocument;
import tn.esprit.pidev.entity.DocumentStatusEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.service.admin.LegalDocumentService;
import tn.esprit.pidev.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * REST Controller for Document Management (ADMIN operations)
 */
@RestController
@RequestMapping("/api/admin/documents")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDocumentController.class);

    @Autowired
    private LegalDocumentService legalDocumentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/admin/documents - List all documents with advanced search
     */
    @GetMapping
    public ResponseEntity<Page<LegalDocumentResponse>> searchDocuments(
            @RequestParam(required = false) @Positive(message = "User ID must be positive") Long userId,
            @RequestParam(required = false) @Positive(message = "Document type ID must be positive") Long documentTypeId,
            @RequestParam(required = false) DocumentStatusEnum status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") int size,
            @RequestParam(defaultValue = "uploadDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        logger.info("GET /api/admin/documents - Searching with filters: userId={}, documentTypeId={}, status={}, page: {}, size: {}", 
                   userId, documentTypeId, status, page, size);

        DocumentSearchCriteria criteria = new DocumentSearchCriteria(userId, documentTypeId, status, page, size, sortBy, sortDir);
        Page<LegalDocument> documents = legalDocumentService.searchDocuments(criteria);
        Page<LegalDocumentResponse> responseData = documents.map(legalDocumentService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * GET /api/admin/documents/pending - Get all pending documents for review
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<LegalDocumentResponse>> getPendingDocuments(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") int size) {

        logger.info("GET /api/admin/documents/pending - Fetching pending documents for review, page: {}, size: {}", page, size);

        Page<LegalDocument> documents = legalDocumentService.getPendingDocuments(page, size);
        Page<LegalDocumentResponse> responseData = documents.map(legalDocumentService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * GET /api/admin/documents/{id} - Get document details
     */
    @GetMapping("/{id}")
    public ResponseEntity<LegalDocumentResponse> getDocument(@PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id) {
        logger.info("GET /api/admin/documents/{} - Fetching document details", id);

        LegalDocument document = legalDocumentService.getDocumentByIdAdmin(id);
        LegalDocumentResponse response = legalDocumentService.toDto(document);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/documents/user/{userId} - Get all documents of a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LegalDocumentResponse>> getUserDocuments(
            @PathVariable @NotNull(message = "User ID is required") @Positive(message = "User ID must be positive") Long userId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") int size) {

        logger.info("GET /api/admin/documents/user/{} - Fetching all documents for user {}, page: {}, size: {}", userId, userId, page, size);

        Page<LegalDocument> documents = legalDocumentService.getUserDocuments(userId, page, size);
        Page<LegalDocumentResponse> responseData = documents.map(legalDocumentService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * POST /api/admin/documents/{id}/approve - Approve document
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<LegalDocumentResponse> approveDocument(@PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id) {
        Long adminUserId = getCurrentUserId();
        logger.info("POST /api/admin/documents/{}/approve - Admin {} approving document", id, adminUserId);

        LegalDocument document = legalDocumentService.approveDocument(id, adminUserId);
        LegalDocumentResponse response = legalDocumentService.toDto(document);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/documents/{id}/reject - Reject document
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<LegalDocumentResponse> rejectDocument(
            @PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id,
            @Valid @RequestBody DocumentApprovalRequest request) {

        Long adminUserId = getCurrentUserId();
        logger.info("POST /api/admin/documents/{}/reject - Admin {} rejecting document", id, adminUserId);

        LegalDocument document = legalDocumentService.rejectDocument(id, request.getRejectionReason(), adminUserId);
        LegalDocumentResponse response = legalDocumentService.toDto(document);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/documents/{id}/request-update - Request user to update document
     */
    @PostMapping("/{id}/request-update")
    public ResponseEntity<LegalDocumentResponse> requestDocumentUpdate(
            @PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id,
            @Valid @RequestBody DocumentApprovalRequest request) {

        Long adminUserId = getCurrentUserId();
        logger.info("POST /api/admin/documents/{}/request-update - Admin {} requesting update", id, adminUserId);

        LegalDocument document = legalDocumentService.requestDocumentUpdate(id, request.getRejectionReason(), adminUserId);
        LegalDocumentResponse response = legalDocumentService.toDto(document);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/documents/{id}/toggle-status - Toggle document status between REJECTED and VALID
     * Allows admin to quickly switch a document from approved to rejected and vice versa
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<LegalDocumentResponse> toggleDocumentStatus(
            @PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id) {

        Long adminUserId = getCurrentUserId();
        logger.info("POST /api/admin/documents/{}/toggle-status - Admin {} toggling document status", id, adminUserId);

        LegalDocument document = legalDocumentService.toggleDocumentStatus(id, adminUserId);
        LegalDocumentResponse response = legalDocumentService.toDto(document);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/documents/{id} - Delete document (hard delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id) {
        logger.info("DELETE /api/admin/documents/{} - Admin deleting document", id);

        legalDocumentService.deleteDocumentAdmin(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/admin/documents/check-expired - Manually trigger expired documents check
     */
    @PostMapping("/check-expired")
    public ResponseEntity<String> checkExpiredDocuments() {
        logger.info("POST /api/admin/documents/check-expired - Admin manually triggering expiry check");

        legalDocumentService.checkAndUpdateExpiredDocuments();
        return ResponseEntity.ok("✓ Expired documents check completed");
    }

    /**
     * POST /api/admin/documents/send-notifications - Manually trigger expiry notifications
     */
    @PostMapping("/send-notifications")
    public ResponseEntity<String> sendExpiryNotifications() {
        logger.info("POST /api/admin/documents/send-notifications - Admin manually triggering notifications");

        legalDocumentService.sendExpiryNotifications();
        return ResponseEntity.ok("✓ Expiry notifications sent");
    }

    /**
     * Helper: Get current admin user ID from security context
     * Extracts username from authenticated principal and looks up user ID from database
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            logger.warn("No authenticated admin user found in security context");
            throw new IllegalStateException("Admin user must be authenticated");
        }

        // Get the principal (UserDetails)
        Object principal = auth.getPrincipal();
        String username;
        
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        if (username == null || username.isEmpty()) {
            logger.warn("Could not extract username from authentication");
            throw new IllegalStateException("Could not determine authenticated admin user");
        }

        // Look up user ID from database using username
        final String finalUsername = username;
        User user = userRepository.findByUsername(finalUsername)
                .orElseThrow(() -> {
                    logger.error("Admin user not found in database: {}", finalUsername);
                    return new IllegalStateException("Authenticated admin user not found in database");
                });

        logger.debug("Current admin user ID: {} (username: {})", user.getId(), username);
        return user.getId();
    }
}
