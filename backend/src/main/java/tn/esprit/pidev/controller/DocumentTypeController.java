package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.Documents.DocumentTypeResponse;
import tn.esprit.pidev.entity.DocumentType;
import tn.esprit.pidev.service.admin.DocumentTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * REST Controller for Document Type Operations (USER side)
 * Allows authenticated users to view available document types
 */
@RestController(value = "userDocumentTypeController")
@RequestMapping("/api/document-types")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
public class DocumentTypeController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTypeController.class);

    @Autowired
    private DocumentTypeService documentTypeService;

    /**
     * GET /api/document-types - Get all document types (for users to browse)
     * Returns all available document types for uploading documents
     */
    @GetMapping
    public ResponseEntity<Page<DocumentTypeResponse>> getAllDocumentTypes(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be less than 0") int page,
            @RequestParam(defaultValue = "100") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 500, message = "Page size cannot exceed 500") int size) {

        logger.info("GET /api/document-types - User fetching available document types, page: {}, size: {}", page, size);

        Page<DocumentType> documentTypes = documentTypeService.getAllDocumentTypes(page, size);
        Page<DocumentTypeResponse> responseData = documentTypes.map(documentTypeService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * GET /api/document-types/{id} - Get specific document type details
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentTypeResponse> getDocumentTypeById(@PathVariable Long id) {
        logger.info("GET /api/document-types/{} - User fetching document type details", id);

        DocumentType documentType = documentTypeService.getDocumentTypeById(id);
        DocumentTypeResponse response = documentTypeService.toDto(documentType);

        return ResponseEntity.ok(response);
    }
}

