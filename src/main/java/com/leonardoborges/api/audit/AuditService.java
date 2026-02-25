package com.leonardoborges.api.audit;

import com.leonardoborges.api.model.AuditLog;
import com.leonardoborges.api.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for auditing sensitive operations.
 * Persists audit logs to database for compliance and security purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * Logs a sensitive operation for audit purposes.
     * Persists to database asynchronously.
     * 
     * @param action The action performed (e.g., "TASK_DELETED", "USER_CREATED")
     * @param resourceType The type of resource affected (e.g., "Task", "User")
     * @param resourceId The ID of the resource affected
     * @param details Additional details about the action
     */
    @Async
    @Transactional
    public void audit(String action, String resourceType, Long resourceId, String details) {
        try {
            String username = getCurrentUsername();
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(resourceType)
                    .entityId(resourceId)
                    .username(username)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .description(details)
                    .success(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log persisted: {} | {} | {}", action, resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage(), e);
            // Fallback to logging only
            log.info("[AUDIT] Action: {} | Resource: {} | ID: {} | User: {} | Details: {} | Timestamp: {}",
                    action, resourceType, resourceId, getCurrentUsername(), details, LocalDateTime.now());
        }
    }
    
    /**
     * Logs a sensitive operation with additional context.
     * Persists to database asynchronously.
     * 
     * @param action The action performed
     * @param resourceType The type of resource affected
     * @param resourceId The ID of the resource affected
     * @param details Additional details
     * @param oldValue Previous value (for updates)
     * @param newValue New value (for updates)
     */
    @Async
    @Transactional
    public void auditWithChanges(String action, String resourceType, Long resourceId, 
                                 String details, String oldValue, String newValue) {
        try {
            String username = getCurrentUsername();
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(resourceType)
                    .entityId(resourceId)
                    .username(username)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .description(details)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .success(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log with changes persisted: {} | {} | {}", action, resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to persist audit log with changes: {}", e.getMessage(), e);
            // Fallback to logging only
            log.info("[AUDIT] Action: {} | Resource: {} | ID: {} | User: {} | Details: {} | Old: {} | New: {} | Timestamp: {}",
                    action, resourceType, resourceId, getCurrentUsername(), details, oldValue, newValue, LocalDateTime.now());
        }
    }
    
    /**
     * Logs authentication-related events.
     * Persists to database asynchronously.
     * 
     * @param event The authentication event (e.g., "LOGIN_SUCCESS", "LOGIN_FAILED", "LOGOUT")
     * @param username The username involved
     * @param details Additional details
     */
    @Async
    @Transactional
    public void auditAuthentication(String event, String username, String details) {
        try {
            HttpServletRequest request = getCurrentRequest();
            boolean success = event.contains("SUCCESS") || event.contains("REFRESHED");
            
            AuditLog auditLog = AuditLog.builder()
                    .action(event)
                    .entityType("Authentication")
                    .username(username)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .description(details)
                    .success(success)
                    .error(success ? null : details)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Authentication audit log persisted: {} | {}", event, username);
        } catch (Exception e) {
            log.error("Failed to persist authentication audit log: {}", e.getMessage(), e);
            // Fallback to logging only
            log.info("[AUDIT] Auth Event: {} | User: {} | Details: {} | Timestamp: {}",
                    event, username, details, LocalDateTime.now());
        }
    }
    
    /**
     * Logs security-related events.
     * Persists to database asynchronously.
     * 
     * @param event The security event (e.g., "RATE_LIMIT_EXCEEDED", "UNAUTHORIZED_ACCESS")
     * @param details Additional details
     */
    @Async
    @Transactional
    public void auditSecurity(String event, String details) {
        try {
            String username = getCurrentUsername();
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .action(event)
                    .entityType("Security")
                    .username(username)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .description(details)
                    .success(false)
                    .error(details)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            auditLogRepository.save(auditLog);
            log.warn("Security audit log persisted: {} | User: {} | IP: {}", event, username, getClientIpAddress(request));
        } catch (Exception e) {
            log.error("Failed to persist security audit log: {}", e.getMessage(), e);
            // Fallback to logging only
            String fallbackUsername = getCurrentUsername();
            log.warn("[AUDIT] Security Event: {} | User: {} | IP: {} | Details: {} | Timestamp: {}",
                    event, fallbackUsername, getClientIpAddress(getCurrentRequest()), details, LocalDateTime.now());
        }
    }
    
    private String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("ANONYMOUS");
    }
    
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent;
    }
}
