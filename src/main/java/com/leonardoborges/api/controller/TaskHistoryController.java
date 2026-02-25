package com.leonardoborges.api.controller;

import com.leonardoborges.api.model.TaskHistory;
import com.leonardoborges.api.repository.TaskHistoryRepository;
import com.leonardoborges.api.service.TaskHistoryService;
import com.leonardoborges.api.service.TaskHistoryService;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller para consulta de histórico de mudanças de tasks.
 * Permite rastrear todas as alterações feitas em uma task.
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task History", description = "Histórico de mudanças de tasks")
public class TaskHistoryController {
    
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskHistoryService taskHistoryService;
    
    @GetMapping
    @Operation(
            summary = "Listar histórico de mudanças de uma task",
            description = "Retorna lista paginada de todas as mudanças feitas em uma task específica. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Histórico retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = com.leonardoborges.api.exception.ErrorResponse.class))
            )
    })
    public ResponseEntity<Page<TaskHistory>> getTaskHistory(
            @Parameter(description = "ID da task") @PathVariable Long taskId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/tasks/{}/history - Fetching task history", taskId);
        Page<TaskHistory> history = taskHistoryRepository.findByTaskId(taskId, pageable);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/field/{fieldName}")
    @Operation(
            summary = "Listar histórico de um campo específico",
            description = "Retorna histórico de mudanças de um campo específico de uma task. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<List<TaskHistory>> getTaskHistoryByField(
            @Parameter(description = "ID da task") @PathVariable Long taskId,
            @Parameter(description = "Nome do campo (ex: title, status, priority)") @PathVariable String fieldName) {
        log.debug("GET /api/v1/tasks/{}/history/field/{} - Fetching field history", taskId, fieldName);
        List<TaskHistory> history = taskHistoryRepository.findByTaskIdAndFieldName(taskId, fieldName);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/all")
    @Operation(
            summary = "Listar todo o histórico de uma task",
            description = "Retorna todo o histórico de mudanças de uma task ordenado por data. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<List<TaskHistory>> getAllTaskHistory(
            @Parameter(description = "ID da task") @PathVariable Long taskId) {
        log.debug("GET /api/v1/tasks/{}/history/all - Fetching all task history", taskId);
        List<TaskHistory> history = taskHistoryService.getTaskHistory(taskId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/date-range")
    @Operation(
            summary = "Listar histórico por intervalo de datas",
            description = "Retorna histórico de mudanças em um intervalo de datas. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<TaskHistory>> getTaskHistoryByDateRange(
            @Parameter(description = "ID da task") @PathVariable Long taskId,
            @Parameter(description = "Data inicial (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Data final (formato: yyyy-MM-ddTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/tasks/{}/history/date-range - Fetching history by date range", taskId);
        Page<TaskHistory> history = taskHistoryRepository.findByTaskIdAndDateRange(
                taskId, startDate, endDate, pageable);
        return ResponseEntity.ok(history);
    }
}
