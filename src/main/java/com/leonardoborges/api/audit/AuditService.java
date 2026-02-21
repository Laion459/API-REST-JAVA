package com.leonardoborges.api.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for auditing sensitive operations.
 * Logs critical actions for security and compliance purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    /**
     * Logs a sensitive operation for audit purposes.
     * 
     * @param action The action performed (e.g., "TASK_DELETED", "USER_CREATED")
     * @param resourceType The type of resource affected (e.g., "Task", "User")
     * @param resourceId The ID of the resource affected
     * @param details Additional details about the action
     */
    public void audit(String action, String resourceType, Long resourceId, String details) {
        String username = getCurrentUsername();
        String message = String.format(
                "[AUDIT] Action: %s | Resource: %s | ID: %d | User: %s | Details: %s | Timestamp: %s",
                action, resourceType, resourceId, username, details, LocalDateTime.now()
        );
        log.info(message);
    }
    
    /**
     * Logs a sensitive operation with additional context.
     * 
     * @param action The action performed
     * @param resourceType The type of resource affected
     * @param resourceId The ID of the resource affected
     * @param details Additional details
     * @param oldValue Previous value (for updates)
     * @param newValue New value (for updates)
     */
    public void auditWithChanges(String action, String resourceType, Long resourceId, 
                                 String details, String oldValue, String newValue) {
        String username = getCurrentUsername();
        String message = String.format(
                "[AUDIT] Action: %s | Resource: %s | ID: %d | User: %s | Details: %s | " +
                "Old Value: %s | New Value: %s | Timestamp: %s",
                action, resourceType, resourceId, username, details, 
                oldValue, newValue, LocalDateTime.now()
        );
        log.info(message);
    }
    
    /**
     * Logs authentication-related events.
     * 
     * @param event The authentication event (e.g., "LOGIN_SUCCESS", "LOGIN_FAILED", "LOGOUT")
     * @param username The username involved
     * @param details Additional details
     */
    public void auditAuthentication(String event, String username, String details) {
        String message = String.format(
                "[AUDIT] Auth Event: %s | User: %s | Details: %s | Timestamp: %s",
                event, username, details, LocalDateTime.now()
        );
        log.info(message);
    }
    
    /**
     * Logs security-related events.
     * 
     * @param event The security event (e.g., "RATE_LIMIT_EXCEEDED", "UNAUTHORIZED_ACCESS")
     * @param details Additional details
     */
    public void auditSecurity(String event, String details) {
        String username = getCurrentUsername();
        String ipAddress = getClientIpAddress();
        String message = String.format(
                "[AUDIT] Security Event: %s | User: %s | IP: %s | Details: %s | Timestamp: %s",
                event, username, ipAddress, details, LocalDateTime.now()
        );
        log.warn(message); // Security events should be logged at WARN level
    }
    
    private String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("ANONYMOUS");
    }
    
    private String getClientIpAddress() {
        // This would need to be passed from the controller or filter
        // For now, return a placeholder
        return "UNKNOWN";
    }
}
