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
 * Controller for querying task change history.
 * Allows tracking all changes made to a task.
 */
@RestController
@RequestMapping("/api/v1/tasks/{taskId}/history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task History", description = "Task change history")
public class TaskHistoryController {
    
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskHistoryService taskHistoryService;
    
    @GetMapping
    @Operation(
            summary = "List task change history",
            description = "Returns a paginated list of all changes made to a specific task. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "History returned successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = com.leonardoborges.api.exception.ErrorResponse.class))
            )
    })
    public ResponseEntity<Page<TaskHistory>> getTaskHistory(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("GET /api/v1/tasks/{}/history - Fetching task history", taskId);
        Page<TaskHistory> history = taskHistoryRepository.findByTaskId(taskId, pageable);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/field/{fieldName}")
    @Operation(
            summary = "List history of a specific field",
            description = "Returns change history of a specific field of a task. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<List<TaskHistory>> getTaskHistoryByField(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "Field name (e.g., title, status, priority)") @PathVariable String fieldName) {
        log.debug("GET /api/v1/tasks/{}/history/field/{} - Fetching field history", taskId, fieldName);
        List<TaskHistory> history = taskHistoryRepository.findByTaskIdAndFieldName(taskId, fieldName);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/all")
    @Operation(
            summary = "List all task history",
            description = "Returns all change history of a task ordered by date. Requires JWT authentication.",
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
            summary = "List history by date range",
            description = "Returns change history within a date range. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<Page<TaskHistory>> getTaskHistoryByDateRange(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
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
