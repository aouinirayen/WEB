package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.LegalDocument;
import tn.esprit.pidev.entity.DocumentStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LegalDocument entity
 */
@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

    /**
     * Find documents by user ID
     */
    Page<LegalDocument> findByUserId(Long userId, Pageable pageable);

    /**
     * Find a specific document by ID and user ID (security check)
     */
    Optional<LegalDocument> findByIdAndUserId(Long id, Long userId);

    /**
     * Find all documents of a user by status
     */
    Page<LegalDocument> findByUserIdAndStatus(Long userId, DocumentStatusEnum status, Pageable pageable);

    /**
     * Find all documents by document type
     */
    Page<LegalDocument> findByDocumentTypeId(Long documentTypeId, Pageable pageable);

    /**
     * Search documents with advanced criteria for admin
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE " +
           "(:userId IS NULL OR ld.userId = :userId) AND " +
           "(:documentTypeId IS NULL OR ld.documentType.id = :documentTypeId) AND " +
           "(:status IS NULL OR ld.status = :status)")
    Page<LegalDocument> searchDocuments(
            @Param("userId") Long userId,
            @Param("documentTypeId") Long documentTypeId,
            @Param("status") DocumentStatusEnum status,
            Pageable pageable);

    /**
     * Find all pending documents (for admin review)
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.status = 'PENDING' ORDER BY ld.uploadDate ASC")
    Page<LegalDocument> findAllPending(Pageable pageable);

    /**
     * Find documents with expired status based on expiry date
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND ld.expiryDate < :now AND ld.status != 'EXPIRED'")
    List<LegalDocument> findExpiredDocuments(@Param("now") LocalDateTime now);

    /**
     * Find documents expiring within X days (for notifications)
     */
    @Query("SELECT ld FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :from AND :to AND ld.status IN ('PENDING', 'VALID')")
    List<LegalDocument> findDocumentsExpiringWithin(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Find documents by file hash (duplicate check)
     */
    Optional<LegalDocument> findByFileHashAndUserId(String fileHash, Long userId);

    /**
     * Count pending documents for a user
     */
    long countByUserIdAndStatus(Long userId, DocumentStatusEnum status);

    /**
     * Find documents expiring within X days (paginated) with eager loading
     */
    @Query("SELECT DISTINCT ld FROM LegalDocument ld LEFT JOIN FETCH ld.documentType WHERE ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :from AND :to")
    Page<LegalDocument> findExpiringDocuments(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    /**
     * Find all expired documents with eager loading
     */
    @Query("SELECT DISTINCT ld FROM LegalDocument ld LEFT JOIN FETCH ld.documentType WHERE ld.expiryDate IS NOT NULL AND ld.expiryDate < :now")
    Page<LegalDocument> findAllExpiredDocuments(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find documents expiring within X days for a specific user (paginated) with eager loading
     */
    @Query("SELECT DISTINCT ld FROM LegalDocument ld LEFT JOIN FETCH ld.documentType WHERE ld.userId = :userId AND ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :from AND :to")
    Page<LegalDocument> findUserExpiringDocuments(@Param("userId") Long userId, @Param("from") LocalDateTime from, 
                                                  @Param("to") LocalDateTime to, Pageable pageable);

    /**
     * Find documents expiring within X days by document type (paginated) with eager loading
     */
    @Query("SELECT DISTINCT ld FROM LegalDocument ld LEFT JOIN FETCH ld.documentType WHERE ld.documentType.id = :documentTypeId AND ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :from AND :to")
    Page<LegalDocument> findExpiringDocumentsByType(@Param("documentTypeId") Long documentTypeId, 
                                                    @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    /**
     * Count documents expiring within 7 days
     */
    @Query("SELECT COUNT(ld) FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :today AND :sevenDaysLater")
    long countDocumentsExpiringWithinSevenDays(@Param("today") LocalDateTime today, @Param("sevenDaysLater") LocalDateTime sevenDaysLater);

    /**
     * Count documents expiring within 30 days
     */
    @Query("SELECT COUNT(ld) FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :today AND :thirtyDaysLater")
    long countDocumentsExpiringWithinThirtyDays(@Param("today") LocalDateTime today, @Param("thirtyDaysLater") LocalDateTime thirtyDaysLater);

    /**
     * Count documents expiring within 90 days
     */
    @Query("SELECT COUNT(ld) FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND " +
           "ld.expiryDate BETWEEN :today AND :ninetyDaysLater")
    long countDocumentsExpiringWithinNinetyDays(@Param("today") LocalDateTime today, @Param("ninetyDaysLater") LocalDateTime ninetyDaysLater);

    /**
     * Count already expired documents
     */
    @Query("SELECT COUNT(ld) FROM LegalDocument ld WHERE ld.expiryDate IS NOT NULL AND ld.expiryDate < :today")
    long countAlreadyExpiredDocuments(@Param("today") LocalDateTime today);
}
