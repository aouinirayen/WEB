package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.AuditLogDTO;
import tn.esprit.pidev.enums.AuditLogActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Service interface for Audit Log operations
 */
public interface AuditLogService {

    /**
     * Log an audit action
     */
    AuditLogDTO logAction(AuditLogDTO auditLogDTO);

    /**
     * Get all audit logs for a specific user
     */
    Page<AuditLogDTO> getUserActivityLog(Long userId, Pageable pageable);

    /**
     * Get filtered audit logs for a specific user by action type
     */
    Page<AuditLogDTO> getUserActivityLogByActionType(Long userId, AuditLogActionType actionType, Pageable pageable);

    /**
     * Get all system activity logs (admin only)
     */
    Page<AuditLogDTO> getAllActivityLogs(Pageable pageable);

    /**
     * Search audit logs with advanced filtering
     */
    Page<AuditLogDTO> searchActivityLogs(Long userId, AuditLogActionType actionType,
                                         LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    /**
     * Get audit logs by action type
     */
    Page<AuditLogDTO> getActivityLogsByActionType(AuditLogActionType actionType, Pageable pageable);

    /**
     * Get audit logs by resource
     */
    Page<AuditLogDTO> getActivityLogsByResource(String resourceType, Long resourceId, Pageable pageable);

    /**
     * Delete audit logs older than the specified number of days
     * Used for cleanup of old activity logs
     */
    int deleteOldAuditLogs(int retentionDays);
}
