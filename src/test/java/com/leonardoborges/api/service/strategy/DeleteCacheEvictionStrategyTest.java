package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.CacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCacheEvictionStrategy Tests")
class DeleteCacheEvictionStrategyTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private DeleteCacheEvictionStrategy strategy;

    @Test
    @DisplayName("Should evict task, lists, and stats when deleting task with status")
    void shouldEvictTaskListsAndStats_WhenDeletingTaskWithStatus() {
        Long taskId = 1L;
        Task.TaskStatus status = Task.TaskStatus.PENDING;

        strategy.evict(taskId, status, null);

        verify(cacheService, times(1)).evictTask(taskId);
        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, times(1)).evictTasksByStatus("PENDING");
        verify(cacheService, times(1)).evictTaskStats("PENDING");
    }

    @Test
    @DisplayName("Should evict task and lists only when deleting task without status")
    void shouldEvictTaskAndListsOnly_WhenDeletingTaskWithoutStatus() {
        Long taskId = 1L;

        strategy.evict(taskId, null, null);

        verify(cacheService, times(1)).evictTask(taskId);
        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, never()).evictTasksByStatus(anyString());
        verify(cacheService, never()).evictTaskStats(anyString());
    }

    @Test
    @DisplayName("Should evict stats for different statuses")
    void shouldEvictStats_ForDifferentStatuses() {
        Long taskId = 1L;

        strategy.evict(taskId, Task.TaskStatus.IN_PROGRESS, null);
        verify(cacheService, times(1)).evictTaskStats("IN_PROGRESS");
        verify(cacheService, times(1)).evictTasksByStatus("IN_PROGRESS");

        strategy.evict(taskId, Task.TaskStatus.COMPLETED, null);
        verify(cacheService, times(1)).evictTaskStats("COMPLETED");
        verify(cacheService, times(1)).evictTasksByStatus("COMPLETED");

        strategy.evict(taskId, Task.TaskStatus.CANCELLED, null);
        verify(cacheService, times(1)).evictTaskStats("CANCELLED");
        verify(cacheService, times(1)).evictTasksByStatus("CANCELLED");
    }
}
