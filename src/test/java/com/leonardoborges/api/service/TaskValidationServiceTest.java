package com.leonardoborges.api.service;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.exception.OptimisticLockingException;
import com.leonardoborges.api.exception.ValidationException;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.util.InputSanitizer;
import com.leonardoborges.api.util.SqlInjectionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskValidationService Tests")
class TaskValidationServiceTest {

    @Mock
    private SqlInjectionValidator sqlInjectionValidator;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private TaskValidationService validationService;

    private TaskRequest validTaskRequest;
    private Task task;

    @BeforeEach
    void setUp() {
        validTaskRequest = TaskRequest.builder()
                .title("Valid Task")
                .description("Valid Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(1L)
                .build();

        task = Task.builder()
                .id(1L)
                .title("Existing Task")
                .description("Existing Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(1L)
                .build();

        lenient().when(sqlInjectionValidator.isSafe(anyString())).thenReturn(true);
        lenient().when(inputSanitizer.sanitizeAndTruncate(anyString(), anyInt())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should validate and sanitize valid request successfully")
    void shouldValidateAndSanitizeValidRequestSuccessfully() {
        assertDoesNotThrow(() -> validationService.validateAndSanitizeTaskRequest(validTaskRequest));
        verify(sqlInjectionValidator, times(2)).isSafe(anyString());
        verify(inputSanitizer, times(2)).sanitizeAndTruncate(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw exception when request is null")
    void shouldThrowExceptionWhenRequestIsNull() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateAndSanitizeTaskRequest(null);
        });

        assertEquals("Task request cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        validTaskRequest.setTitle(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateAndSanitizeTaskRequest(validTaskRequest);
        });

        assertEquals("Task title cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when title is empty")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        validTaskRequest.setTitle("   ");

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateAndSanitizeTaskRequest(validTaskRequest);
        });

        assertEquals("Task title cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when SQL injection is detected in title")
    void shouldThrowExceptionWhenSqlInjectionIsDetectedInTitle() {
        validTaskRequest.setTitle("'; DROP TABLE tasks; --");
        when(sqlInjectionValidator.isSafe("'; DROP TABLE tasks; --")).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateAndSanitizeTaskRequest(validTaskRequest);
        });

        assertEquals("Invalid input detected in title field", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when SQL injection is detected in description")
    void shouldThrowExceptionWhenSqlInjectionIsDetectedInDescription() {
        validTaskRequest.setDescription("'; DROP TABLE tasks; --");
        when(sqlInjectionValidator.isSafe("'; DROP TABLE tasks; --")).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateAndSanitizeTaskRequest(validTaskRequest);
        });

        assertEquals("Invalid input detected in description field", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when priority is negative")
    void shouldThrowExceptionWhenPriorityIsNegative() {
        validTaskRequest.setPriority(-1);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateAndSanitizeTaskRequest(validTaskRequest);
        });

        assertEquals("Task priority cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should sanitize title and description")
    void shouldSanitizeTitleAndDescription() {
        validTaskRequest.setTitle("<script>alert('xss')</script>Valid Task");
        validTaskRequest.setDescription("<script>alert('xss')</script>Valid Description");
        
        when(inputSanitizer.sanitizeAndTruncate(anyString(), anyInt()))
                .thenAnswer(invocation -> "Sanitized: " + invocation.getArgument(0));

        validationService.validateAndSanitizeTaskRequest(validTaskRequest);

        assertTrue(validTaskRequest.getTitle().startsWith("Sanitized: "));
        assertTrue(validTaskRequest.getDescription().startsWith("Sanitized: "));
    }

    @Test
    @DisplayName("Should allow valid status transition")
    void shouldAllowValidStatusTransition() {
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(
                    Task.TaskStatus.PENDING, 
                    Task.TaskStatus.IN_PROGRESS
            );
        });
    }

    @Test
    @DisplayName("Should allow keeping same status")
    void shouldAllowKeepingSameStatus() {
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(
                    Task.TaskStatus.PENDING, 
                    Task.TaskStatus.PENDING
            );
        });
    }

    @Test
    @DisplayName("Should allow null as new status")
    void shouldAllowNullAsNewStatus() {
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(
                    Task.TaskStatus.PENDING, 
                    null
            );
        });
    }

    @Test
    @DisplayName("Should throw exception when trying to change from COMPLETED to status other than CANCELLED")
    void shouldThrowExceptionWhenTryingToChangeFromCompletedToOtherStatus() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateStatusTransition(
                    Task.TaskStatus.COMPLETED, 
                    Task.TaskStatus.IN_PROGRESS
            );
        });

        assertTrue(exception.getMessage().contains("Cannot change status from COMPLETED"));
    }

    @Test
    @DisplayName("Should allow changing from COMPLETED to CANCELLED")
    void shouldAllowChangingFromCompletedToCancelled() {
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(
                    Task.TaskStatus.COMPLETED, 
                    Task.TaskStatus.CANCELLED
            );
        });
    }

    @Test
    @DisplayName("Should throw exception when trying to change status of CANCELLED task")
    void shouldThrowExceptionWhenTryingToChangeStatusOfCancelledTask() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateStatusTransition(
                    Task.TaskStatus.CANCELLED, 
                    Task.TaskStatus.PENDING
            );
        });

        assertEquals("Cannot change status of a CANCELLED task", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate version for optimistic locking successfully")
    void shouldValidateVersionForOptimisticLockingSuccessfully() {
        task.setVersion(1L);
        validTaskRequest.setVersion(1L);

        assertDoesNotThrow(() -> {
            validationService.validateVersionForOptimisticLocking(task, validTaskRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when version is null in request")
    void shouldThrowExceptionWhenVersionIsNullInRequest() {
        validTaskRequest.setVersion(null);

        OptimisticLockingException exception = assertThrows(OptimisticLockingException.class, () -> {
            validationService.validateVersionForOptimisticLocking(task, validTaskRequest);
        });

        assertTrue(exception.getMessage().contains("Version field is required"));
    }

    @Test
    @DisplayName("Should throw exception when version does not match")
    void shouldThrowExceptionWhenVersionDoesNotMatch() {
        task.setVersion(1L);
        validTaskRequest.setVersion(2L);

        OptimisticLockingException exception = assertThrows(OptimisticLockingException.class, () -> {
            validationService.validateVersionForOptimisticLocking(task, validTaskRequest);
        });

        assertTrue(exception.getMessage().contains("Task version mismatch"));
        assertTrue(exception.getMessage().contains("Expected: 1"));
        assertTrue(exception.getMessage().contains("but was: 2"));
    }

    @Test
    @DisplayName("Should handle null description in validation")
    void shouldHandleNullDescriptionInValidation() {
        validTaskRequest.setDescription(null);

        assertDoesNotThrow(() -> validationService.validateAndSanitizeTaskRequest(validTaskRequest));
        verify(sqlInjectionValidator, times(1)).isSafe(anyString());
        verify(inputSanitizer, times(1)).sanitizeAndTruncate(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should handle empty description in validation")
    void shouldHandleEmptyDescriptionInValidation() {
        validTaskRequest.setDescription("");

        assertDoesNotThrow(() -> validationService.validateAndSanitizeTaskRequest(validTaskRequest));
        verify(sqlInjectionValidator, times(2)).isSafe(anyString());
        verify(inputSanitizer, times(2)).sanitizeAndTruncate(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should handle null priority in validation")
    void shouldHandleNullPriorityInValidation() {
        validTaskRequest.setPriority(null);

        assertDoesNotThrow(() -> validationService.validateAndSanitizeTaskRequest(validTaskRequest));
    }

    @Test
    @DisplayName("Should handle zero priority in validation")
    void shouldHandleZeroPriorityInValidation() {
        validTaskRequest.setPriority(0);

        assertDoesNotThrow(() -> validationService.validateAndSanitizeTaskRequest(validTaskRequest));
    }

    @Test
    @DisplayName("Should allow all valid status transitions from PENDING")
    void shouldAllowAllValidStatusTransitionsFromPending() {
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(Task.TaskStatus.PENDING, Task.TaskStatus.IN_PROGRESS);
        });
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(Task.TaskStatus.PENDING, Task.TaskStatus.COMPLETED);
        });
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(Task.TaskStatus.PENDING, Task.TaskStatus.CANCELLED);
        });
    }

    @Test
    @DisplayName("Should allow all valid status transitions from IN_PROGRESS")
    void shouldAllowAllValidStatusTransitionsFromInProgress() {
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(Task.TaskStatus.IN_PROGRESS, Task.TaskStatus.COMPLETED);
        });
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(Task.TaskStatus.IN_PROGRESS, Task.TaskStatus.CANCELLED);
        });
        assertDoesNotThrow(() -> {
            validationService.validateStatusTransition(Task.TaskStatus.IN_PROGRESS, Task.TaskStatus.PENDING);
        });
    }

    @Test
    @DisplayName("Should throw exception when trying to change from COMPLETED to PENDING")
    void shouldThrowExceptionWhenTryingToChangeFromCompletedToPending() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateStatusTransition(Task.TaskStatus.COMPLETED, Task.TaskStatus.PENDING);
        });

        assertTrue(exception.getMessage().contains("Cannot change status from COMPLETED"));
    }

    @Test
    @DisplayName("Should throw exception when trying to change from COMPLETED to IN_PROGRESS")
    void shouldThrowExceptionWhenTryingToChangeFromCompletedToInProgress() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateStatusTransition(Task.TaskStatus.COMPLETED, Task.TaskStatus.IN_PROGRESS);
        });

        assertTrue(exception.getMessage().contains("Cannot change status from COMPLETED"));
    }

    @Test
    @DisplayName("Should throw exception when trying to change from CANCELLED to IN_PROGRESS")
    void shouldThrowExceptionWhenTryingToChangeFromCancelledToInProgress() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateStatusTransition(Task.TaskStatus.CANCELLED, Task.TaskStatus.IN_PROGRESS);
        });

        assertEquals("Cannot change status of a CANCELLED task", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when trying to change from CANCELLED to COMPLETED")
    void shouldThrowExceptionWhenTryingToChangeFromCancelledToCompleted() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validationService.validateStatusTransition(Task.TaskStatus.CANCELLED, Task.TaskStatus.COMPLETED);
        });

        assertEquals("Cannot change status of a CANCELLED task", exception.getMessage());
    }

}
