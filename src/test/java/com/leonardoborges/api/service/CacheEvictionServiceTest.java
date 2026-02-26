package com.leonardoborges.api.service;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.strategy.CreateCacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.DeleteCacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.UpdateCacheEvictionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CacheEvictionService using Strategy Pattern.
 * 
 * Uses @ExtendWith(MockitoExtension.class) for pure tests with mocks.
 * Does not load Spring context, making tests faster and isolated.
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Descriptive test names with @DisplayName
 * - Behavior verification (verify)
 * - Strategy Pattern tests
 */
@ExtendWith(MockitoExtension.class)
class CacheEvictionServiceTest {
    
    @Mock
    private CreateCacheEvictionStrategy createStrategy;
    
    @Mock
    private UpdateCacheEvictionStrategy updateStrategy;
    
    @Mock
    private DeleteCacheEvictionStrategy deleteStrategy;
    
    private CacheEvictionService cacheEvictionService;
    
    @BeforeEach
    void setUp() {
        cacheEvictionService = new CacheEvictionService(
                createStrategy,
                updateStrategy,
                deleteStrategy
        );
    }
    
    @Test
    @DisplayName("Should use create strategy for create operations")
    void shouldUseCreateStrategy_ForCreateOperations() {
        // Arrange
        Long taskId = 1L;
        Task.TaskStatus status = Task.TaskStatus.PENDING;
        
        // Act
        cacheEvictionService.evictAfterCreate(taskId, status);
        
        // Assert
        verify(createStrategy, times(1)).evict(taskId, null, status);
        verify(updateStrategy, never()).evict(anyLong(), any(), any());
        verify(deleteStrategy, never()).evict(anyLong(), any(), any());
    }
    
    @Test
    @DisplayName("Should use update strategy for update operations")
    void shouldUseUpdateStrategy_ForUpdateOperations() {
        // Arrange
        Long taskId = 1L;
        Task.TaskStatus oldStatus = Task.TaskStatus.PENDING;
        Task.TaskStatus newStatus = Task.TaskStatus.IN_PROGRESS;
        
        // Act
        cacheEvictionService.evictAfterUpdate(taskId, oldStatus, newStatus);
        
        // Assert
        verify(updateStrategy, times(1)).evict(taskId, oldStatus, newStatus);
        verify(createStrategy, never()).evict(anyLong(), any(), any());
        verify(deleteStrategy, never()).evict(anyLong(), any(), any());
    }
    
    @Test
    @DisplayName("Should use delete strategy for delete operations")
    void shouldUseDeleteStrategy_ForDeleteOperations() {
        // Arrange
        Long taskId = 1L;
        Task.TaskStatus status = Task.TaskStatus.COMPLETED;
        
        // Act
        cacheEvictionService.evictAfterDelete(taskId, status);
        
        // Assert
        verify(deleteStrategy, times(1)).evict(taskId, status, null);
        verify(createStrategy, never()).evict(anyLong(), any(), any());
        verify(updateStrategy, never()).evict(anyLong(), any(), any());
    }
}
