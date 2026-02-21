package com.leonardoborges.api.service;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final CacheService cacheService;
    private final InputSanitizer inputSanitizer;
    
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task: title={}", request.getTitle());
        
        // Sanitize input
        String sanitizedTitle = inputSanitizer.sanitizeAndTruncate(
            request.getTitle(), TaskConstants.TITLE_MAX_LENGTH);
        String sanitizedDescription = inputSanitizer.sanitizeAndTruncate(
            request.getDescription(), TaskConstants.DESCRIPTION_MAX_LENGTH);
        
        Task task = Task.builder()
                .title(sanitizedTitle)
                .description(sanitizedDescription)
                .status(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : TaskConstants.DEFAULT_PRIORITY)
                .build();
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        
        // Invalidate related caches after creation
        cacheService.evictTaskLists();
        cacheService.evictTaskStats(savedTask.getStatus().name());
        
        return mapToResponse(savedTask);
    }
    
    @Cacheable(value = "tasks", key = "#id")
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return mapToResponse(task);
    }
    
    @Cacheable(value = "tasks", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.debug("Fetching all tasks with pagination: {}", pageable);
        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }
    
    @Cacheable(value = "tasks", key = "'status-' + #status + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(Task.TaskStatus status, Pageable pageable) {
        log.debug("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }
    
    @Cacheable(value = "taskStats", key = "#status")
    @Transactional(readOnly = true)
    public Long getTaskCountByStatus(Task.TaskStatus status) {
        log.debug("Counting tasks with status: {}", status);
        return taskRepository.countByStatus(status);
    }
    
    @Transactional
    @CachePut(value = "tasks", key = "#id")
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with ID: {}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        Task.TaskStatus oldStatus = task.getStatus();
        
        // Sanitize input before updating
        String sanitizedTitle = inputSanitizer.sanitizeAndTruncate(
            request.getTitle(), TaskConstants.TITLE_MAX_LENGTH);
        String sanitizedDescription = inputSanitizer.sanitizeAndTruncate(
            request.getDescription(), TaskConstants.DESCRIPTION_MAX_LENGTH);
        
        task.setTitle(sanitizedTitle);
        task.setDescription(sanitizedDescription);
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        
        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with ID: {}", updatedTask.getId());
        
        // Selective cache eviction - only evict what changed
        cacheService.evictTask(id); // Evict old cached version
        cacheService.evictTaskLists(); // Evict paginated lists
        cacheService.evictTasksByStatus(oldStatus.name()); // Evict old status list
        if (request.getStatus() != null && !oldStatus.equals(request.getStatus())) {
            cacheService.evictTasksByStatus(request.getStatus().name()); // Evict new status list
            cacheService.evictTaskStats(oldStatus.name()); // Evict old status stats
            cacheService.evictTaskStats(request.getStatus().name()); // Evict new status stats
        } else {
            cacheService.evictTaskStats(oldStatus.name()); // Evict stats if status unchanged
        }
        
        return mapToResponse(updatedTask);
    }
    
    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        
        // Get task before deletion to know its status for cache eviction
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        Task.TaskStatus status = task.getStatus();
        taskRepository.deleteById(id);
        log.info("Task deleted successfully with ID: {}", id);
        
        // Selective cache eviction - only evict what's affected
        cacheService.evictTask(id); // Evict the deleted task
        cacheService.evictTaskLists(); // Evict paginated lists
        cacheService.evictTasksByStatus(status.name()); // Evict status-filtered list
        cacheService.evictTaskStats(status.name()); // Evict stats for this status
    }
    
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
