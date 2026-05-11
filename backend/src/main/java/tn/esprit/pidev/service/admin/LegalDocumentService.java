package tn.esprit.pidev.service.admin;

import tn.esprit.pidev.dto.Documents.LegalDocumentResponse;
import tn.esprit.pidev.dto.Documents.DocumentSearchCriteria;
import tn.esprit.pidev.entity.LegalDocument;
import tn.esprit.pidev.entity.DocumentStatusEnum;
import tn.esprit.pidev.entity.DocumentType;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.exception.DocumentNotFoundException;
import tn.esprit.pidev.exception.DocumentTypeNotFoundException;
import tn.esprit.pidev.exception.InvalidDocumentException;
import tn.esprit.pidev.repository.LegalDocumentRepository;
import tn.esprit.pidev.repository.DocumentTypeRepository;
import tn.esprit.pidev.repository.UserRepository;
import tn.esprit.pidev.service.EmailService;
import tn.esprit.pidev.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing legal documents (USER and ADMIN operations)
 */
@Service
public class LegalDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(LegalDocumentService.class);

    @Autowired
    private LegalDocumentRepository legalDocumentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DocumentTypeService documentTypeService;

    /**
     * Upload new legal document (USER operation)
     */
    @Transactional
    public LegalDocument uploadDocument(Long userId, Long documentTypeId, MultipartFile file,
                                       LocalDateTime expiryDate, String customFields) {
        logger.info("User {} uploading document type {}", userId, documentTypeId);

        // Verify document type exists
        DocumentType docType = documentTypeRepository.findById(documentTypeId)
            .orElseThrow(() -> new DocumentTypeNotFoundException(documentTypeId));

        // Validate expiry date if required
        if (docType.getRequiresExpiry() && expiryDate == null) {
            throw new InvalidDocumentException("Expiry date is required for document type: " + docType.getName());
        }

        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            throw new InvalidDocumentException("Expiry date cannot be in the past");
        }

        // Upload file
        String fileHash = fileUploadService.calculateFileHash(file);
        String documentUrl = fileUploadService.uploadFile(file, userId);

        // Create and save document with DocumentType relationship
        LegalDocument document = new LegalDocument(userId, docType, documentUrl, fileHash, expiryDate);
        document.setStatus(DocumentStatusEnum.PENDING);

        LegalDocument saved = legalDocumentRepository.save(document);
        logger.info("✓ Document uploaded: ID {}, User {}, Type {}", saved.getId(), userId, documentTypeId);

        return saved;
    }

    /**
     * Get document by ID (USER can only see their own)
     */
    @Transactional(readOnly = true)
    public LegalDocument getDocumentById(Long documentId, Long userId) {
        LegalDocument document = legalDocumentRepository.findByIdAndUserId(documentId, userId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));
        return document;
    }

    /**
     * Get document by ID (ADMIN operation - no user check)
     */
    @Transactional(readOnly = true)
    public LegalDocument getDocumentByIdAdmin(Long documentId) {
        return legalDocumentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    /**
     * Get all documents for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<LegalDocument> getUserDocuments(Long userId, int page, int size) {
        logger.info("Fetching documents for user: {}, page: {}", userId, page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadDate"));
        return legalDocumentRepository.findByUserId(userId, pageable);
    }

    /**
     * Search documents with advanced criteria (ADMIN operation)
     */
    @Transactional(readOnly = true)
    public Page<LegalDocument> searchDocuments(DocumentSearchCriteria criteria) {
        logger.info("Searching documents with criteria: userId={}, documentTypeId={}, status={}",
                   criteria.getUserId(), criteria.getDocumentTypeId(), criteria.getStatus());

        Sort sort = Sort.by(criteria.getSortDir().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                           criteria.getSortBy());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return legalDocumentRepository.searchDocuments(
            criteria.getUserId(),
            criteria.getDocumentTypeId(),
            criteria.getStatus(),
            pageable
        );
    }

    /**
     * Get all pending documents for admin review
     */
    @Transactional(readOnly = true)
    public Page<LegalDocument> getPendingDocuments(int page, int size) {
        logger.info("Fetching pending documents for admin review");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "uploadDate"));
        return legalDocumentRepository.findAllPending(pageable);
    }

    /**
     * Approve document (ADMIN operation)
     */
    @Transactional
    public LegalDocument approveDocument(Long documentId, Long adminUserId) {
        logger.info("Admin {} approving document {}", adminUserId, documentId);

        LegalDocument document = legalDocumentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (document.getStatus() != DocumentStatusEnum.PENDING) {
            throw new InvalidDocumentException("Cannot approve document with status: " + document.getStatus());
        }

        document.setStatus(DocumentStatusEnum.VALID);

        LegalDocument saved = legalDocumentRepository.save(document);
        logger.info("✓ Document approved: ID {}", documentId);

        return saved;
    }

    /**
     * Reject document (ADMIN operation)
     */
    @Transactional
    public LegalDocument rejectDocument(Long documentId, String rejectionReason, Long adminUserId) {
        logger.info("Admin {} rejecting document {}", adminUserId, documentId);

        LegalDocument document = legalDocumentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (document.getStatus() != DocumentStatusEnum.PENDING) {
            throw new InvalidDocumentException("Cannot reject document with status: " + document.getStatus());
        }

        document.setStatus(DocumentStatusEnum.REJECTED);

        LegalDocument saved = legalDocumentRepository.save(document);
        logger.info("✓ Document rejected: ID {}", documentId);

        return saved;
    }

    /**
     * Request update from user (ADMIN operation)
     */
    @Transactional
    public LegalDocument requestDocumentUpdate(Long documentId, String feedback, Long adminUserId) {
        logger.info("Admin {} requesting update for document {}", adminUserId, documentId);

        LegalDocument document = legalDocumentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (document.getStatus() != DocumentStatusEnum.PENDING) {
            throw new InvalidDocumentException("Cannot request update for document with status: " + document.getStatus());
        }

        document.setStatus(DocumentStatusEnum.REQUEST_UPDATE);

        LegalDocument saved = legalDocumentRepository.save(document);
        logger.info("✓ Update requested for document: ID {}", documentId);

        return saved;
    }

    /**
     * Toggle document status between REJECTED and VALID (ADMIN operation)
     * Allows admin to change a rejected document to approved and vice versa
     * @param documentId ID of the document to toggle
     * @param adminUserId ID of the admin performing the action
     * @return Updated LegalDocument with new status
     * @throws DocumentNotFoundException if document not found
     * @throws InvalidDocumentException if document status is not REJECTED or VALID
     */
    @Transactional
    public LegalDocument toggleDocumentStatus(Long documentId, Long adminUserId) {
        logger.info("Admin {} toggling status for document {}", adminUserId, documentId);

        LegalDocument document = legalDocumentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        DocumentStatusEnum currentStatus = document.getStatus();

        // Only allow toggling between VALID and REJECTED
        if (currentStatus == DocumentStatusEnum.VALID) {
            document.setStatus(DocumentStatusEnum.REJECTED);
            logger.info("✓ Document {} status changed from VALID to REJECTED by admin {}", documentId, adminUserId);
        } else if (currentStatus == DocumentStatusEnum.REJECTED) {
            document.setStatus(DocumentStatusEnum.VALID);
            logger.info("✓ Document {} status changed from REJECTED to VALID by admin {}", documentId, adminUserId);
        } else {
            throw new InvalidDocumentException(
                "Cannot toggle status for document with status: " + currentStatus + 
                ". Only VALID and REJECTED documents can be toggled."
            );
        }

        LegalDocument saved = legalDocumentRepository.save(document);
        logger.info("✓ Document status toggled: ID {} - New Status: {}", documentId, saved.getStatus());

        return saved;
    }

    /**
     * Delete document (hard delete)
     */
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        logger.info("User {} deleting document {}", userId, documentId);

        LegalDocument document = legalDocumentRepository.findByIdAndUserId(documentId, userId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (document.getStatus() != DocumentStatusEnum.PENDING && document.getStatus() != DocumentStatusEnum.REJECTED) {
            throw new InvalidDocumentException("Cannot delete document with status: " + document.getStatus() + ". Only PENDING and REJECTED documents can be deleted.");
        }

        // Delete file from storage
        fileUploadService.deleteFile(document.getDocumentUrl());

        // Hard delete
        legalDocumentRepository.deleteById(documentId);
        logger.info("✓ Document deleted: ID {}", documentId);
    }

    /**
     * Delete document (ADMIN only)
     */
    @Transactional
    public void deleteDocumentAdmin(Long documentId) {
        logger.info("Admin deleting document: {}", documentId);

        LegalDocument document = legalDocumentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        // Delete file from storage
        fileUploadService.deleteFile(document.getDocumentUrl());

        // Hard delete
        legalDocumentRepository.delete(document);
        logger.info("✓ Document deleted: ID {}", documentId);
    }

    /**
     * Scheduled job: Check for expired documents and update status
     */
    @Transactional
    public void checkAndUpdateExpiredDocuments() {
        logger.info("Running scheduled job: checking for expired documents...");

        LocalDateTime now = LocalDateTime.now();
        List<LegalDocument> expiredDocs = legalDocumentRepository.findExpiredDocuments(now);

        if (expiredDocs.isEmpty()) {
            logger.info("No expired documents found");
            return;
        }

        logger.info("Found {} expired documents, updating status...", expiredDocs.size());
        for (LegalDocument doc : expiredDocs) {
            doc.setStatus(DocumentStatusEnum.EXPIRED);
            legalDocumentRepository.save(doc);
        }

        logger.info("✓ Updated {} documents to EXPIRED status", expiredDocs.size());
    }

    /**
     * Scheduled job: Send expiry notifications (30, 15, 7 days)
     */
    @Transactional
    public void sendExpiryNotifications() {
        logger.info("Running scheduled job: sending expiry notifications...");

        LocalDateTime now = LocalDateTime.now();
        int[] daysThresholds = {30, 15, 7};

        for (int days : daysThresholds) {
            LocalDateTime from = now.plusDays(days).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime to = from.withHour(23).withMinute(59).withSecond(59);

            List<LegalDocument> expiringDocs = legalDocumentRepository.findDocumentsExpiringWithin(from, to);

            logger.info("Found {} documents expiring in {} days", expiringDocs.size(), days);

            for (LegalDocument doc : expiringDocs) {
                try {
                    sendExpiryWarningEmail(doc, days);
                } catch (Exception e) {
                    logger.error("Failed to send expiry notification for document {}: {}", doc.getId(), e.getMessage());
                }
            }
        }

        logger.info("✓ Expiry notifications completed");
    }

    /**
     * Helper: Send expiry warning email
     */
    private void sendExpiryWarningEmail(LegalDocument doc, int daysUntilExpiry) {
        try {
            // This would require extending EmailService to include user email lookup
            logger.info("Sending expiry notification for document {} - {} days until expiry", doc.getId(), daysUntilExpiry);

            // In a real implementation:
            // User user = userRepository.findById(doc.getUserId()).orElse(null);
            // if (user != null) {
            //     emailService.sendExpiryWarningEmail(user.getEmail(), doc, daysUntilExpiry);
            // }
        } catch (Exception e) {
            logger.error("Error sending expiry email for document {}: {}", doc.getId(), e.getMessage());
        }
    }

    /**
     * Convert entity to DTO (without file content)
     */
    public LegalDocumentResponse toDto(LegalDocument document) {
        // Get username from User entity
        String username = null;
        if (document.getUserId() != null) {
            username = userRepository.findById(document.getUserId())
                .map(User::getUsername)
                .orElse(null);
        }

        LegalDocumentResponse response = new LegalDocumentResponse(
            document.getId(),
            document.getUserId(),
            username,
            document.getDocumentTypeId(),
            document.getDocumentUrl(),
            document.getUploadDate(),
            document.getExpiryDate(),
            document.getStatus()
        );
        
        // Include the nested DocumentType information
        if (document.getDocumentType() != null) {
            response.setDocumentType(documentTypeService.toDto(document.getDocumentType()));
        }
        
        return response;
    }
}

