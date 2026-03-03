package com.leonardoborges.api.controller;

import com.leonardoborges.api.dto.TaskPageResponse;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.ReactiveTaskService;
import com.leonardoborges.api.util.SecurityUtils;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Reactive controller for concurrent read operations using WebFlux.
 * Uses WebFlux for better scalability and resource usage.
 * 
 * Reactive endpoints optimized for:
 * - High concurrency (thousands of simultaneous requests)
 * - Low latency (non-blocking operations)
 * - Better resource usage (event loop vs thread pool)
 * 
 * For write operations (POST/PUT/DELETE), use TaskController (MVC).
 */
@RestController
@RequestMapping("/api/v2/reactive/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reactive Tasks", description = "Reactive endpoints for task reading using WebFlux (non-blocking I/O)")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = false)
public class ReactiveTaskController {
    
    private final ReactiveTaskService reactiveTaskService;
    private final SecurityUtils securityUtils;
    
    @GetMapping("/{id}")
    @Operation(
            summary = "Get task by ID (Reactive)",
            description = "Returns a specific task using reactive programming (WebFlux) with non-blocking I/O. " +
                    "Suitable for high concurrency scenarios. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<TaskResponse>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        log.debug("GET /api/v2/reactive/tasks/{} - Fetching task reactively", id);
        Long userId = securityUtils.getCurrentUser().getId();
        return reactiveTaskService.getTaskById(id, userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
            summary = "List all tasks (Reactive)",
            description = "Returns a paginated list of tasks using reactive programming (WebFlux) with non-blocking I/O. " +
                    "Suitable for high concurrency scenarios. Requires JWT authentication.\n\n" +
                    "**Parameters:**\n" +
                    "- `page`: Page number (default: 0)\n" +
                    "- `size`: Page size (default: 20)\n\n" +
                    "**Architecture:** Uses non-blocking event loop for concurrent request handling.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task list returned successfully",
                    content = @Content(schema = @Schema(implementation = TaskPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<TaskPageResponse>> getAllTasks(
            @Parameter(description = "Page number (default: 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") 
            @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v2/reactive/tasks - Fetching all tasks reactively: page={}, size={}", page, size);
        Long userId = securityUtils.getCurrentUser().getId();
        
        return reactiveTaskService.getAllTasks(page, size, userId)
                .collectList()
                .map(tasks -> {
                    TaskPageResponse response = TaskPageResponse.builder()
                            .content(tasks)
                            .size(size)
                            .number(page)
                            .numberOfElements(tasks.size())
                            .first(page == 0)
                            .last(tasks.size() < size)
                            .empty(tasks.isEmpty())
                            .build();
                    return ResponseEntity.ok(response);
                });
    }
    
    @GetMapping("/status/{status}")
    @Operation(
            summary = "List tasks by status (Reactive)",
            description = "Returns a paginated list of tasks filtered by status using reactive programming (WebFlux). " +
                    "Uses non-blocking I/O for concurrent operations. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered task list by status returned successfully",
                    content = @Content(schema = @Schema(implementation = TaskPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<TaskPageResponse>> getTasksByStatus(
            @Parameter(description = "Task status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)") 
            @PathVariable Task.TaskStatus status,
            @Parameter(description = "Page number (default: 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") 
            @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v2/reactive/tasks/status/{} - Fetching tasks by status reactively", status);
        Long userId = securityUtils.getCurrentUser().getId();
        
        return reactiveTaskService.getTasksByStatus(status, page, size, userId)
                .collectList()
                .map(tasks -> {
                    TaskPageResponse response = TaskPageResponse.builder()
                            .content(tasks)
                            .size(size)
                            .number(page)
                            .numberOfElements(tasks.size())
                            .first(page == 0)
                            .last(tasks.size() < size)
                            .empty(tasks.isEmpty())
                            .build();
                    return ResponseEntity.ok(response);
                });
    }
    
    @GetMapping("/stats/count")
    @Operation(
            summary = "Task statistics (Reactive)",
            description = "Returns task count by status using reactive programming. " +
                    "Optimized for fast reads. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics returned successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<Map<String, Long>>> getTaskStats() {
        log.debug("GET /api/v2/reactive/tasks/stats/count - Fetching task statistics reactively");
        Long userId = securityUtils.getCurrentUser().getId();
        return reactiveTaskService.getTaskStats(userId)
                .map(ResponseEntity::ok);
    }
}
