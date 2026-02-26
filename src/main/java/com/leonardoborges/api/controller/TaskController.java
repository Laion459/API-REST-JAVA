package com.leonardoborges.api.controller;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskPageResponse;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.TaskService;
import com.leonardoborges.api.util.SortParameterValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "High-performance task management API")
public class TaskController {
    
    private final TaskService taskService;
    private final SortParameterValidator sortParameterValidator;
    
    @PostMapping
    @Operation(
            summary = "Create new task",
            description = "Creates a new task in the system. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Valid JWT token but insufficient permissions to access this resource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("POST /api/v1/tasks - Creating new task");
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(
            summary = "Get task by ID",
            description = "Returns a specific task by ID. Requires JWT authentication.",
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
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Valid JWT token but insufficient permissions to access this resource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        log.debug("GET /api/v1/tasks/{} - Fetching task", id);
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(
            summary = "List all tasks",
            description = "Returns a paginated list of all tasks. Supports pagination and sorting. Requires JWT authentication.\n\n" +
                    "**Pagination parameters:**\n" +
                    "- `page`: Page number (starts at 0, default: 0)\n" +
                    "- `size`: Page size (default: 20, recommended: 10-50)\n" +
                    "- `sort`: Field for sorting (e.g., `createdAt`, `title`, `priority`). Use `,desc` for descending order (e.g., `createdAt,desc`)\n\n" +
                    "**Example:** `?page=0&size=20&sort=createdAt,desc`",
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
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Valid JWT token but insufficient permissions to access this resource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TaskPageResponse> getAllTasks(
            @Parameter(description = "Pagination parameters. Example: ?page=0&size=20&sort=createdAt,desc. Default: page=0, size=20, sort=createdAt") 
            @PageableDefault(size = TaskConstants.DEFAULT_PAGE_SIZE, sort = "createdAt") Pageable pageable) {
        log.debug("GET /api/v1/tasks - Fetching all tasks with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Pageable validPageable = sortParameterValidator.validateAndNormalizeTaskSort(
                pageable, "createdAt", Sort.Direction.DESC);
        
        Page<TaskResponse> page = taskService.getAllTasks(validPageable);
        
        TaskPageResponse response = TaskPageResponse.builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .number(page.getNumber())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{status}")
    @Operation(
            summary = "List tasks by status",
            description = "Returns a paginated list of tasks filtered by status. Possible values: PENDING, IN_PROGRESS, COMPLETED, CANCELLED. Requires JWT authentication.\n\n" +
                    "**Pagination parameters:**\n" +
                    "- `page`: Page number (starts at 0, default: 0)\n" +
                    "- `size`: Page size (default: 20, recommended: 10-50)\n" +
                    "- `sort`: Field for sorting (e.g., `priority`, `createdAt`, `title`). Use `,desc` for descending order\n\n" +
                    "**Example:** `/api/v1/tasks/status/PENDING?page=0&size=20&sort=priority,desc`",
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
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Valid JWT token but insufficient permissions to access this resource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TaskPageResponse> getTasksByStatus(
            @Parameter(description = "Task status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)") @PathVariable Task.TaskStatus status,
            @Parameter(description = "Pagination parameters. Example: ?page=0&size=20&sort=priority,desc. Default: page=0, size=20, sort=priority") 
            @PageableDefault(size = TaskConstants.DEFAULT_PAGE_SIZE, sort = "priority") Pageable pageable) {
        log.debug("GET /api/v1/tasks/status/{} - Fetching tasks by status", status);
        
        Pageable validPageable = sortParameterValidator.validateAndNormalizeTaskSort(
                pageable, "priority", Sort.Direction.DESC);
        
        Page<TaskResponse> page = taskService.getTasksByStatus(status, validPageable);
        
        TaskPageResponse response = TaskPageResponse.builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .number(page.getNumber())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/count")
    @Operation(
            summary = "Task statistics",
            description = "Returns task count by status. Requires JWT authentication.\n\n" +
                    "**Response example:**\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"pending\": 5,\n" +
                    "  \"in_progress\": 3,\n" +
                    "  \"completed\": 10,\n" +
                    "  \"cancelled\": 1\n" +
                    "}\n" +
                    "```",
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
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Valid JWT token but insufficient permissions to access this resource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Map<String, Long>> getTaskStats() {
        log.debug("GET /api/v1/tasks/stats/count - Fetching task statistics");
        Map<String, Long> stats = Map.of(
                "pending", taskService.getTaskCountByStatus(Task.TaskStatus.PENDING),
                "in_progress", taskService.getTaskCountByStatus(Task.TaskStatus.IN_PROGRESS),
                "completed", taskService.getTaskCountByStatus(Task.TaskStatus.COMPLETED),
                "cancelled", taskService.getTaskCountByStatus(Task.TaskStatus.CANCELLED)
        );
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{id}")
    @Operation(
            summary = "Update task",
            description = "Updates an existing task. Supports optimistic locking through the 'version' field. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Valid JWT token but insufficient permissions to access this resource",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Version conflict - Task was modified by another user (optimistic locking)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "ID da tarefa") @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        log.info("PUT /api/v1/tasks/{} - Updating task", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete task",
            description = "Removes a task from the system. Requires JWT authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Task deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@Parameter(description = "ID da tarefa") @PathVariable Long id) {
        log.info("DELETE /api/v1/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
    }
}
