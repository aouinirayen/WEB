package tn.esprit.pidev.controller.admin;

import tn.esprit.pidev.dto.Documents.DocumentTypeCreateRequest;
import tn.esprit.pidev.dto.Documents.DocumentTypeResponse;
import tn.esprit.pidev.entity.DocumentType;
import tn.esprit.pidev.service.admin.DocumentTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

/**
 * REST Controller for Document Type Management (ADMIN operations)
 */
@RestController(value = "adminDocumentTypeController")
@RequestMapping("/api/admin/document-types")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class DocumentTypeController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTypeController.class);

    @Autowired
    private DocumentTypeService documentTypeService;

    /**
     * GET /api/admin/document-types - List all document types with pagination
     */
    @GetMapping
    public ResponseEntity<Page<DocumentTypeResponse>> getAllDocumentTypes(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") int size) {

        logger.info("GET /api/admin/document-types - Fetching all document types, page: {}, size: {}", page, size);

        Page<DocumentType> documentTypes = documentTypeService.getAllDocumentTypes(page, size);
        Page<DocumentTypeResponse> responseData = documentTypes.map(documentTypeService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * GET /api/admin/document-types/search - Search document types
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DocumentTypeResponse>> searchDocumentTypes(
            @RequestParam @NotBlank(message = "Search keyword is required") String keyword,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number cannot be less than 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") int size) {

        logger.info("GET /api/admin/document-types/search - Searching with keyword: {}, page: {}, size: {}", keyword, page, size);

        Page<DocumentType> documentTypes = documentTypeService.searchDocumentTypes(keyword, page, size);
        Page<DocumentTypeResponse> responseData = documentTypes.map(documentTypeService::toDto);

        return ResponseEntity.ok(responseData);
    }

    /**
     * GET /api/admin/document-types/{id} - Get document type by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentTypeResponse> getDocumentTypeById(@PathVariable @NotNull(message = "Document type ID is required") @Positive(message = "Document type ID must be positive") Long id) {
        logger.info("GET /api/admin/document-types/{} - Fetching document type", id);

        DocumentType documentType = documentTypeService.getDocumentTypeById(id);
        DocumentTypeResponse response = documentTypeService.toDto(documentType);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/document-types - Create new document type
     */
    @PostMapping
    public ResponseEntity<DocumentTypeResponse> createDocumentType(
            @Valid @RequestBody DocumentTypeCreateRequest request) {

        logger.info("POST /api/admin/document-types - Creating document type: {}", request.getName());

        DocumentType documentType = documentTypeService.createDocumentType(request);
        DocumentTypeResponse response = documentTypeService.toDto(documentType);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/admin/document-types/{id} - Update document type
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentTypeResponse> updateDocumentType(
            @PathVariable @NotNull(message = "Document type ID is required") @Positive(message = "Document type ID must be positive") Long id,
            @Valid @RequestBody DocumentTypeCreateRequest request) {

        logger.info("PUT /api/admin/document-types/{} - Updating document type", id);

        DocumentType documentType = documentTypeService.updateDocumentType(id, request);
        DocumentTypeResponse response = documentTypeService.toDto(documentType);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/document-types/{id} - Delete document type (hard delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentType(@PathVariable @NotNull(message = "Document type ID is required") @Positive(message = "Document type ID must be positive") Long id) {
        logger.info("DELETE /api/admin/document-types/{} - Deleting document type", id);

        documentTypeService.deleteDocumentType(id);
        return ResponseEntity.noContent().build();
    }
}

