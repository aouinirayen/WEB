package tn.esprit.pidev.service.admin;

import tn.esprit.pidev.dto.Documents.DocumentTypeCreateRequest;
import tn.esprit.pidev.dto.Documents.DocumentTypeResponse;
import tn.esprit.pidev.entity.DocumentType;
import tn.esprit.pidev.exception.DocumentTypeNotFoundException;
import tn.esprit.pidev.exception.InvalidDocumentException;
import tn.esprit.pidev.repository.DocumentTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service for managing document types (ADMIN operations)
 */
@Service
public class DocumentTypeService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTypeService.class);

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    /**
     * Create new document type
     */
    @Transactional
    public DocumentType createDocumentType(DocumentTypeCreateRequest request) {
        logger.info("Creating new document type: {}", request.getName());

        // Check if name already exists
        if (documentTypeRepository.findByName(request.getName()).isPresent()) {
            throw new InvalidDocumentException("Document type with name '" + request.getName() + "' already exists");
        }

        DocumentType documentType = new DocumentType(
            request.getName(),
            request.getDescription(),
            request.getRequiresExpiry(),
            request.getAllowedRoles()
        );

        DocumentType saved = documentTypeRepository.save(documentType);
        logger.info("✓ Document type created: {} (ID: {})", request.getName(), saved.getId());
        return saved;
    }

    /**
     * Update existing document type
     */
    @Transactional
    public DocumentType updateDocumentType(Long documentTypeId, DocumentTypeCreateRequest request) {
        logger.info("Updating document type: {}", documentTypeId);

        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
            .orElseThrow(() -> new DocumentTypeNotFoundException(documentTypeId));

        // Check if name is being changed to existing one
        if (!documentType.getName().equals(request.getName())) {
            if (documentTypeRepository.findByName(request.getName()).isPresent()) {
                throw new InvalidDocumentException("Document type with name '" + request.getName() + "' already exists");
            }
        }

        documentType.setName(request.getName());
        documentType.setDescription(request.getDescription());
        documentType.setRequiresExpiry(request.getRequiresExpiry());
        documentType.setAllowedRoles(request.getAllowedRoles());

        DocumentType updated = documentTypeRepository.save(documentType);
        logger.info("✓ Document type updated: {}", documentTypeId);
        return updated;
    }

    /**
     * Get document type by ID
     */
    @Transactional(readOnly = true)
    public DocumentType getDocumentTypeById(Long documentTypeId) {
        return documentTypeRepository.findById(documentTypeId)
            .orElseThrow(() -> new DocumentTypeNotFoundException(documentTypeId));
    }

    /**
     * Get document type by name
     */
    @Transactional(readOnly = true)
    public DocumentType getDocumentTypeByName(String name) {
        return documentTypeRepository.findByName(name)
            .orElseThrow(() -> new DocumentTypeNotFoundException("Document type '" + name + "' not found"));
    }

    /**
     * Get all document types (for users to see what they can upload)
     */
    @Transactional(readOnly = true)
    public Page<DocumentType> getAllDocumentTypes(int page, int size) {
        logger.info("Fetching all document types, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return documentTypeRepository.findAll(pageable);
    }

    /**
     * Search document types by keyword
     */
    @Transactional(readOnly = true)
    public Page<DocumentType> searchDocumentTypes(String keyword, int page, int size) {
        logger.info("Searching document types with keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return documentTypeRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * Delete document type (hard delete)
     */
    @Transactional
    public void deleteDocumentType(Long documentTypeId) {
        logger.info("Deleting document type: {}", documentTypeId);

        documentTypeRepository.findById(documentTypeId)
            .orElseThrow(() -> new DocumentTypeNotFoundException(documentTypeId));

        documentTypeRepository.deleteById(documentTypeId);
        logger.info("✓ Document type deleted: {}", documentTypeId);
    }

    /**
     * Convert entity to DTO
     */
    public DocumentTypeResponse toDto(DocumentType documentType) {
        return new DocumentTypeResponse(
            documentType.getId(),
            documentType.getName(),
            documentType.getDescription(),
            documentType.getRequiresExpiry(),
            documentType.getAllowedRoles()
        );
    }
}

