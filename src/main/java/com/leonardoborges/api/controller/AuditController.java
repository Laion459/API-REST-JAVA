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
            summary = "Listar logs de auditoria",
            description = "Retorna lista paginada de logs de auditoria. Requer role ADMIN.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de logs retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
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
            summary = "Buscar logs por ação",
            description = "Retorna logs de auditoria filtrados por ação específica. Requer role ADMIN.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
            @Parameter(description = "Ação a filtrar (ex: TASK_CREATED, LOGIN_SUCCESS)") 
            @PathVariable String action,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit/action/{} - Fetching audit logs by action", action);
        Page<AuditLog> logs = auditLogRepository.findByAction(action, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(
            summary = "Buscar logs por entidade",
            description = "Retorna logs de auditoria para uma entidade específica. Requer role ADMIN.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByEntity(
            @Parameter(description = "Tipo da entidade (ex: Task, User)") 
            @PathVariable String entityType,
            @Parameter(description = "ID da entidade") 
            @PathVariable Long entityId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit/entity/{}/{} - Fetching audit logs by entity", entityType, entityId);
        Page<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/user/{username}")
    @Operation(
            summary = "Buscar logs por usuário",
            description = "Retorna logs de auditoria para um usuário específico. Requer role ADMIN.",
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
            summary = "Buscar logs por intervalo de datas",
            description = "Retorna logs de auditoria em um intervalo de datas. Requer role ADMIN.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @Parameter(description = "Data inicial (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Data final (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/audit/date-range - Fetching audit logs by date range");
        Page<AuditLog> logs = auditLogRepository.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/stats/failed")
    @Operation(
            summary = "Estatísticas de ações falhadas",
            description = "Retorna contagem de ações falhadas desde uma data. Requer role ADMIN.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Long> getFailedActionsCount(
            @Parameter(description = "Data inicial (formato: yyyy-MM-ddTHH:mm:ss, padrão: 24h atrás)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        if (since == null) {
            since = LocalDateTime.now().minusHours(24);
        }
        log.debug("GET /api/v1/audit/stats/failed - Fetching failed actions count since {}", since);
        long count = auditLogRepository.countFailedActionsSince(since);
        return ResponseEntity.ok(count);
    }
}
