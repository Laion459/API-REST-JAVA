package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for batch operations on tasks.
 * Optimized to process multiple operations efficiently.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchTaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskValidationService taskValidationService;
    private final CacheEvictionService cacheEvictionService;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;
    
    /**
     * Creates multiple tasks in a single transaction.
     * 
     * @param requests List of requests for creation
     * @return List of created tasks
     */
    @Transactional
    public List<TaskResponse> createBatch(List<TaskRequest> requests) {
        validateBatchSize(requests, "Batch request cannot be empty");
        log.info("Creating batch of {} tasks", requests.size());
        
        User currentUser = securityUtils.getCurrentUser();
        List<Task> tasks = prepareTasksForCreation(requests, currentUser);
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        
        log.info("Batch created successfully: {} tasks", savedTasks.size());
        handlePostBatchCreateActions(savedTasks);
        
        return mapToResponseList(savedTasks);
    }
    
    private void validateBatchSize(List<?> items, String emptyMessage) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(emptyMessage);
        }
        
        if (items.size() > 100) {
            throw new BusinessException("Batch size cannot exceed 100 tasks");
        }
    }
    
    private List<Task> prepareTasksForCreation(List<TaskRequest> requests, User currentUser) {
        return requests.stream()
                .map(request -> {
                    taskValidationService.validateAndSanitizeTaskRequest(request);
                    Task task = taskMapper.toEntity(request);
                    task.setUser(currentUser);
                    return task;
                })
                .collect(Collectors.toList());
    }
    
    private void handlePostBatchCreateActions(List<Task> savedTasks) {
        recordBatchCreateAudit(savedTasks);
        evictCacheForBatchCreate(savedTasks);
    }
    
    private void recordBatchCreateAudit(List<Task> tasks) {
        tasks.forEach(task -> 
                auditService.audit("TASK_CREATED", "Task", task.getId(), 
                        String.format("Batch creation: Title: %s", task.getTitle())));
    }
    
    private void evictCacheForBatchCreate(List<Task> tasks) {
        tasks.forEach(task -> 
                cacheEvictionService.evictAfterCreate(task.getId(), task.getStatus()));
    }
    
    private List<TaskResponse> mapToResponseList(List<Task> tasks) {
        return tasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Updates multiple tasks in a single transaction.
     * 
     * @param updates Map of taskId -> TaskRequest
     * @return List of updated tasks
     */
    @Transactional
    public List<TaskResponse> updateBatch(java.util.Map<Long, TaskRequest> updates) {
        validateBatchSize(new java.util.ArrayList<>(updates.values()), "Batch update cannot be empty");
        log.info("Updating batch of {} tasks", updates.size());
        
        User currentUser = securityUtils.getCurrentUser();
        List<Task> tasks = findTasksForBatchUpdate(updates.keySet(), currentUser);
        validateAllTasksFound(tasks, updates.size());
        
        prepareTasksForBatchUpdate(tasks, updates);
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        
        log.info("Batch updated successfully: {} tasks", savedTasks.size());
        handlePostBatchUpdateActions(savedTasks);
        
        return mapToResponseList(savedTasks);
    }
    
    private List<Task> findTasksForBatchUpdate(Set<Long> taskIds, User currentUser) {
        return taskRepository.findAllById(taskIds).stream()
                .filter(task -> task.getUser().getId().equals(currentUser.getId()))
                .filter(task -> !task.getDeleted())
                .collect(Collectors.toList());
    }
    
    private void validateAllTasksFound(List<Task> tasks, int expectedSize) {
        if (tasks.size() != expectedSize) {
            throw new BusinessException("Some tasks were not found or do not belong to the user");
        }
    }
    
    private void prepareTasksForBatchUpdate(List<Task> tasks, java.util.Map<Long, TaskRequest> updates) {
        tasks.forEach(task -> {
            TaskRequest request = updates.get(task.getId());
            if (request != null) {
                Task.TaskStatus oldStatus = task.getStatus();
                taskValidationService.validateAndSanitizeTaskRequest(request);
                taskValidationService.validateStatusTransition(oldStatus, request.getStatus());
                taskMapper.updateEntityFromRequest(task, request);
                cacheEvictionService.evictAfterUpdate(task.getId(), oldStatus, request.getStatus());
            }
        });
    }
    
    private void handlePostBatchUpdateActions(List<Task> savedTasks) {
        recordBatchUpdateAudit(savedTasks);
    }
    
    private void recordBatchUpdateAudit(List<Task> tasks) {
        tasks.forEach(task -> 
                auditService.audit("TASK_UPDATED", "Task", task.getId(), 
                        String.format("Batch update: Title: %s", task.getTitle())));
    }
    
    /**
     * Deletes multiple tasks in a single transaction (soft delete).
     * 
     * @param taskIds List of task IDs to delete
     */
    @Transactional
    public void deleteBatch(List<Long> taskIds) {
        validateBatchSize(taskIds, "Batch delete cannot be empty");
        log.info("Deleting batch of {} tasks", taskIds.size());
        
        User currentUser = securityUtils.getCurrentUser();
        List<Task> tasks = findTasksForBatchDelete(taskIds, currentUser);
        validateAllTasksFound(tasks, taskIds.size());
        
        performBatchSoftDelete(tasks, currentUser);
        taskRepository.saveAll(tasks);
        
        log.info("Batch deleted successfully: {} tasks", tasks.size());
        handlePostBatchDeleteActions(tasks);
    }
    
    private List<Task> findTasksForBatchDelete(List<Long> taskIds, User currentUser) {
        return taskRepository.findAllById(taskIds).stream()
                .filter(task -> task.getUser().getId().equals(currentUser.getId()))
                .filter(task -> !task.getDeleted())
                .collect(Collectors.toList());
    }
    
    private void performBatchSoftDelete(List<Task> tasks, User currentUser) {
        String username = currentUser.getUsername();
        tasks.forEach(task -> {
            task.softDelete(username);
            cacheEvictionService.evictAfterDelete(task.getId(), task.getStatus());
        });
    }
    
    private void handlePostBatchDeleteActions(List<Task> tasks) {
        recordBatchDeleteAudit(tasks);
    }
    
    private void recordBatchDeleteAudit(List<Task> tasks) {
        tasks.forEach(task -> 
                auditService.audit("TASK_DELETED", "Task", task.getId(), 
                        String.format("Batch deletion: Title: %s", task.getTitle())));
    }
}
