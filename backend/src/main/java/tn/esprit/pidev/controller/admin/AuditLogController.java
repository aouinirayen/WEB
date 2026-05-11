package tn.esprit.pidev.controller.admin;

import tn.esprit.pidev.dto.AuditLogDTO;
import tn.esprit.pidev.enums.AuditLogActionType;
import tn.esprit.pidev.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for audit log endpoints
 * Admin endpoints for tracking user activity
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * GET /api/admin/users/{userId}/activity-log
     * Returns paginated activity log for a specific user
     */
    @GetMapping("/{userId}/activity-log")
    public ResponseEntity<Page<AuditLogDTO>> getUserActivityLog(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getUserActivityLog(userId, pageable));
    }

    /**
     * GET /api/admin/users/{userId}/activity-log/filter
     * Returns filtered activity log for a user by action type
     */
    @GetMapping("/{userId}/activity-log/filter")
    public ResponseEntity<Page<AuditLogDTO>> getUserActivityLogByActionType(
            @PathVariable Long userId,
            @RequestParam AuditLogActionType actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getUserActivityLogByActionType(userId, actionType, pageable));
    }

    /**
     * GET /api/admin/users/activity-logs/all
     * Returns all system activity logs (admin only)
     */
    @GetMapping("/activity-logs/all")
    public ResponseEntity<Page<AuditLogDTO>> getAllActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAllActivityLogs(pageable));
    }

    /**
     * GET /api/admin/users/activity-logs/search
     * Advanced search for activity logs with multiple filter options
     */
    @GetMapping("/activity-logs/search")
    public ResponseEntity<Page<AuditLogDTO>> searchActivityLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) AuditLogActionType actionType,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDateTime from = dateFrom != null ? LocalDateTime.parse(dateFrom, DATE_FORMATTER) : null;
        LocalDateTime to = dateTo != null ? LocalDateTime.parse(dateTo, DATE_FORMATTER) : null;

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.searchActivityLogs(userId, actionType, from, to, pageable));
    }

    /**
     * GET /api/admin/users/activity-logs/by-action
     * Get activity logs filtered by action type only
     */
    @GetMapping("/activity-logs/by-action")
    public ResponseEntity<Page<AuditLogDTO>> getActivityLogsByActionType(
            @RequestParam AuditLogActionType actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getActivityLogsByActionType(actionType, pageable));
    }

    /**
     * GET /api/admin/users/activity-logs/by-resource
     * Get activity logs filtered by resource type and ID
     */
    @GetMapping("/activity-logs/by-resource")
    public ResponseEntity<Page<AuditLogDTO>> getActivityLogsByResource(
            @RequestParam String resourceType,
            @RequestParam Long resourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getActivityLogsByResource(resourceType, resourceId, pageable));
    }
}

