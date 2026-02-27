package com.leonardoborges.api.repository;

import com.leonardoborges.api.dto.TaskFilterRequest;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskRepositoryImpl Tests")
class TaskRepositoryImplTest {
    
    @Mock
    private EntityManager entityManager;
    
    @InjectMocks
    private TaskRepositoryImpl repository;
    
    private User testUser;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        pageable = PageRequest.of(0, 20);
    }
    
    @Test
    void shouldHandleNullFilters() {
        Page<Task> result = repository.findTasksWithFilters(testUser, null, pageable);
        
        assertNotNull(result);
        verify(entityManager, atLeastOnce()).createQuery(any());
    }
    
    @Test
    void shouldHandleEmptyFilters() {
        TaskFilterRequest filters = TaskFilterRequest.builder().build();
        
        Page<Task> result = repository.findTasksWithFilters(testUser, filters, pageable);
        
        assertNotNull(result);
    }
    
    @Test
    void shouldHandleStatusFilter() {
        TaskFilterRequest filters = TaskFilterRequest.builder()
                .status(Task.TaskStatus.PENDING)
                .build();
        
        Page<Task> result = repository.findTasksWithFilters(testUser, filters, pageable);
        
        assertNotNull(result);
    }
    
    @Test
    void shouldHandlePriorityRangeFilter() {
        TaskFilterRequest filters = TaskFilterRequest.builder()
                .minPriority(5)
                .maxPriority(10)
                .build();
        
        Page<Task> result = repository.findTasksWithFilters(testUser, filters, pageable);
        
        assertNotNull(result);
    }
    
    @Test
    void shouldHandleTextSearchFilters() {
        TaskFilterRequest filters = TaskFilterRequest.builder()
                .titleContains("test")
                .descriptionContains("feature")
                .build();
        
        Page<Task> result = repository.findTasksWithFilters(testUser, filters, pageable);
        
        assertNotNull(result);
    }
    
    @Test
    void shouldHandleDateRangeFilters() {
        TaskFilterRequest filters = TaskFilterRequest.builder()
                .createdAfter(java.time.LocalDateTime.now().minusDays(7))
                .createdBefore(java.time.LocalDateTime.now())
                .build();
        
        Page<Task> result = repository.findTasksWithFilters(testUser, filters, pageable);
        
        assertNotNull(result);
    }
}
