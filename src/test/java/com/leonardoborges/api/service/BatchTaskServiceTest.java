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
}
