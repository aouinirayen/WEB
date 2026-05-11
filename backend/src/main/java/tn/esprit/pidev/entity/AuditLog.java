package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import tn.esprit.pidev.enums.AuditLogActionType;
import tn.esprit.pidev.enums.AuditLogStatus;
import java.time.LocalDateTime;

/**
 * Entity for tracking user activity audit logs
 * Records all significant actions performed by users in the system
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_action_type", columnList = "action_type"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_user_timestamp", columnList = "user_id,timestamp"),
    @Index(name = "idx_action_timestamp", columnList = "action_type,timestamp")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditLogActionType actionType;

    @Column
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditLogStatus status = AuditLogStatus.SUCCESS;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.status = AuditLogStatus.SUCCESS;
    }

    public AuditLog(Long userId, String username, AuditLogActionType actionType) {
        this();
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuditLogActionType getActionType() {
        return actionType;
    }

    public void setActionType(AuditLogActionType actionType) {
        this.actionType = actionType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public AuditLogStatus getStatus() {
        return status;
    }

    public void setStatus(AuditLogStatus status) {
        this.status = status;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", actionType=" + actionType +
                ", resourceType='" + resourceType + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return java.util.Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}

