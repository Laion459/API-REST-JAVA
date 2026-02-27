package com.leonardoborges.api.service;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.exception.OptimisticLockingException;
import com.leonardoborges.api.exception.ValidationException;
import com.leonardoborges.api.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskValidationService {
    
    private final BaseValidationService baseValidationService;
    
    public void validateAndSanitizeTaskRequest(TaskRequest request) {
        if (request == null) {
            throw new ValidationException("Task request cannot be null");
        }
        
        baseValidationService.validateNotNullOrEmpty(request.getTitle(), "Task title");
        baseValidationService.validateSqlInjectionSafe(request.getTitle(), "title");
        baseValidationService.validateSqlInjectionSafe(request.getDescription(), "description");
        
        sanitizeTaskRequest(request);
        validateBusinessRules(request);
    }
    
    public void validateStatusTransition(Task.TaskStatus currentStatus, Task.TaskStatus newStatus) {
        if (newStatus == null) {
            return;
        }
        
        if (currentStatus == newStatus) {
            return;
        }
        
        if (currentStatus == Task.TaskStatus.COMPLETED && newStatus != Task.TaskStatus.CANCELLED) {
            throw new ValidationException("Cannot change status from COMPLETED to " + newStatus);
        }
        
        if (currentStatus == Task.TaskStatus.CANCELLED) {
            throw new ValidationException("Cannot change status of a CANCELLED task");
        }
    }
    
    private void validateBusinessRules(TaskRequest request) {
        baseValidationService.validateNotNegative(request.getPriority(), "Task priority");
    }
    
    private void sanitizeTaskRequest(TaskRequest request) {
        if (request.getTitle() != null) {
            String sanitizedTitle = baseValidationService.sanitizeAndTruncate(
                    request.getTitle(), TaskConstants.TITLE_MAX_LENGTH);
            request.setTitle(sanitizedTitle);
        }
        
        if (request.getDescription() != null) {
            String sanitizedDescription = baseValidationService.sanitizeAndTruncate(
                    request.getDescription(), TaskConstants.DESCRIPTION_MAX_LENGTH);
            request.setDescription(sanitizedDescription);
        }
    }
    
    /**
     * Validates version for optimistic locking.
     * Ensures the version in the request matches the current version of the task.
     * 
     * @param task The current task entity
     * @param request The update request containing the version
     * @throws OptimisticLockingException if version is missing or mismatched
     */
    public void validateVersionForOptimisticLocking(Task task, TaskRequest request) {
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
}
