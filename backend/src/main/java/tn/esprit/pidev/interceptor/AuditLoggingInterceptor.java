package tn.esprit.pidev.interceptor;

import tn.esprit.pidev.dto.AuditLogDTO;
import tn.esprit.pidev.enums.AuditLogActionType;
import tn.esprit.pidev.enums.AuditLogStatus;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.repository.UserRepository;
import tn.esprit.pidev.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * HTTP Interceptor for automatic audit logging of user activities
 */
@Component
@RequiredArgsConstructor
public class AuditLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingInterceptor.class);

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Store request start time for logging purposes
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            String method = request.getMethod();
            String path = request.getRequestURI();
            int status = response.getStatus();

            // Only log certain endpoints
            if (shouldLogEndpoint(path)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.isAuthenticated()) {
                    String username = authentication.getName();
                    Long userId = extractUserIdFromAuth(authentication);
                    
                    logger.debug("Audit Log - Endpoint: {}, Username: {}, UserId: {}", path, username, userId);
                    
                    // Only log if we successfully extracted the user ID
                    if (userId != null) {
                        // Check if user is an admin - skip logging for admins
                        RoleEnum userRole = extractUserRoleFromAuth(authentication);
                        logger.debug("Audit Log - User Role: {}", userRole);
                        
                        if (userRole == RoleEnum.ADMIN) {
                            logger.debug("Audit Log - Skipping ADMIN user: {}", username);
                            return;
                        }

                        AuditLogActionType actionType = determineActionType(path, method);
                        AuditLogStatus auditStatus = (status >= 200 && status < 300) ? AuditLogStatus.SUCCESS : AuditLogStatus.FAILED;
                        String ipAddress = getClientIpAddress(request);

                        // Create audit log entry
                        AuditLogDTO auditLog = new AuditLogDTO();
                        auditLog.setUserId(userId);
                        auditLog.setUsername(username);
                        auditLog.setActionType(actionType);
                        auditLog.setResourceType(extractResourceType(path));
                        auditLog.setResourceId(extractResourceId(path));
                        auditLog.setDescription(createDescription(method, path));
                        auditLog.setIpAddress(ipAddress);
                        auditLog.setUserAgent(request.getHeader("User-Agent"));
                        auditLog.setStatus(auditStatus);
                        auditLog.setTimestamp(LocalDateTime.now());

                        if (auditStatus == AuditLogStatus.FAILED) {
                            auditLog.setErrorDetails("HTTP Status: " + status);
                        }

                        auditLogService.logAction(auditLog);
                        logger.debug("Audit Log - Created entry for user: {} ({})", username, actionType);
                    } else {
                        logger.debug("Audit Log - Could not extract userId for user: {}", username);
                    }
                } else {
                    logger.debug("Audit Log - Authentication is null or not authenticated");
                }
            } else {
                logger.debug("Audit Log - Endpoint not logged: {}", path);
            }
        } catch (Exception e) {
            logger.error("Error in audit logging: {}", e.getMessage(), e);
        }
    }

    private boolean shouldLogEndpoint(String path) {
        // Log important endpoints - be more inclusive
        return path.contains("/api/documents") ||
               path.contains("/api/auth") ||
               path.contains("/api/admin/users") ||
               path.contains("/api/users") ||
               path.contains("/api/legal") ||
               path.contains("/api/profile");
    }

    private AuditLogActionType determineActionType(String path, String method) {
        if (path.contains("/auth/login")) return AuditLogActionType.LOGIN;
        if (path.contains("/auth/logout")) return AuditLogActionType.LOGOUT;
        if (path.contains("/auth/change-password")) return AuditLogActionType.PASSWORD_CHANGED;
        if (path.contains("/profile") && method.equals("PUT")) return AuditLogActionType.PROFILE_UPDATED;
        if (path.contains("/documents") && method.equals("POST")) return AuditLogActionType.DOCUMENT_UPLOADED;
        if (path.contains("/documents") && method.equals("GET")) return AuditLogActionType.DOCUMENT_VIEWED;
        if (path.contains("/documents/download")) return AuditLogActionType.DOCUMENT_DOWNLOADED;
        if (path.contains("/approve")) return AuditLogActionType.DOCUMENT_APPROVED;
        if (path.contains("/reject")) return AuditLogActionType.DOCUMENT_REJECTED;
        if (path.contains("/admin/users") && method.equals("POST")) return AuditLogActionType.USER_CREATED;
        if (path.contains("/admin/users") && method.equals("PUT")) return AuditLogActionType.USER_UPDATED;
        if (path.contains("/admin/users") && method.equals("DELETE")) return AuditLogActionType.USER_DELETED;

        return AuditLogActionType.LOGIN; // Default
    }

    private String extractResourceType(String path) {
        if (path.contains("/documents")) return "DOCUMENT";
        if (path.contains("/admin/users")) return "USER";
        if (path.contains("/profile")) return "PROFILE";
        return "OTHER";
    }

    private Long extractResourceId(String path) {
        // Simple extraction - can be improved with regex if needed
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ((parts[i].equals("documents") || parts[i].equals("users")) && i + 1 < parts.length) {
                try {
                    return Long.parseLong(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String createDescription(String method, String path) {
        return method + " " + path;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        // Extract username from authentication
        String username = authentication.getName();
        
        // Look up user ID from the database using username
        try {
            return userRepository.findByUsername(username)
                    .map(user -> user.getId())
                    .orElse(null);
        } catch (Exception e) {
            // If lookup fails, return null
            return null;
        }
    }

    private RoleEnum extractUserRoleFromAuth(Authentication authentication) {
        // Extract username from authentication
        String username = authentication.getName();
        
        // Look up user role from the database using username
        try {
            return userRepository.findByUsername(username)
                    .map(user -> user.getRole())
                    .orElse(null);
        } catch (Exception e) {
            // If lookup fails, return null
            return null;
        }
    }
}

