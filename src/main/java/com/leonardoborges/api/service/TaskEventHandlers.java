package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.event.TaskCreatedEvent;
import com.leonardoborges.api.event.TaskDeletedEvent;
import com.leonardoborges.api.event.TaskUpdatedEvent;
import com.leonardoborges.api.metrics.TaskMetrics;
import com.leonardoborges.api.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventHandlers {
    
    private final TaskMetrics taskMetrics;
    private final CacheEvictionService cacheEvictionService;
    private final AuditService auditService;
    private final TaskHistoryService taskHistoryService;
    
    @EventListener
    @Async
    public void handleTaskCreated(TaskCreatedEvent event) {
        Task task = event.getTask();
        log.debug("Handling task created event for task ID: {}", task.getId());
        
        recordCreateAudit(task);
        recordCreateMetrics(task);
        evictCacheAfterCreate(task.getId(), task.getStatus());
    }
    
    @EventListener
    @Async
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        log.debug("Handling task updated event for task ID: {}", event.getTaskId());
        
        recordTaskHistory(event);
        recordUpdateAudit(event);
        evictCacheAfterUpdate(event);
    }
    
    @EventListener
    @Async
    public void handleTaskDeleted(TaskDeletedEvent event) {
        log.debug("Handling task deleted event for task ID: {}", event.getTaskId());
        
        recordDeleteAudit(event);
        recordDeleteMetrics();
        evictCacheAfterDelete(event.getTaskId(), event.getStatus());
    }
    
    private void recordCreateAudit(Task task) {
        auditService.audit("TASK_CREATED", "Task", task.getId(), 
                String.format("Title: %s, Status: %s", task.getTitle(), task.getStatus()));
    }
    
    private void recordCreateMetrics(Task task) {
        if (taskMetrics != null) {
            taskMetrics.incrementTaskCreated();
            taskMetrics.incrementTaskStatus(task.getStatus().name());
        }
    }
    
    private void evictCacheAfterCreate(Long taskId, Task.TaskStatus status) {
        cacheEvictionService.evictAfterCreate(taskId, status);
    }
    
    private void recordTaskHistory(TaskUpdatedEvent event) {
        Task oldTaskSnapshot = createTaskSnapshot(
                event.getOldTask(), event.getOldStatus());
        taskHistoryService.recordTaskChanges(
                event.getTaskId(), oldTaskSnapshot, event.getUpdatedTask());
    }
    
    private void recordUpdateAudit(TaskUpdatedEvent event) {
        auditService.auditWithChanges("TASK_UPDATED", "Task", event.getUpdatedTask().getId(), 
                String.format("Title: %s", event.getUpdatedTask().getTitle()), 
                event.getOldStatus().toString(), event.getUpdatedTask().getStatus().toString());
    }
    
    private void evictCacheAfterUpdate(TaskUpdatedEvent event) {
        cacheEvictionService.evictAfterUpdate(
                event.getTaskId(), event.getOldStatus(), event.getUpdatedTask().getStatus());
    }
    
    private void recordDeleteAudit(TaskDeletedEvent event) {
        auditService.audit("TASK_DELETED", "Task", event.getTaskId(), 
                String.format("Title: %s, Status: %s", event.getTaskTitle(), event.getStatus()));
    }
    
    private void recordDeleteMetrics() {
        if (taskMetrics != null) {
            taskMetrics.incrementTaskDeleted();
        }
    }
    
    private void evictCacheAfterDelete(Long taskId, Task.TaskStatus status) {
        cacheEvictionService.evictAfterDelete(taskId, status);
    }
    
    private Task createTaskSnapshot(
            Task task, 
            Task.TaskStatus oldStatus) {
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
