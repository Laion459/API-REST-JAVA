package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.OptimisticLockingException;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.metrics.TaskMetrics;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final CacheService cacheService;
    private final TaskMapper taskMapper;
    private final TaskValidationService taskValidationService;
    private final TaskMetrics taskMetrics;
    private final AuditService auditService;
    
    private boolean isMetricsEnabled() {
        return taskMetrics != null;
    }
    
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task: title={}", request.getTitle());
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskCreationTimer() : null;
        
        try {
            taskValidationService.validateAndSanitizeTaskRequest(request);
            
            Task task = taskMapper.toEntity(request);
            if (task.getPriority() == null) {
                task.setPriority(TaskConstants.DEFAULT_PRIORITY);
            }
            
            Task savedTask = taskRepository.save(task);
            log.info("Task created successfully with ID: {}", savedTask.getId());
            
            auditService.audit("TASK_CREATED", "Task", savedTask.getId(), 
                    String.format("Title: %s, Status: %s", savedTask.getTitle(), savedTask.getStatus()));
            
            if (isMetricsEnabled()) {
                taskMetrics.incrementTaskCreated();
                taskMetrics.incrementTaskStatus(savedTask.getStatus().name());
            }
            
            cacheService.evictTaskLists();
            cacheService.evictTaskStats(savedTask.getStatus().name());
            
            return taskMapper.toResponse(savedTask);
        } finally {
            if (isMetricsEnabled() && sample != null) {
                taskMetrics.recordTaskCreation(sample);
            }
        }
    }
    
    @Cacheable(value = "tasks", key = "#id")
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with ID: {}", id);
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskRetrievalTimer() : null;
        
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new TaskNotFoundException(id));
            if (isMetricsEnabled()) {
                taskMetrics.incrementTaskRetrieved();
            }
            return taskMapper.toResponse(task);
        } finally {
            if (isMetricsEnabled() && sample != null) {
                taskMetrics.recordTaskRetrieval(sample);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.debug("Fetching all tasks with pagination: {}", pageable);
        Page<Task> taskPage = taskRepository.findAll(pageable);
        return taskMapper.toResponsePage(taskPage);
    }
    
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(Task.TaskStatus status, Pageable pageable) {
        log.debug("Fetching tasks with status: {}", status);
        Page<Task> taskPage = taskRepository.findByStatus(status, pageable);
        return taskMapper.toResponsePage(taskPage);
    }
    
    @Cacheable(value = "taskStats", key = "#status")
    @Transactional(readOnly = true)
    public Long getTaskCountByStatus(Task.TaskStatus status) {
        log.debug("Counting tasks with status: {}", status);
        return taskRepository.countByStatus(status);
    }
    
    @Transactional
    @CachePut(value = "tasks", key = "#id")
    @Retryable(retryFor = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with ID: {}", id);
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskUpdateTimer() : null;
        
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new TaskNotFoundException(id));
            
            validateVersionForOptimisticLocking(task, request);
            Task.TaskStatus oldStatus = task.getStatus();
            
            taskValidationService.validateAndSanitizeTaskRequest(request);
            taskValidationService.validateStatusTransition(oldStatus, request.getStatus());
            taskMapper.updateEntityFromRequest(task, request);
            
            Task updatedTask = taskRepository.save(task);
            log.info("Task updated successfully with ID: {}, version: {}", updatedTask.getId(), updatedTask.getVersion());
            
            auditService.auditWithChanges("TASK_UPDATED", "Task", updatedTask.getId(), 
                    String.format("Title: %s", updatedTask.getTitle()), 
                    oldStatus.toString(), updatedTask.getStatus().toString());
            
            if (isMetricsEnabled()) {
                taskMetrics.incrementTaskUpdated();
                if (request.getStatus() != null && !oldStatus.equals(request.getStatus())) {
                    taskMetrics.incrementTaskStatus(request.getStatus().name());
                }
            }
            
            evictCacheAfterUpdate(id, oldStatus, request.getStatus());
            
            return taskMapper.toResponse(updatedTask);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic locking conflict for task ID: {}", id);
            throw new OptimisticLockingException(
                    "The task has been modified by another user. Please refresh and try again.", e);
        } finally {
            if (isMetricsEnabled() && sample != null) {
                taskMetrics.recordTaskUpdate(sample);
            }
        }
    }
    
    private void validateVersionForOptimisticLocking(Task task, TaskRequest request) {
        if (request.getVersion() == null) {
            log.warn("Task update attempted without version field. Optimistic locking may not work correctly for task ID: {}", task.getId());
            return;
        }
        
        if (!request.getVersion().equals(task.getVersion())) {
            throw new OptimisticLockingException(
                    String.format("Task version mismatch. Expected: %d, but was: %d. Please refresh and try again.",
                            task.getVersion(), request.getVersion()));
        }
    }
    
    private void evictCacheAfterUpdate(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus) {
        cacheService.evictTask(taskId);
        cacheService.evictTaskLists();
        cacheService.evictTasksByStatus(oldStatus.name());
        
        if (newStatus != null && !oldStatus.equals(newStatus)) {
            cacheService.evictTasksByStatus(newStatus.name());
            cacheService.evictTaskStats(oldStatus.name());
            cacheService.evictTaskStats(newStatus.name());
        } else {
            cacheService.evictTaskStats(oldStatus.name());
        }
    }
    
    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        Task.TaskStatus status = task.getStatus();
        String taskTitle = task.getTitle();
        taskRepository.deleteById(id);
        log.info("Task deleted successfully with ID: {}", id);
        
        auditService.audit("TASK_DELETED", "Task", id, 
                String.format("Title: %s, Status: %s", taskTitle, status));
        
        if (isMetricsEnabled()) {
            taskMetrics.incrementTaskDeleted();
        }
        
        evictCacheAfterDelete(id, status);
    }
    
    private void evictCacheAfterDelete(Long taskId, Task.TaskStatus status) {
        cacheService.evictTask(taskId);
        cacheService.evictTaskLists();
        cacheService.evictTasksByStatus(status.name());
        cacheService.evictTaskStats(status.name());
    }
}
