package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.exception.OptimisticLockingException;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.metrics.TaskMetrics;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.util.SecurityUtils;
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
    private final CacheEvictionService cacheEvictionService;
    private final TaskMapper taskMapper;
    private final TaskValidationService taskValidationService;
    private final TaskMetrics taskMetrics;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final TaskHistoryService taskHistoryService;
    
    private boolean isMetricsEnabled() {
        return taskMetrics != null;
    }
    
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task: title={}", request.getTitle());
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskCreationTimer() : null;
        
        try {
            User currentUser = securityUtils.getCurrentUser();
            taskValidationService.validateAndSanitizeTaskRequest(request);
            
            Task task = taskMapper.toEntity(request);
            task.setUser(currentUser);
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
            
            cacheEvictionService.evictAfterCreate(savedTask.getId(), savedTask.getStatus());
            
            return taskMapper.toResponse(savedTask);
        } finally {
            if (isMetricsEnabled() && sample != null) {
                taskMetrics.recordTaskCreation(sample);
            }
        }
    }
    
    @Cacheable(value = "tasks", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with ID: {}", id);
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskRetrievalTimer() : null;
        
        try {
            User currentUser = securityUtils.getCurrentUser();
            Task task = taskRepository.findByIdAndUser(id, currentUser)
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
    
    @Cacheable(value = "taskLists", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.debug("Fetching all tasks with pagination: {}", pageable);
        User currentUser = securityUtils.getCurrentUser();
        Page<Task> taskPage = taskRepository.findByUser(currentUser, pageable);
        return taskMapper.toResponsePage(taskPage);
    }
    
    @Cacheable(value = "taskLists", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(Task.TaskStatus status, Pageable pageable) {
        log.debug("Fetching tasks with status: {}", status);
        User currentUser = securityUtils.getCurrentUser();
        Page<Task> taskPage = taskRepository.findByUserAndStatus(currentUser, status, pageable);
        return taskMapper.toResponsePage(taskPage);
    }
    
    @Cacheable(value = "taskStats", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public Long getTaskCountByStatus(Task.TaskStatus status) {
        log.debug("Counting tasks with status: {}", status);
        User currentUser = securityUtils.getCurrentUser();
        return taskRepository.countByUserAndStatus(currentUser, status);
    }
    
    @Transactional
    @CachePut(value = "tasks", keyGenerator = "taskCacheKeyGenerator")
    @Retryable(retryFor = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with ID: {}", id);
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskUpdateTimer() : null;
        
        try {
            User currentUser = securityUtils.getCurrentUser();
            Task task = taskRepository.findByIdAndUser(id, currentUser)
                    .orElseThrow(() -> new TaskNotFoundException(id));
            
            validateVersionForOptimisticLocking(task, request);
            Task.TaskStatus oldStatus = task.getStatus();
            
            taskValidationService.validateAndSanitizeTaskRequest(request);
            taskValidationService.validateStatusTransition(oldStatus, request.getStatus());
            taskMapper.updateEntityFromRequest(task, request);
            
            Task updatedTask = taskRepository.save(task);
            log.info("Task updated successfully with ID: {}, version: {}", updatedTask.getId(), updatedTask.getVersion());
            
            Task oldTaskSnapshot = createTaskSnapshot(task, oldStatus);
            taskHistoryService.recordTaskChanges(id, oldTaskSnapshot, updatedTask);
            
            auditService.auditWithChanges("TASK_UPDATED", "Task", updatedTask.getId(), 
                    String.format("Title: %s", updatedTask.getTitle()), 
                    oldStatus.toString(), updatedTask.getStatus().toString());
            
            if (isMetricsEnabled()) {
                taskMetrics.incrementTaskUpdated();
                if (request.getStatus() != null && !oldStatus.equals(request.getStatus())) {
                    taskMetrics.incrementTaskStatus(request.getStatus().name());
                }
            }
            
            cacheEvictionService.evictAfterUpdate(id, oldStatus, request.getStatus());
            
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
            throw new OptimisticLockingException(
                    "Version field is required for optimistic locking. Please include the current version of the task.");
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
        
        User currentUser = securityUtils.getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        Task.TaskStatus status = task.getStatus();
        String taskTitle = task.getTitle();
        
        // Soft delete
        task.softDelete(currentUser.getUsername());
        taskRepository.save(task);
        
        log.info("Task soft deleted successfully with ID: {}", id);
        
        auditService.audit("TASK_DELETED", "Task", id, 
                String.format("Title: %s, Status: %s", taskTitle, status));
        
        if (isMetricsEnabled()) {
            taskMetrics.incrementTaskDeleted();
        }
        
        cacheEvictionService.evictAfterDelete(id, status);
    }
    
    @Transactional
    public void restoreTask(Long id) {
        log.info("Restoring task with ID: {}", id);
        
        User currentUser = securityUtils.getCurrentUser();
        Task task = taskRepository.findByIdAndUserIncludingDeleted(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        if (!task.getDeleted()) {
            throw new BusinessException("Task is not deleted");
        }
        
        task.restore();
        taskRepository.save(task);
        
        log.info("Task restored successfully with ID: {}", id);
        
        auditService.audit("TASK_RESTORED", "Task", id, 
                String.format("Title: %s", task.getTitle()));
        
        cacheEvictionService.evictAfterCreate(id, task.getStatus());
    }
    
    /**
     * Creates a snapshot of the task before changes for history tracking.
     * Captures the current state before applying changes.
     */
    private Task createTaskSnapshot(Task task, Task.TaskStatus oldStatus) {
        Task snapshot = new Task();
        snapshot.setId(task.getId());
        snapshot.setTitle(task.getTitle());
        snapshot.setDescription(task.getDescription());
        snapshot.setStatus(oldStatus != null ? oldStatus : task.getStatus());
        snapshot.setPriority(task.getPriority());
        snapshot.setVersion(task.getVersion());
        return snapshot;
    }
}
