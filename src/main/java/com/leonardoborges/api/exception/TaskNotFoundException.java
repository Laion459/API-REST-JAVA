package com.leonardoborges.api.exception;

/**
 * Exception thrown when a task is not found in the system.
 */
public class TaskNotFoundException extends BaseException {
    
    private static final String ERROR_CODE = "TASK_NOT_FOUND";
    
    public TaskNotFoundException(Long taskId) {
        super(String.format("Task not found with id: %d", taskId), ERROR_CODE);
    }
    
    public TaskNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
