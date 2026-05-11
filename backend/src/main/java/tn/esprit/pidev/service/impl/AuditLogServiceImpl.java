package tn.esprit.pidev.service.impl;

import tn.esprit.pidev.dto.AuditLogDTO;
import tn.esprit.pidev.entity.AuditLog;
import tn.esprit.pidev.enums.AuditLogActionType;
import tn.esprit.pidev.repository.AuditLogRepository;
import tn.esprit.pidev.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of AuditLogService
 * Handles all audit log operations
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements tn.esprit.pidev.service.AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLogDTO logAction(AuditLogDTO auditLogDTO) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(auditLogDTO.getUserId());
        auditLog.setUsername(auditLogDTO.getUsername());
        auditLog.setActionType(auditLogDTO.getActionType());
        auditLog.setResourceType(auditLogDTO.getResourceType());
        auditLog.setResourceId(auditLogDTO.getResourceId());
        auditLog.setResourceName(auditLogDTO.getResourceName());
        auditLog.setDescription(auditLogDTO.getDescription());
        auditLog.setIpAddress(auditLogDTO.getIpAddress());
        auditLog.setUserAgent(auditLogDTO.getUserAgent());
        auditLog.setStatus(auditLogDTO.getStatus());
        auditLog.setErrorDetails(auditLogDTO.getErrorDetails());

        AuditLog savedAuditLog = auditLogRepository.save(auditLog);
        return mapToDTO(savedAuditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getUserActivityLog(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getUserActivityLogByActionType(Long userId, AuditLogActionType actionType, Pageable pageable) {
        return auditLogRepository.findByUserIdAndActionType(userId, actionType, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAllActivityLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> searchActivityLogs(Long userId, AuditLogActionType actionType,
                                               LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(userId, actionType, dateFrom, dateTo, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getActivityLogsByActionType(AuditLogActionType actionType, Pageable pageable) {
        return auditLogRepository.findByActionType(actionType, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getActivityLogsByResource(String resourceType, Long resourceId, Pageable pageable) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public int deleteOldAuditLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        return auditLogRepository.deleteAuditLogsOlderThan(cutoffDate);
    }

    /**
     * Helper method to convert AuditLog entity to DTO
     */
    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        return new AuditLogDTO(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getUsername(),
                auditLog.getActionType(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getResourceName(),
                auditLog.getDescription(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getTimestamp(),
                auditLog.getStatus(),
                auditLog.getErrorDetails()
        );
    }
}

