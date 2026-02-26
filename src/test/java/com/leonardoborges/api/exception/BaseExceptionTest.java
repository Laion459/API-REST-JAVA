package com.leonardoborges.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BaseException Tests")
class BaseExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void shouldCreateException_WithMessageOnly() {
        TaskNotFoundException ex = new TaskNotFoundException(1L);

        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("Task not found with id: 1"));
        assertEquals("TASK_NOT_FOUND", ex.getErrorCode());
    }


    @Test
    @DisplayName("Should create BusinessException with message and cause")
    void shouldCreateBusinessException_WithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        BusinessException ex = new BusinessException("Business rule violated", cause);

        assertNotNull(ex);
        assertEquals("Business rule violated", ex.getMessage());
        assertEquals("BUSINESS_RULE_VIOLATION", ex.getErrorCode());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateException_WithMessageAndCauseForOptimisticLocking() {
        Throwable cause = new IllegalArgumentException("Root cause");
        OptimisticLockingException ex = new OptimisticLockingException("Version conflict", cause);

        assertNotNull(ex);
        assertEquals("Version conflict", ex.getMessage());
        assertEquals("OPTIMISTIC_LOCKING_ERROR", ex.getErrorCode());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("Should create ResourceNotFoundException with resource type and ID")
    void shouldCreateResourceNotFoundException_WithResourceTypeAndId() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", 123L);

        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains("123"));
        assertEquals("RESOURCE_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    @DisplayName("Should create ResourceNotFoundException with message")
    void shouldCreateResourceNotFoundException_WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        assertNotNull(ex);
        assertEquals("Resource not found", ex.getMessage());
        assertEquals("RESOURCE_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    @DisplayName("Should create ValidationException with message and errors")
    void shouldCreateValidationException_WithMessageAndErrors() {
        java.util.Map<String, String> errors = java.util.Map.of("field1", "Error 1", "field2", "Error 2");
        ValidationException ex = new ValidationException("Validation failed", errors);

        assertNotNull(ex);
        assertEquals("Validation failed", ex.getMessage());
        assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        assertNotNull(ex.getValidationErrors());
        assertEquals(2, ex.getValidationErrors().size());
    }

    @Test
    @DisplayName("Should create TaskNotFoundException with task ID")
    void shouldCreateTaskNotFoundException_WithTaskId() {
        TaskNotFoundException ex = new TaskNotFoundException(123L);

        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("123"));
        assertEquals("TASK_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    @DisplayName("Should create TaskNotFoundException with message")
    void shouldCreateTaskNotFoundException_WithMessage() {
        TaskNotFoundException ex = new TaskNotFoundException("Custom task not found message");

        assertNotNull(ex);
        assertEquals("Custom task not found message", ex.getMessage());
        assertEquals("TASK_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    @DisplayName("Should create ValidationException with message only")
    void shouldCreateValidationException_WithMessageOnly() {
        ValidationException ex = new ValidationException("Validation failed");

        assertNotNull(ex);
        assertEquals("Validation failed", ex.getMessage());
        assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        assertNull(ex.getValidationErrors());
    }
}
