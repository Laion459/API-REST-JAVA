package com.leonardoborges.api.service;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.exception.OptimisticLockingException;
import com.leonardoborges.api.exception.ValidationException;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.util.InputSanitizer;
import com.leonardoborges.api.util.SqlInjectionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskValidationService {
    
    private final SqlInjectionValidator sqlInjectionValidator;
    private final InputSanitizer inputSanitizer;
    
    public void validateAndSanitizeTaskRequest(TaskRequest request) {
        if (request == null) {
            throw new ValidationException("Task request cannot be null");
        }
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Task title cannot be null or empty");
        }
        
        validateSqlInjection(request);
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
        if (request.getPriority() != null && request.getPriority() < 0) {
            throw new ValidationException("Task priority cannot be negative");
        }
    }
    
    private void validateSqlInjection(TaskRequest request) {
        if (request.getTitle() != null && !sqlInjectionValidator.isSafe(request.getTitle())) {
            throw new ValidationException("Invalid input detected in title field");
        }
        
        if (request.getDescription() != null && !sqlInjectionValidator.isSafe(request.getDescription())) {
            throw new ValidationException("Invalid input detected in description field");
        }
    }
    
    private void sanitizeTaskRequest(TaskRequest request) {
        if (request.getTitle() != null) {
            String sanitizedTitle = inputSanitizer.sanitizeAndTruncate(
                    request.getTitle(), TaskConstants.TITLE_MAX_LENGTH);
            request.setTitle(sanitizedTitle);
        }
        
        if (request.getDescription() != null) {
            String sanitizedDescription = inputSanitizer.sanitizeAndTruncate(
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
