package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.ExpiringDocumentsResponse;
import tn.esprit.pidev.dto.ExpiryStatsDTO;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Document Expiry operations
 */
public interface DocumentExpiryService {

    /**
     * Get documents expiring within X days with expiry stats
     */
    ExpiringDocumentsResponse getExpiringDocuments(int days, Pageable pageable);

    /**
     * Get all expired documents
     */
    ExpiringDocumentsResponse getExpiredDocuments(Pageable pageable);

    /**
     * Get user-specific expiring documents
     */
    ExpiringDocumentsResponse getUserExpiringDocuments(Long userId, int days, Pageable pageable);

    /**
     * Get document type-specific expiring documents
     */
    ExpiringDocumentsResponse getExpiringDocumentsByType(Long documentTypeId, int days, Pageable pageable);

    /**
     * Get expiry statistics
     */
    ExpiryStatsDTO getExpiryStatistics();
}

