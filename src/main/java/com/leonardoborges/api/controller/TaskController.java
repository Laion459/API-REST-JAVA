package com.leonardoborges.api.controller;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "API para gerenciamento de tarefas de alta performance")
public class TaskController {
    
    private final TaskService taskService;
    
    @PostMapping
    @Operation(summary = "Criar nova tarefa", description = "Cria uma nova tarefa no sistema")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("POST /api/v1/tasks - Creating new task");
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID", description = "Retorna uma tarefa específica pelo ID")
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "ID da tarefa") @PathVariable Long id) {
        log.debug("GET /api/v1/tasks/{} - Fetching task", id);
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Listar todas as tarefas", description = "Retorna lista paginada de todas as tarefas")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @PageableDefault(size = TaskConstants.DEFAULT_PAGE_SIZE, sort = "createdAt") Pageable pageable) {
        log.debug("GET /api/v1/tasks - Fetching all tasks with pagination");
        Page<TaskResponse> response = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Listar tarefas por status", description = "Retorna lista paginada de tarefas filtradas por status")
    public ResponseEntity<Page<TaskResponse>> getTasksByStatus(
            @Parameter(description = "Status da tarefa") @PathVariable Task.TaskStatus status,
            @PageableDefault(size = TaskConstants.DEFAULT_PAGE_SIZE, sort = "priority") Pageable pageable) {
        log.debug("GET /api/v1/tasks/status/{} - Fetching tasks by status", status);
        Page<TaskResponse> response = taskService.getTasksByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/count")
    @Operation(summary = "Estatísticas de tarefas", description = "Retorna contagem de tarefas por status")
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
    @Operation(summary = "Atualizar tarefa", description = "Atualiza uma tarefa existente")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "ID da tarefa") @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        log.info("PUT /api/v1/tasks/{} - Updating task", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tarefa", description = "Remove uma tarefa do sistema")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@Parameter(description = "ID da tarefa") @PathVariable Long id) {
        log.info("DELETE /api/v1/tasks/{} - Deleting task", id);
        taskService.deleteTask(id);
    }
}
