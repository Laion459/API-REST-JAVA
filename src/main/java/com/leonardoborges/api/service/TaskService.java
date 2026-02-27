package com.leonardoborges.api.service;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskFilterRequest;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.event.TaskCreatedEvent;
import com.leonardoborges.api.event.TaskDeletedEvent;
import com.leonardoborges.api.event.TaskUpdatedEvent;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.exception.OptimisticLockingException;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.metrics.TaskMetrics;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.service.interfaces.ITaskService;
import com.leonardoborges.api.util.SecurityUtils;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService implements ITaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskValidationService taskValidationService;
    private final TaskMetrics taskMetrics;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher eventPublisher;
    
    private boolean isMetricsEnabled() {
        return taskMetrics != null;
    }
    
    @Override
    @Transactional
    public TaskResponse createTask(@NonNull TaskRequest request) {
        log.info("Creating new task: title={}", request.getTitle());
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskCreationTimer() : null;
        
        try {
            User currentUser = securityUtils.getCurrentUser();
            Task task = prepareTaskForCreation(request, currentUser);
            Task savedTask = taskRepository.save(task);
            
            log.info("Task created successfully with ID: {}", savedTask.getId());
            eventPublisher.publishEvent(new TaskCreatedEvent(this, savedTask));
            
            return taskMapper.toResponse(savedTask);
        } finally {
            if (isMetricsEnabled() && sample != null) {
                taskMetrics.recordTaskCreation(sample);
            }
        }
    }
    
    private Task prepareTaskForCreation(@NonNull TaskRequest request, @NonNull User currentUser) {
        taskValidationService.validateAndSanitizeTaskRequest(request);
        Task task = taskMapper.toEntity(request);
        task.setUser(currentUser);
        if (task.getPriority() == null) {
            task.setPriority(TaskConstants.DEFAULT_PRIORITY);
        }
        return task;
    }
    
    
    @Override
    @Cacheable(value = "tasks", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(@NonNull Long id) {
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
    
    @Override
    @Cacheable(value = "taskLists", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(@NonNull Pageable pageable) {
        log.debug("Fetching all tasks with pagination: {}", pageable);
        User currentUser = securityUtils.getCurrentUser();
        Page<Task> taskPage = taskRepository.findByUser(currentUser, pageable);
        return taskMapper.toResponsePage(taskPage);
    }
    
    @Override
    @Cacheable(value = "taskLists", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(@NonNull Task.TaskStatus status, @NonNull Pageable pageable) {
        log.debug("Fetching tasks with status: {}", status);
        User currentUser = securityUtils.getCurrentUser();
        Page<Task> taskPage = taskRepository.findByUserAndStatus(currentUser, status, pageable);
        return taskMapper.toResponsePage(taskPage);
    }
    
    @Override
    @Cacheable(value = "taskLists", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksWithFilters(@NonNull TaskFilterRequest filters, @NonNull Pageable pageable) {
        log.debug("Fetching tasks with filters: {}", filters);
        User currentUser = securityUtils.getCurrentUser();
        Page<Task> taskPage = taskRepository.findTasksWithFilters(currentUser, filters, pageable);
        return taskMapper.toResponsePage(taskPage);
    }
    
    @Override
    @Cacheable(value = "taskStats", keyGenerator = "taskCacheKeyGenerator")
    @Transactional(readOnly = true)
    public Long getTaskCountByStatus(@NonNull Task.TaskStatus status) {
        log.debug("Counting tasks with status: {}", status);
        User currentUser = securityUtils.getCurrentUser();
        return taskRepository.countByUserAndStatus(currentUser, status);
    }
    
    @Override
    @Transactional
    @CachePut(value = "tasks", keyGenerator = "taskCacheKeyGenerator")
    @Retryable(retryFor = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TaskResponse updateTask(@NonNull Long id, @NonNull TaskRequest request) {
        log.info("Updating task with ID: {}", id);
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskUpdateTimer() : null;
        
        try {
            User currentUser = securityUtils.getCurrentUser();
            Task task = findTaskForUpdate(id, currentUser);
            Task.TaskStatus oldStatus = task.getStatus();
            
            prepareTaskForUpdate(task, request, oldStatus);
            Task updatedTask = taskRepository.save(task);
            
            log.info("Task updated successfully with ID: {}, version: {}", updatedTask.getId(), updatedTask.getVersion());
            eventPublisher.publishEvent(new TaskUpdatedEvent(id, task, oldStatus, updatedTask));
            
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
    
    @Override
    @Transactional
    @CachePut(value = "tasks", keyGenerator = "taskCacheKeyGenerator")
    @Retryable(retryFor = {OptimisticLockingFailureException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TaskResponse patchTask(@NonNull Long id, @NonNull TaskRequest request) {
        log.info("Patching task with ID: {}", id);
        Timer.Sample sample = isMetricsEnabled() ? taskMetrics.startTaskUpdateTimer() : null;
        
        try {
            User currentUser = securityUtils.getCurrentUser();
            Task task = findTaskForUpdate(id, currentUser);
            Task.TaskStatus oldStatus = task.getStatus();
            
            prepareTaskForPartialUpdate(task, request, oldStatus);
            Task updatedTask = taskRepository.save(task);
            
            log.info("Task patched successfully with ID: {}, version: {}", updatedTask.getId(), updatedTask.getVersion());
            eventPublisher.publishEvent(new TaskUpdatedEvent(id, task, oldStatus, updatedTask));
            
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
    
    private void prepareTaskForPartialUpdate(@NonNull Task task, @NonNull TaskRequest request, @NonNull Task.TaskStatus oldStatus) {
        if (request.getVersion() != null) {
            taskValidationService.validateVersionForOptimisticLocking(task, request);
        }
        
        if (request.getTitle() != null || request.getDescription() != null || 
            request.getStatus() != null || request.getPriority() != null) {
            taskValidationService.validateAndSanitizeTaskRequest(request);
        }
        
        if (request.getStatus() != null && !oldStatus.equals(request.getStatus())) {
            taskValidationService.validateStatusTransition(oldStatus, request.getStatus());
        }
        
        taskMapper.patchEntityFromRequest(task, request);
    }
    
    private Task findTaskForUpdate(@NonNull Long id, @NonNull User currentUser) {
        return taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
    
    private void prepareTaskForUpdate(@NonNull Task task, @NonNull TaskRequest request, @NonNull Task.TaskStatus oldStatus) {
        taskValidationService.validateVersionForOptimisticLocking(task, request);
        taskValidationService.validateAndSanitizeTaskRequest(request);
        taskValidationService.validateStatusTransition(oldStatus, request.getStatus());
        taskMapper.updateEntityFromRequest(task, request);
    }
    
    
    @Override
    @Transactional
    public void deleteTask(@NonNull Long id) {
        log.info("Deleting task with ID: {}", id);
        
        User currentUser = securityUtils.getCurrentUser();
        Task task = findTaskForDeletion(id, currentUser);
        Task.TaskStatus status = task.getStatus();
        String taskTitle = task.getTitle();
        
        performSoftDelete(task, currentUser);
        log.info("Task soft deleted successfully with ID: {}", id);
        
        eventPublisher.publishEvent(new TaskDeletedEvent(id, taskTitle, status));
    }
    
    private Task findTaskForDeletion(@NonNull Long id, @NonNull User currentUser) {
        return taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
    
    private void performSoftDelete(@NonNull Task task, @NonNull User currentUser) {
        task.softDelete(currentUser.getUsername());
        taskRepository.save(task);
    }
    
    
    @Override
    @Transactional
    public void restoreTask(@NonNull Long id) {
        log.info("Restoring task with ID: {}", id);
        
        User currentUser = securityUtils.getCurrentUser();
        Task task = findTaskForRestore(id, currentUser);
        
        performRestore(task);
        log.info("Task restored successfully with ID: {}", id);
        
        handlePostRestoreActions(id, task);
    }
    
    private Task findTaskForRestore(@NonNull Long id, @NonNull User currentUser) {
        Task task = taskRepository.findByIdAndUserIncludingDeleted(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        if (!task.getDeleted()) {
            throw new BusinessException("Task is not deleted");
        }
        
        return task;
    }
    
    private void performRestore(@NonNull Task task) {
        task.restore();
        taskRepository.save(task);
    }
    
    private void handlePostRestoreActions(@NonNull Long taskId, @NonNull Task task) {
        eventPublisher.publishEvent(new TaskCreatedEvent(this, task));
    }
}
