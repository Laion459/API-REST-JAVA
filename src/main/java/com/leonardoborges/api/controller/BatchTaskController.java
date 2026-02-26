package com.leonardoborges.api.controller;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.service.BatchTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for batch operations on tasks.
 * Optimized to process multiple operations efficiently.
 */
@RestController
@RequestMapping("/api/v1/tasks/batch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Batch Tasks", description = "Batch operations for high performance")
public class BatchTaskController {
    
    private final BatchTaskService batchTaskService;
    
    @Data
    public static class BatchCreateRequest {
        @NotEmpty(message = "Tasks list cannot be empty")
        @Size(max = 100, message = "Batch size cannot exceed 100 tasks")
        private List<@Valid TaskRequest> tasks;
    }
    
    @Data
    public static class BatchUpdateRequest {
        @NotEmpty(message = "Updates map cannot be empty")
        @Size(max = 100, message = "Batch size cannot exceed 100 tasks")
        private Map<Long, @Valid TaskRequest> updates;
    }
    
    @Data
    public static class BatchDeleteRequest {
        @NotEmpty(message = "Task IDs list cannot be empty")
        @Size(max = 100, message = "Batch size cannot exceed 100 tasks")
        private List<Long> taskIds;
    }
    
    @PostMapping("/create")
    @Operation(
            summary = "Create multiple tasks in batch",
            description = "Creates multiple tasks in a single transaction. Maximum of 100 tasks per batch. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tasks created successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or batch too large",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<TaskResponse>> createBatch(@Valid @RequestBody BatchCreateRequest request) {
        log.info("POST /api/v1/tasks/batch/create - Creating batch of {} tasks", request.getTasks().size());
        List<TaskResponse> responses = batchTaskService.createBatch(request.getTasks());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @PutMapping("/update")
    @Operation(
            summary = "Update multiple tasks in batch",
            description = "Updates multiple tasks in a single transaction. Maximum of 100 tasks per batch. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or batch too large",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<TaskResponse>> updateBatch(@Valid @RequestBody BatchUpdateRequest request) {
        log.info("PUT /api/v1/tasks/batch/update - Updating batch of {} tasks", request.getUpdates().size());
        List<TaskResponse> responses = batchTaskService.updateBatch(request.getUpdates());
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/delete")
    @Operation(
            summary = "Delete multiple tasks in batch",
            description = "Deletes multiple tasks (soft delete) in a single transaction. Maximum of 100 tasks per batch. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Tasks deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or batch too large",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(@Valid @RequestBody BatchDeleteRequest request) {
        log.info("DELETE /api/v1/tasks/batch/delete - Deleting batch of {} tasks", request.getTaskIds().size());
        batchTaskService.deleteBatch(request.getTaskIds());
    }
}
