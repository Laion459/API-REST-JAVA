package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCacheEvictionStrategy Tests")
class CreateCacheEvictionStrategyTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CreateCacheEvictionStrategy strategy;

    @Test
    @DisplayName("Should evict task lists and stats when creating task with status")
    void shouldEvictTaskListsAndStats_WhenCreatingTaskWithStatus() {
        Long taskId = 1L;
        Task.TaskStatus status = Task.TaskStatus.PENDING;

        strategy.evict(taskId, null, status);

        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, times(1)).evictTaskStats("PENDING");
    }

    @Test
    @DisplayName("Should evict task lists only when creating task without status")
    void shouldEvictTaskListsOnly_WhenCreatingTaskWithoutStatus() {
        Long taskId = 1L;

        strategy.evict(taskId, null, null);

        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, never()).evictTaskStats(anyString());
    }

    @Test
    @DisplayName("Should evict stats for different statuses")
    void shouldEvictStats_ForDifferentStatuses() {
        Long taskId = 1L;

        strategy.evict(taskId, null, Task.TaskStatus.IN_PROGRESS);
        verify(cacheService, times(1)).evictTaskStats("IN_PROGRESS");

        strategy.evict(taskId, null, Task.TaskStatus.COMPLETED);
        verify(cacheService, times(1)).evictTaskStats("COMPLETED");

        strategy.evict(taskId, null, Task.TaskStatus.CANCELLED);
        verify(cacheService, times(1)).evictTaskStats("CANCELLED");
    }
}
