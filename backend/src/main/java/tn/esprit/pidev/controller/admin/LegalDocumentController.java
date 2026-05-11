package tn.esprit.pidev.controller.admin;

import tn.esprit.pidev.dto.Documents.LegalDocumentResponse;
import tn.esprit.pidev.entity.LegalDocument;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.exception.FileUploadException;
import tn.esprit.pidev.service.admin.LegalDocumentService;
import tn.esprit.pidev.service.FileUploadService;
import tn.esprit.pidev.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for Legal Document Operations (USER side)
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
public class LegalDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(LegalDocumentController.class);

    @Autowired
    private LegalDocumentService legalDocumentService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/documents - Get all documents for current user
     */
    @GetMapping
    public ResponseEntity<Page<LegalDocumentResponse>> getUserDocuments(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") int size) {

        Long userId = getCurrentUserId();
        logger.info("GET /api/documents - User {} fetching their documents, page: {}, size: {}", userId, page, size);

        Page<LegalDocument> documents = legalDocumentService.getUserDocuments(userId, page, size);
        Page<LegalDocumentResponse> responseData = documents.map(legalDocumentService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * GET /api/documents/{id} - Get specific document (own only)
     */
    @GetMapping("/{id}")
    public ResponseEntity<LegalDocumentResponse> getDocument(@PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id) {
        Long userId = getCurrentUserId();
        logger.info("GET /api/documents/{} - User {} fetching document", id, userId);

        LegalDocument document = legalDocumentService.getDocumentById(id, userId);
        LegalDocumentResponse response = legalDocumentService.toDto(document);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/documents - Upload new document
     * Multipart form data:
     *   - file: Document file
     *   - documentTypeId: ID of document type
     *   - expiryDate: ISO format date (optional, depends on document type)
     *   - customFields: JSON string with custom metadata (optional)
     */
    @PostMapping
    public ResponseEntity<LegalDocumentResponse> uploadDocument(
            @RequestParam @NotNull(message = "Document type ID is required") @Positive(message = "Document type ID must be positive") Long documentTypeId,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String customFields,
            @RequestParam("file") @NotNull(message = "File is required") MultipartFile file) {

        Long userId = getCurrentUserId();
        logger.info("POST /api/documents - User {} uploading document type {}", userId, documentTypeId);

        try {
            LocalDateTime expiryDateTime;
            if (expiryDate != null && !expiryDate.isEmpty()) {
                expiryDateTime = LocalDateTime.parse(expiryDate, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                expiryDateTime = null;
            }

            LegalDocument document = legalDocumentService.uploadDocument(
                userId,
                documentTypeId,
                file,
                expiryDateTime,
                customFields
            );

            LegalDocumentResponse response = legalDocumentService.toDto(document);
            return ResponseEntity.status(201).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid date format: {}", e.getMessage());
            throw new FileUploadException("Invalid expiry date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
    }

    /**
     * GET /api/documents/{id}/download - Download document file
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id) {
        Long userId = getCurrentUserId();
        logger.info("GET /api/documents/{}/download - User {} downloading document", id, userId);

        LegalDocument document = legalDocumentService.getDocumentById(id, userId);
        byte[] fileContent = fileUploadService.downloadFile(document.getDocumentUrl());

        // Determine content type from file extension
        String contentType = determineContentType(document.getDocumentUrl());

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document_" + id + "\"")
            .body(fileContent);
    }

    /**
     * DELETE /api/documents/{id} - Delete document (only PENDING or REJECTED)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id) {
        Long userId = getCurrentUserId();
        logger.info("DELETE /api/documents/{} - User {} deleting document", id, userId);

        legalDocumentService.deleteDocument(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/documents/{id}/reupload - Re-upload document (for REQUEST_UPDATE status)
     */
    @PostMapping("/{id}/reupload")
    public ResponseEntity<LegalDocumentResponse> reuploadDocument(
            @PathVariable @NotNull(message = "Document ID is required") @Positive(message = "Document ID must be positive") Long id,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String customFields,
            @RequestParam("file") @NotNull(message = "File is required") MultipartFile file) {

        Long userId = getCurrentUserId();
        logger.info("POST /api/documents/{}/reupload - User {} re-uploading document", id, userId);

        try {
            // Get original document
            LegalDocument originalDoc = legalDocumentService.getDocumentById(id, userId);

            // Delete original document (soft delete + file removal)
            legalDocumentService.deleteDocument(id, userId);

            // Upload as new document
            LocalDateTime expiryDateTime;
            if (expiryDate != null && !expiryDate.isEmpty()) {
                expiryDateTime = LocalDateTime.parse(expiryDate, DateTimeFormatter.ISO_DATE_TIME);
            } else {
                expiryDateTime = originalDoc.getExpiryDate();
            }

            LegalDocument newDocument = legalDocumentService.uploadDocument(
                userId,
                originalDoc.getDocumentTypeId(),
                file,
                expiryDateTime,
                customFields
            );

            LegalDocumentResponse response = legalDocumentService.toDto(newDocument);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid date format: {}", e.getMessage());
            throw new FileUploadException("Invalid expiry date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
    }

    /**
     * Helper: Get current user ID from security context
     * Extracts username from authenticated principal and looks up user ID from database
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            logger.warn("No authenticated user found in security context");
            throw new IllegalStateException("User must be authenticated");
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
            throw new IllegalStateException("Could not determine authenticated user");
        }

        // Look up user ID from database using username
        final String finalUsername = username;
        User user = userRepository.findByUsername(finalUsername)
                .orElseThrow(() -> {
                    logger.error("User not found in database: {}", finalUsername);
                    return new IllegalStateException("Authenticated user not found in database");
                });

        logger.debug("Current user ID: {} (username: {})", user.getId(), username);
        return user.getId();
    }

    /**
     * Helper: Determine content type based on file extension
     */
    private String determineContentType(String filePath) {
        if (filePath.endsWith(".pdf")) return "application/pdf";
        if (filePath.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (filePath.endsWith(".doc")) return "application/msword";
        if (filePath.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (filePath.endsWith(".xls")) return "application/vnd.ms-excel";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".gif")) return "image/gif";
        if (filePath.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}

