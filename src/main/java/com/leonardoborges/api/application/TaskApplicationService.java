package com.leonardoborges.api.application;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Application Service Layer.
 * Orchestrates domain services and coordinates use cases.
 * Separates application logic from domain logic.
 * 
 * This layer represents the Application Services in Clean Architecture,
 * sitting between Controllers (Interface Adapters) and Domain Services.
 */
@Service
@RequiredArgsConstructor
public class TaskApplicationService {
    
    private final TaskService taskService;
    
    /**
     * Application use case: Create a new task.
     * Orchestrates validation, business rules, and persistence.
     */
    public TaskResponse createTask(@NonNull TaskRequest request) {
        return taskService.createTask(request);
    }
    
    /**
     * Application use case: Retrieve a task by ID.
     * Handles authorization and data retrieval.
     */
    public TaskResponse getTaskById(@NonNull Long id) {
        return taskService.getTaskById(id);
    }
    
    /**
     * Application use case: List all tasks with pagination.
     * Coordinates pagination and filtering.
     */
    public Page<TaskResponse> getAllTasks(@NonNull Pageable pageable) {
        return taskService.getAllTasks(pageable);
    }
    
    /**
     * Application use case: List tasks by status.
     * Coordinates filtering and pagination.
     */
    public Page<TaskResponse> getTasksByStatus(@NonNull Task.TaskStatus status, @NonNull Pageable pageable) {
        return taskService.getTasksByStatus(status, pageable);
    }
    
    /**
     * Application use case: Search tasks with advanced filters.
     * Coordinates complex filtering logic.
     */
    public Page<TaskResponse> searchTasks(@NonNull com.leonardoborges.api.dto.TaskFilterRequest filters, 
                                         @NonNull Pageable pageable) {
        return taskService.getTasksWithFilters(filters, pageable);
    }
    
    /**
     * Application use case: Get task statistics.
     * Coordinates data aggregation.
     */
    public Long getTaskCountByStatus(@NonNull Task.TaskStatus status) {
        return taskService.getTaskCountByStatus(status);
    }
    
    /**
     * Application use case: Update an existing task.
     * Orchestrates validation, optimistic locking, and persistence.
     */
    public TaskResponse updateTask(@NonNull Long id, @NonNull TaskRequest request) {
        return taskService.updateTask(id, request);
    }
    
    /**
     * Application use case: Partially update a task.
     * Handles partial updates and optimistic locking.
     */
    public TaskResponse patchTask(@NonNull Long id, @NonNull TaskRequest request) {
        return taskService.patchTask(id, request);
    }
    
    /**
     * Application use case: Delete a task.
     * Orchestrates soft delete and cleanup.
     */
    public void deleteTask(@NonNull Long id) {
        taskService.deleteTask(id);
    }
    
    /**
     * Application use case: Restore a deleted task.
     * Handles data recovery.
     */
    public void restoreTask(@NonNull Long id) {
        taskService.restoreTask(id);
    }
}
