package com.leonardoborges.api.controller;

import com.leonardoborges.api.model.AuditLog;
import com.leonardoborges.api.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for audit log management.
 * Only accessible by ADMIN users.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit", description = "Audit log management endpoints (Admin only)")
public class AuditController {
    
    private final AuditLogRepository auditLogRepository;
    
    @GetMapping
    @Operation(
            summary = "List audit logs",
            description = "Returns a paginated list of audit logs. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Log list returned successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = com.leonardoborges.api.exception.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer role ADMIN",
                    content = @Content(schema = @Schema(implementation = com.leonardoborges.api.exception.ErrorResponse.class))
            )
    })
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit - Fetching audit logs");
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/action/{action}")
    @Operation(
            summary = "Get logs by action",
            description = "Returns audit logs filtered by specific action. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
            @Parameter(description = "Action to filter (e.g., TASK_CREATED, LOGIN_SUCCESS)") 
            @PathVariable String action,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit/action/{} - Fetching audit logs by action", action);
        Page<AuditLog> logs = auditLogRepository.findByAction(action, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(
            summary = "Get logs by entity",
            description = "Returns audit logs for a specific entity. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByEntity(
            @Parameter(description = "Entity type (e.g., Task, User)") 
            @PathVariable String entityType,
            @Parameter(description = "Entity ID") 
            @PathVariable Long entityId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit/entity/{}/{} - Fetching audit logs by entity", entityType, entityId);
        Page<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/user/{username}")
    @Operation(
            summary = "Get logs by user",
            description = "Returns audit logs for a specific user. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUser(
            @Parameter(description = "Username") 
            @PathVariable String username,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit/user/{} - Fetching audit logs by user", username);
        Page<AuditLog> logs = auditLogRepository.findByUsername(username, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/date-range")
    @Operation(
            summary = "Get logs by date range",
            description = "Returns audit logs within a date range. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @Parameter(description = "Start date (format: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (format: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit/date-range - Fetching audit logs by date range");
        Page<AuditLog> logs = auditLogRepository.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/stats/failed")
    @Operation(
            summary = "Failed actions statistics",
            description = "Returns count of failed actions since a date. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Long> getFailedActionsCount(
            @Parameter(description = "Start date (format: yyyy-MM-ddTHH:mm:ss, default: 24h ago)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        if (since == null) {
            since = LocalDateTime.now().minusHours(24);
        }
        log.debug("GET /api/v1/audit/stats/failed - Fetching failed actions count since {}", since);
        long count = auditLogRepository.countFailedActionsSince(since);
        return ResponseEntity.ok(count);
    }
}
