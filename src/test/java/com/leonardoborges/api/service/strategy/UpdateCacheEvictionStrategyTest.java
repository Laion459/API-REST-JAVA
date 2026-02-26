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
@DisplayName("UpdateCacheEvictionStrategy Tests")
class UpdateCacheEvictionStrategyTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UpdateCacheEvictionStrategy strategy;

    @Test
    @DisplayName("Should evict task, lists, and stats when updating task")
    void shouldEvictTaskListsAndStats_WhenUpdatingTask() {
        Long taskId = 1L;
        Task.TaskStatus oldStatus = Task.TaskStatus.PENDING;
        Task.TaskStatus newStatus = Task.TaskStatus.IN_PROGRESS;

        strategy.evict(taskId, oldStatus, newStatus);

        verify(cacheService, times(1)).evictTask(taskId);
        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, times(1)).evictTaskStats("PENDING");
        verify(cacheService, times(1)).evictTasksByStatus("PENDING");
        verify(cacheService, times(1)).evictTasksByStatus("IN_PROGRESS");
        verify(cacheService, times(1)).evictTaskStats("IN_PROGRESS");
    }

    @Test
    @DisplayName("Should evict task and lists when status does not change")
    void shouldEvictTaskAndLists_WhenStatusDoesNotChange() {
        Long taskId = 1L;
        Task.TaskStatus status = Task.TaskStatus.PENDING;

        strategy.evict(taskId, status, status);

        verify(cacheService, times(1)).evictTask(taskId);
        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, times(1)).evictTaskStats("PENDING");
        verify(cacheService, times(1)).evictTasksByStatus("PENDING");
        verify(cacheService, never()).evictTasksByStatus("IN_PROGRESS");
        verify(cacheService, never()).evictTaskStats("IN_PROGRESS");
    }

    @Test
    @DisplayName("Should handle null old status")
    void shouldHandleNullOldStatus() {
        Long taskId = 1L;
        Task.TaskStatus newStatus = Task.TaskStatus.PENDING;

        strategy.evict(taskId, null, newStatus);

        verify(cacheService, times(1)).evictTask(taskId);
        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, never()).evictTaskStats(anyString());
        verify(cacheService, never()).evictTasksByStatus(anyString());
    }

    @Test
    @DisplayName("Should handle null new status")
    void shouldHandleNullNewStatus() {
        Long taskId = 1L;
        Task.TaskStatus oldStatus = Task.TaskStatus.PENDING;

        strategy.evict(taskId, oldStatus, null);

        verify(cacheService, times(1)).evictTask(taskId);
        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, times(1)).evictTaskStats("PENDING");
        verify(cacheService, times(1)).evictTasksByStatus("PENDING");
        verify(cacheService, never()).evictTasksByStatus(eq("IN_PROGRESS"));
        verify(cacheService, never()).evictTaskStats(eq("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Should handle both statuses null")
    void shouldHandleBothStatusesNull() {
        Long taskId = 1L;

        strategy.evict(taskId, null, null);

        verify(cacheService, times(1)).evictTask(taskId);
        verify(cacheService, times(1)).evictTaskLists();
        verify(cacheService, never()).evictTaskStats(anyString());
        verify(cacheService, never()).evictTasksByStatus(anyString());
    }
}
