package com.leonardoborges.api.service;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.TaskHistory;
import com.leonardoborges.api.repository.TaskHistoryRepository;
import com.leonardoborges.api.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskHistoryService.
 * 
 * Uses @ExtendWith(MockitoExtension.class) for pure tests with mocks.
 * Does not load Spring context, making tests faster and isolated.
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Descriptive test names with @DisplayName
 * - Behavior verification (verify)
 * - Tests for success and error cases
 */
@ExtendWith(MockitoExtension.class)
class TaskHistoryServiceTest {
    
    @Mock
    private TaskHistoryRepository taskHistoryRepository;
    
    @Mock
    private SecurityUtils securityUtils;
    
    private TaskHistoryService taskHistoryService;
    
    @BeforeEach
    void setUp() {
        taskHistoryService = new TaskHistoryService(
                taskHistoryRepository,
                securityUtils
        );
        
        lenient().when(securityUtils.getCurrentUsername()).thenReturn("testuser");
    }
    
    @Test
    @DisplayName("Should record individual field change")
    void shouldRecordFieldChange_WhenFieldChanged() {
        // Arrange
        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        taskHistoryService.recordFieldChange(1L, "title", "Old Title", "New Title");
        
        // Assert
        verify(taskHistoryRepository, times(1)).save(any(TaskHistory.class));
    }
    
    @Test
    @DisplayName("Should record multiple field changes")
    void shouldRecordTaskChanges_WhenMultipleFieldsChanged() {
        // Arrange
        Task oldTask = createTask(1L, "Old Title", "Old Description", Task.TaskStatus.PENDING, 1);
        Task newTask = createTask(1L, "New Title", "New Description", Task.TaskStatus.IN_PROGRESS, 2);
        
        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);
        
        verify(taskHistoryRepository, atLeast(3)).save(any(TaskHistory.class));
    }
    
    @Test
    @DisplayName("Should not record history when fields did not change")
    void shouldNotRecordUnchangedFields_WhenNoChanges() {
        // Arrange
        Task task = createTask(1L, "Same Title", "Same Description", Task.TaskStatus.PENDING, 1);
        
        // Act
        taskHistoryService.recordTaskChanges(1L, task, task);
        
        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }
    
    @Test
    @DisplayName("Should get task history ordered by date")
    void shouldGetTaskHistory_OrderedByDate() {
        // Arrange
        TaskHistory history1 = TaskHistory.builder()
                .id(1L)
                .taskId(1L)
                .fieldName("title")
                .oldValue("Old")
                .newValue("New")
                .build();
        
        TaskHistory history2 = TaskHistory.builder()
                .id(2L)
                .taskId(1L)
                .fieldName("status")
                .oldValue("PENDING")
                .newValue("IN_PROGRESS")
                .build();
        
        when(taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(1L))
                .thenReturn(Arrays.asList(history1, history2));
        
        // Act
        List<TaskHistory> history = taskHistoryService.getTaskHistory(1L);
        
        // Assert
        assertEquals(2, history.size());
        verify(taskHistoryRepository, times(1)).findByTaskIdOrderByChangedAtDesc(1L);
    }

    @Test
    @DisplayName("Should handle exception when recording field change")
    void shouldHandleException_WhenRecordingFieldChange() {
        // Arrange
        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenThrow(new RuntimeException("Database error"));
        when(securityUtils.getCurrentUsername()).thenReturn("testuser");

        // Act - Should not throw exception
        try {
            taskHistoryService.recordFieldChange(1L, "title", "Old", "New");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should not record history when oldTask is null")
    void shouldNotRecordHistory_WhenOldTaskIsNull() {
        // Arrange
        Task newTask = createTask(1L, "New Title", "New Description", Task.TaskStatus.PENDING, 1);

        // Act
        taskHistoryService.recordTaskChanges(1L, null, newTask);

        // Assert
        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should not record history when newTask is null")
    void shouldNotRecordHistory_WhenNewTaskIsNull() {
        // Arrange
        Task oldTask = createTask(1L, "Old Title", "Old Description", Task.TaskStatus.PENDING, 1);

        // Act
        taskHistoryService.recordTaskChanges(1L, oldTask, null);

        // Assert
        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record status change when status is null in old task")
    void shouldRecordStatusChange_WhenStatusIsNullInOldTask() {
        // Arrange
        Task oldTask = createTask(1L, "Title", "Description", null, 1);
        Task newTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        // Assert
        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record priority change when priority is null")
    void shouldRecordPriorityChange_WhenPriorityIsNull() {
        // Arrange
        Task oldTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, null);
        Task newTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        // Assert
        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record field change with null values")
    void shouldRecordFieldChange_WithNullValues() {
        // Arrange
        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        taskHistoryService.recordFieldChange(1L, "title", null, "New Title");
        taskHistoryService.recordFieldChange(1L, "description", "Old Description", null);

        // Assert
        verify(taskHistoryRepository, times(2)).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should handle equalsOrNull when both values are null")
    void shouldHandleEqualsOrNull_WhenBothValuesAreNull() {
        Task oldTask = createTask(1L, "Title", "Description", null, null);
        Task newTask = createTask(1L, "Title", "Description", null, null);

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should handle equalsOrNull when first value is null and second is not")
    void shouldHandleEqualsOrNull_WhenFirstValueIsNull() {
        Task oldTask = createTask(1L, "Title", "Description", null, null);
        Task newTask = createTask(1L, "Title", "Description", null, 1);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should handle equalsOrNull when second value is null and first is not")
    void shouldHandleEqualsOrNull_WhenSecondValueIsNull() {
        Task oldTask = createTask(1L, "Title", "Description", null, 1);
        Task newTask = createTask(1L, "Title", "Description", null, null);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should handle equalsOrNull when both values are equal and not null")
    void shouldHandleEqualsOrNull_WhenBothValuesAreEqual() {
        Task oldTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);
        Task newTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should handle equalsOrNull when both values are different and not null")
    void shouldHandleEqualsOrNull_WhenBothValuesAreDifferent() {
        Task oldTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);
        Task newTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 2);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record status change when old status is null and new status is not null")
    void shouldRecordStatusChange_WhenOldStatusIsNullAndNewStatusIsNotNull() {
        Task oldTask = createTask(1L, "Title", "Description", null, 1);
        Task newTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record status change when new status is null and old status is not null")
    void shouldRecordStatusChange_WhenNewStatusIsNullAndOldStatusIsNotNull() {
        Task oldTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);
        Task newTask = createTask(1L, "Title", "Description", null, 1);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record priority change when old priority is null and new priority is not null")
    void shouldRecordPriorityChange_WhenOldPriorityIsNullAndNewPriorityIsNotNull() {
        Task oldTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, null);
        Task newTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 2);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }

    @Test
    @DisplayName("Should record priority change when new priority is null and old priority is not null")
    void shouldRecordPriorityChange_WhenNewPriorityIsNullAndOldPriorityIsNotNull() {
        Task oldTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, 1);
        Task newTask = createTask(1L, "Title", "Description", Task.TaskStatus.PENDING, null);

        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);

        verify(taskHistoryRepository, atLeastOnce()).save(any(TaskHistory.class));
    }
    
    /**
     * Helper method to create Task test objects.
     */
    private Task createTask(Long id, String title, String description, Task.TaskStatus status, Integer priority) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        return task;
    }
}
