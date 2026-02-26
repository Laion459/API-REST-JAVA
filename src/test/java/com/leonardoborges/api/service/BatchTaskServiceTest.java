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
import com.leonardoborges.api.util.TestBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchTaskService.
 * 
 * Uses @ExtendWith(MockitoExtension.class) for pure tests with mocks.
 * Does not load Spring context, making tests faster and isolated.
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Use of builders to create test objects
 * - Descriptive test names with @DisplayName
 * - Behavior verification (verify)
 * - Tests for success and error cases
 */
@ExtendWith(MockitoExtension.class)
class BatchTaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @Mock
    private TaskValidationService taskValidationService;
    
    @Mock
    private CacheEvictionService cacheEvictionService;
    
    @Mock
    private SecurityUtils securityUtils;
    
    @Mock
    private AuditService auditService;
    
    private BatchTaskService batchTaskService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = TestBuilders.buildDefaultUser();
        
        lenient().when(securityUtils.getCurrentUser()).thenReturn(testUser);
        lenient().doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        lenient().doNothing().when(auditService).audit(anyString(), anyString(), anyLong(), anyString());
        
        batchTaskService = new BatchTaskService(
                taskRepository,
                taskMapper,
                taskValidationService,
                cacheEvictionService,
                securityUtils,
                auditService
        );
    }
    
    @Test
    @DisplayName("Should create multiple tasks in batch successfully")
    void shouldCreateBatchTasks_WhenValidRequests() {
        // Arrange
        List<TaskRequest> requests = Arrays.asList(
                TestBuilders.defaultTaskRequest().title("Task 1").build(),
                TestBuilders.defaultTaskRequest().title("Task 2").build()
        );
        
        Task task1 = TestBuilders.defaultTask().id(1L).title("Task 1").build();
        Task task2 = TestBuilders.defaultTask().id(2L).title("Task 2").build();
        
        when(taskMapper.toEntity(any(TaskRequest.class))).thenAnswer(invocation -> {
            TaskRequest req = invocation.getArgument(0);
            return TestBuilders.defaultTask().title(req.getTitle()).build();
        });
        when(taskRepository.saveAll(anyList())).thenReturn(Arrays.asList(task1, task2));
        when(taskMapper.toResponse(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            return TestBuilders.defaultTaskResponse()
                    .id(t.getId())
                    .title(t.getTitle())
                    .build();
        });
        
        // Act
        List<TaskResponse> responses = batchTaskService.createBatch(requests);
        
        // Assert
        assertEquals(2, responses.size());
        verify(taskRepository, times(1)).saveAll(anyList());
        verify(cacheEvictionService, times(2)).evictAfterCreate(anyLong(), any());
    }
    
    @Test
    @DisplayName("Should reject empty or null batch")
    void shouldRejectEmptyBatch_WhenNullOrEmpty() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            batchTaskService.createBatch(null);
        });
        
        assertThrows(BusinessException.class, () -> {
            batchTaskService.createBatch(List.of());
        });
    }
    
    @Test
    @DisplayName("Should reject batch that is too large")
    void shouldRejectBatchTooLarge_WhenExceedsLimit() {
        // Arrange
        List<TaskRequest> largeBatch = Arrays.asList(new TaskRequest[101]);
        Arrays.fill(largeBatch.toArray(), TestBuilders.buildDefaultTaskRequest());
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            batchTaskService.createBatch(largeBatch);
        });
    }
    
    @Test
    @DisplayName("Should update multiple tasks in batch successfully")
    void shouldUpdateBatchTasks_WhenValidUpdates() {
        // Arrange
        Map<Long, TaskRequest> updates = new HashMap<>();
        updates.put(1L, TestBuilders.defaultTaskRequest().title("Updated Task 1").build());
        updates.put(2L, TestBuilders.defaultTaskRequest().title("Updated Task 2").build());
        
        Task task1 = TestBuilders.defaultTask().id(1L).title("Task 1").build();
        Task task2 = TestBuilders.defaultTask().id(2L).title("Task 2").build();
        task1.setUser(testUser);
        task2.setUser(testUser);
        
        when(taskRepository.findAllById(argThat(list -> {
            java.util.List<Long> ids = new java.util.ArrayList<>();
            list.forEach(ids::add);
            return ids.contains(1L) && ids.contains(2L);
        }))).thenReturn(Arrays.asList(task1, task2));
        when(taskRepository.saveAll(anyList())).thenReturn(Arrays.asList(task1, task2));
        when(taskMapper.toResponse(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            return TestBuilders.defaultTaskResponse()
                    .id(t.getId())
                    .title(t.getTitle())
                    .build();
        });
        lenient().doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        
        // Act
        List<TaskResponse> responses = batchTaskService.updateBatch(updates);
        
        // Assert
        assertEquals(2, responses.size());
        verify(taskRepository, times(1)).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Should delete multiple tasks in batch successfully")
    void shouldDeleteBatchTasks_WhenValidIds() {
        // Arrange
        List<Long> taskIds = Arrays.asList(1L, 2L);
        
        Task task1 = TestBuilders.defaultTask().id(1L).title("Task 1").build();
        Task task2 = TestBuilders.defaultTask().id(2L).title("Task 2").build();
        task1.setUser(testUser);
        task2.setUser(testUser);
        
        when(taskRepository.findAllById(taskIds)).thenReturn(Arrays.asList(task1, task2));
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        batchTaskService.deleteBatch(taskIds);
        
        // Assert
        verify(taskRepository, times(1)).saveAll(anyList());
        verify(cacheEvictionService, times(2)).evictAfterDelete(anyLong(), any());
        assertTrue(task1.getDeleted());
        assertTrue(task2.getDeleted());
    }

    @Test
    @DisplayName("Should reject empty batch update")
    void shouldRejectEmptyBatchUpdate_WhenEmpty() {
        assertThrows(BusinessException.class, () -> {
            batchTaskService.updateBatch(new HashMap<>());
        });
    }

    @Test
    @DisplayName("Should reject batch update that is too large")
    void shouldRejectBatchUpdateTooLarge_WhenExceedsLimit() {
        Map<Long, TaskRequest> largeBatch = new HashMap<>();
        for (int i = 0; i < 101; i++) {
            largeBatch.put((long) i, TestBuilders.buildDefaultTaskRequest());
        }

        assertThrows(BusinessException.class, () -> {
            batchTaskService.updateBatch(largeBatch);
        });
    }

    @Test
    @DisplayName("Should reject batch update when tasks not found")
    void shouldRejectBatchUpdate_WhenTasksNotFound() {
        Map<Long, TaskRequest> updates = new HashMap<>();
        updates.put(1L, TestBuilders.buildDefaultTaskRequest());

        when(taskRepository.findAllById(argThat(list -> {
            java.util.List<Long> ids = new java.util.ArrayList<>();
            list.forEach(ids::add);
            return ids.contains(1L);
        }))).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> {
            batchTaskService.updateBatch(updates);
        });
    }

    @Test
    @DisplayName("Should reject batch update when task belongs to different user")
    void shouldRejectBatchUpdate_WhenTaskBelongsToDifferentUser() {
        Map<Long, TaskRequest> updates = new HashMap<>();
        updates.put(1L, TestBuilders.buildDefaultTaskRequest());

        User otherUser = TestBuilders.buildDefaultUser();
        otherUser.setId(999L);
        Task task = TestBuilders.defaultTask().id(1L).build();
        task.setUser(otherUser);

        when(taskRepository.findAllById(argThat(list -> {
            java.util.List<Long> ids = new java.util.ArrayList<>();
            list.forEach(ids::add);
            return ids.contains(1L);
        }))).thenReturn(List.of(task));

        assertThrows(BusinessException.class, () -> {
            batchTaskService.updateBatch(updates);
        });
    }

    @Test
    @DisplayName("Should reject empty batch delete")
    void shouldRejectEmptyBatchDelete_WhenEmpty() {
        assertThrows(BusinessException.class, () -> {
            batchTaskService.deleteBatch(List.of());
        });
    }

    @Test
    @DisplayName("Should reject batch delete that is too large")
    void shouldRejectBatchDeleteTooLarge_WhenExceedsLimit() {
        List<Long> largeBatch = new java.util.ArrayList<>();
        for (long i = 0; i < 101; i++) {
            largeBatch.add(i);
        }

        assertThrows(BusinessException.class, () -> {
            batchTaskService.deleteBatch(largeBatch);
        });
    }

    @Test
    @DisplayName("Should reject batch delete when tasks not found")
    void shouldRejectBatchDelete_WhenTasksNotFound() {
        List<Long> taskIds = Arrays.asList(1L, 2L);

        when(taskRepository.findAllById(taskIds)).thenReturn(List.of());
        lenient().when(securityUtils.getCurrentUser()).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> {
            batchTaskService.deleteBatch(taskIds);
        });
    }

    @Test
    @DisplayName("Should reject batch delete when task belongs to different user")
    void shouldRejectBatchDelete_WhenTaskBelongsToDifferentUser() {
        List<Long> taskIds = Arrays.asList(1L);

        User otherUser = TestBuilders.buildDefaultUser();
        otherUser.setId(999L);
        Task task = TestBuilders.defaultTask().id(1L).build();
        task.setUser(otherUser);

        when(taskRepository.findAllById(taskIds)).thenReturn(List.of(task));
        lenient().when(securityUtils.getCurrentUser()).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> {
            batchTaskService.deleteBatch(taskIds);
        });
    }

    @Test
    @DisplayName("Should reject batch delete when task is already deleted")
    void shouldRejectBatchDelete_WhenTaskAlreadyDeleted() {
        List<Long> taskIds = Arrays.asList(1L);

        Task task = TestBuilders.defaultTask().id(1L).build();
        task.setUser(testUser);
        task.setDeleted(true);

        when(taskRepository.findAllById(taskIds)).thenReturn(List.of(task));
        lenient().when(securityUtils.getCurrentUser()).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> {
            batchTaskService.deleteBatch(taskIds);
        });
    }

    @Test
    @DisplayName("Should handle batch update when request is null for a task")
    void shouldHandleBatchUpdate_WhenRequestIsNullForTask() {
        Map<Long, TaskRequest> updates = new HashMap<>();
        updates.put(1L, TestBuilders.buildDefaultTaskRequest());
        updates.put(2L, null);

        Task task1 = TestBuilders.defaultTask().id(1L).build();
        task1.setUser(testUser);
        Task task2 = TestBuilders.defaultTask().id(2L).build();
        task2.setUser(testUser);

        when(taskRepository.findAllById(anyIterable())).thenReturn(List.of(task1, task2));
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskMapper.toResponse(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            return TestBuilders.defaultTaskResponse().id(t.getId()).build();
        });
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        doNothing().when(cacheEvictionService).evictAfterUpdate(anyLong(), any(), any());
        doNothing().when(auditService).audit(anyString(), anyString(), anyLong(), anyString());

        List<TaskResponse> responses = batchTaskService.updateBatch(updates);

        assertEquals(2, responses.size());
        verify(taskValidationService, times(1)).validateAndSanitizeTaskRequest(any(TaskRequest.class));
    }
}
