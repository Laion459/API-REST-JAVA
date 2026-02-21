package com.leonardoborges.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache tasksCache;

    @Mock
    private Cache statsCache;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        when(cacheManager.getCache("taskStats")).thenReturn(statsCache);
    }

    @Test
    void shouldEvictTask() {
        Long taskId = 1L;
        cacheService.evictTask(taskId);

        verify(tasksCache, times(1)).evict(taskId);
    }

    @Test
    void shouldEvictTaskStats() {
        String status = "PENDING";
        cacheService.evictTaskStats(status);

        verify(statsCache, times(1)).evict(status);
    }

    @Test
    void shouldEvictAllTaskStats() {
        cacheService.evictAllTaskStats();

        verify(statsCache, times(1)).clear();
    }

    @Test
    void shouldHandleNullCache() {
        when(cacheManager.getCache("tasks")).thenReturn(null);

        assertDoesNotThrow(() -> cacheService.evictTask(1L));
    }

    @Test
    void shouldCheckIfTaskIsCached() {
        Long taskId = 1L;
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        when(wrapper.get()).thenReturn(new Object());
        when(tasksCache.get(taskId)).thenReturn(wrapper);

        boolean result = cacheService.isTaskCached(taskId);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenTaskNotCached() {
        Long taskId = 1L;
        when(tasksCache.get(taskId)).thenReturn(null);

        boolean result = cacheService.isTaskCached(taskId);

        assertFalse(result);
    }

    @Test
    void shouldGetCacheStatistics() {
        when(cacheManager.getCacheNames()).thenReturn(java.util.Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        when(cacheManager.getCache("taskStats")).thenReturn(statsCache);
        when(tasksCache.getNativeCache()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());
        when(statsCache.getNativeCache()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());

        String stats = cacheService.getCacheStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("tasks"));
        assertTrue(stats.contains("taskStats"));
    }
}
