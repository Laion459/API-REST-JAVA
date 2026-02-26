package com.leonardoborges.api.service;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.TaskHistory;
import com.leonardoborges.api.repository.TaskHistoryRepository;
import com.leonardoborges.api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing task change history.
 * Records all field changes for complete auditing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskHistoryService {
    
    private final TaskHistoryRepository taskHistoryRepository;
    private final SecurityUtils securityUtils;
    
    /**
     * Records a field change asynchronously.
     * 
     * @param taskId Task ID
     * @param fieldName Name of the changed field
     * @param oldValue Previous value
     * @param newValue New value
     */
    @Async
    @Transactional
    public void recordFieldChange(Long taskId, String fieldName, String oldValue, String newValue) {
        try {
            String changedBy = securityUtils.getCurrentUsername();
            
            TaskHistory history = TaskHistory.builder()
                    .taskId(taskId)
                    .fieldName(fieldName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .changedBy(changedBy)
                    .changedAt(LocalDateTime.now())
                    .build();
            
            taskHistoryRepository.save(history);
            log.debug("Task history recorded: taskId={}, field={}", taskId, fieldName);
        } catch (Exception e) {
            log.error("Failed to record task history: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Records multiple changes of a task.
     * 
     * @param taskId Task ID
     * @param oldTask Task before changes
     * @param newTask Task after changes
     */
    @Async
    @Transactional
    public void recordTaskChanges(Long taskId, Task oldTask, Task newTask) {
        if (oldTask == null || newTask == null) {
            return;
        }
        
        if (!equalsOrNull(oldTask.getTitle(), newTask.getTitle())) {
            recordFieldChange(taskId, "title", 
                    oldTask.getTitle(), newTask.getTitle());
        }
        
        if (!equalsOrNull(oldTask.getDescription(), newTask.getDescription())) {
            recordFieldChange(taskId, "description", 
                    oldTask.getDescription(), newTask.getDescription());
        }
        
        if (oldTask.getStatus() != newTask.getStatus()) {
            recordFieldChange(taskId, "status", 
                    oldTask.getStatus() != null ? oldTask.getStatus().name() : null,
                    newTask.getStatus() != null ? newTask.getStatus().name() : null);
        }
        
        if (!equalsOrNull(oldTask.getPriority(), newTask.getPriority())) {
            recordFieldChange(taskId, "priority", 
                    oldTask.getPriority() != null ? oldTask.getPriority().toString() : null,
                    newTask.getPriority() != null ? newTask.getPriority().toString() : null);
        }
    }
    
    private boolean equalsOrNull(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    /**
     * Gets complete history of a task ordered by date.
     * 
     * @param taskId Task ID
     * @return History list ordered by date (most recent first)
     */
    @Transactional(readOnly = true)
    public List<TaskHistory> getTaskHistory(Long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId);
    }
}
