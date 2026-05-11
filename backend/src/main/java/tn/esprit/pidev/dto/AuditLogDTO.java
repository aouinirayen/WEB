package tn.esprit.pidev.dto;

import tn.esprit.pidev.enums.AuditLogActionType;
import tn.esprit.pidev.enums.AuditLogStatus;
import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private Long userId;
    private String username;
    private AuditLogActionType actionType;
    private String resourceType;
    private Long resourceId;
    private String resourceName;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private AuditLogStatus status;
    private String errorDetails;

    // Constructors
    public AuditLogDTO() {}

    public AuditLogDTO(Long id, Long userId, String username, AuditLogActionType actionType,
                      String resourceType, Long resourceId, String resourceName, String description,
                      String ipAddress, String userAgent, LocalDateTime timestamp,
                      AuditLogStatus status, String errorDetails) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.description = description;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
        this.status = status;
        this.errorDetails = errorDetails;
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
}

