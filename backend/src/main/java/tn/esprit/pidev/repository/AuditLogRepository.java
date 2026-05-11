package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.AuditLog;
import tn.esprit.pidev.enums.AuditLogActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for AuditLog entity
 * Handles database operations for audit log records
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find audit logs for a user filtered by action type
     */
    Page<AuditLog> findByUserIdAndActionType(Long userId, AuditLogActionType actionType, Pageable pageable);

    /**
     * Find all audit logs with advanced filtering
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:userId IS NULL OR al.userId = :userId) AND " +
           "(:actionType IS NULL OR al.actionType = :actionType) AND " +
           "(:dateFrom IS NULL OR al.timestamp >= :dateFrom) AND " +
           "(:dateTo IS NULL OR al.timestamp <= :dateTo)")
    Page<AuditLog> searchAuditLogs(
            @Param("userId") Long userId,
            @Param("actionType") AuditLogActionType actionType,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    /**
     * Find audit logs by action type (for system-wide filtering)
     */
    Page<AuditLog> findByActionType(AuditLogActionType actionType, Pageable pageable);

    /**
     * Find audit logs by resource type and resource ID
     */
    Page<AuditLog> findByResourceTypeAndResourceId(String resourceType, Long resourceId, Pageable pageable);

    /**
     * Delete all audit logs older than the specified date
     * Used for cleanup of old activity logs
     */
    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :cutoffDate")
    int deleteAuditLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
