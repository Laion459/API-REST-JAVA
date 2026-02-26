package com.leonardoborges.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CacheService.
 * 
 * Uses @ExtendWith(MockitoExtension.class) for pure tests with mocks.
 * Does not load Spring context, making tests faster and isolated.
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Descriptive test names with @DisplayName
 * - Behavior verification (verify)
 * - Tests for success and error cases (null cache)
 */
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
        // Setup will be done per test as needed
    }

    @Test
    @DisplayName("Should evict individual task cache")
    void shouldEvictTask_WhenTaskIdProvided() {
        // Arrange
        Long taskId = 1L;
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        
        // Act
        cacheService.evictTask(taskId);

        // Assert
        verify(tasksCache, times(1)).evict(taskId);
    }

    @Test
    @DisplayName("Should evict statistics cache by status")
    void shouldEvictTaskStats_WhenStatusProvided() {
        // Arrange
        String status = "PENDING";
        when(cacheManager.getCache("taskStats")).thenReturn(statsCache);
        
        // Act
        cacheService.evictTaskStats(status);

        // Assert
        verify(statsCache, times(1)).evict(status);
    }

    @Test
    @DisplayName("Should clear all statistics cache")
    void shouldEvictAllTaskStats_WhenCalled() {
        // Arrange
        when(cacheManager.getCache("taskStats")).thenReturn(statsCache);
        
        // Act
        cacheService.evictAllTaskStats();

        // Assert
        verify(statsCache, times(1)).clear();
    }

    @Test
    @DisplayName("Should handle null cache without throwing exception")
    void shouldHandleNullCache_WithoutThrowingException() {
        // Arrange
        when(cacheManager.getCache("tasks")).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> cacheService.evictTask(1L));
    }

    @Test
    @DisplayName("Should check if task is cached")
    void shouldCheckIfTaskIsCached_WhenTaskExists() {
        // Arrange
        Long taskId = 1L;
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        when(wrapper.get()).thenReturn(new Object());
        when(tasksCache.get(taskId)).thenReturn(wrapper);

        // Act
        boolean result = cacheService.isTaskCached(taskId);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when task is not cached")
    void shouldReturnFalse_WhenTaskNotCached() {
        // Arrange
        Long taskId = 1L;
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        when(tasksCache.get(taskId)).thenReturn(null);

        // Act
        boolean result = cacheService.isTaskCached(taskId);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return cache statistics")
    void shouldGetCacheStatistics_WhenCachesExist() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(java.util.Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        when(cacheManager.getCache("taskStats")).thenReturn(statsCache);
        when(tasksCache.getNativeCache()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());
        when(statsCache.getNativeCache()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());

        // Act
        String stats = cacheService.getCacheStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.contains("tasks"));
        assertTrue(stats.contains("taskStats"));
    }

    @Test
    @DisplayName("Should handle null cache when getting statistics")
    void shouldHandleNullCache_WhenGettingStatistics() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(java.util.Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(null);
        when(cacheManager.getCache("taskStats")).thenReturn(null);

        // Act
        String stats = cacheService.getCacheStatistics();

        // Assert
        assertNotNull(stats);
    }

    @Test
    @DisplayName("Should evict all task lists from cache")
    void shouldEvictAllTaskLists_WhenCalled() {
        // Arrange
        Cache taskListsCache = mock(Cache.class);
        when(cacheManager.getCache("taskLists")).thenReturn(taskListsCache);

        // Act
        cacheService.evictTaskLists();

        // Assert
        verify(taskListsCache, times(1)).clear();
    }

    @Test
    @DisplayName("Should handle null cache when evicting task lists")
    void shouldHandleNullCache_WhenEvictingTaskLists() {
        // Arrange
        when(cacheManager.getCache("taskLists")).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> cacheService.evictTaskLists());
    }

    @Test
    @DisplayName("Should evict tasks by status from cache")
    void shouldEvictTasksByStatus_WhenStatusProvided() {
        // Arrange
        Cache taskListsCache = mock(Cache.class);
        String status = "PENDING";
        when(cacheManager.getCache("taskLists")).thenReturn(taskListsCache);

        // Act
        cacheService.evictTasksByStatus(status);

        // Assert
        verify(taskListsCache, times(1)).clear();
    }

    @Test
    @DisplayName("Should handle null cache when evicting tasks by status")
    void shouldHandleNullCache_WhenEvictingTasksByStatus() {
        // Arrange
        when(cacheManager.getCache("taskLists")).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> cacheService.evictTasksByStatus("PENDING"));
    }

    @Test
    @DisplayName("Should clear all caches")
    void shouldClearAllCaches_WhenCalled() {
        // Arrange
        Cache cache1 = mock(Cache.class);
        Cache cache2 = mock(Cache.class);
        when(cacheManager.getCacheNames()).thenReturn(java.util.Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(cache1);
        when(cacheManager.getCache("taskStats")).thenReturn(cache2);

        // Act
        cacheService.clearAllCaches();

        // Assert
        verify(cache1, times(1)).clear();
        verify(cache2, times(1)).clear();
    }

    @Test
    @DisplayName("Should handle null cache when clearing all caches")
    void shouldHandleNullCache_WhenClearingAllCaches() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(java.util.Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(null);
        when(cacheManager.getCache("taskStats")).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> cacheService.clearAllCaches());
    }

    @Test
    @DisplayName("Should return false when cache is null for isTaskCached")
    void shouldReturnFalse_WhenCacheIsNullForIsTaskCached() {
        // Arrange
        when(cacheManager.getCache("tasks")).thenReturn(null);

        // Act
        boolean result = cacheService.isTaskCached(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when value wrapper is null")
    void shouldReturnFalse_WhenValueWrapperIsNull() {
        // Arrange
        Long taskId = 1L;
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        when(tasksCache.get(taskId)).thenReturn(null);

        // Act
        boolean result = cacheService.isTaskCached(taskId);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when value wrapper contains null")
    void shouldReturnFalse_WhenValueWrapperContainsNull() {
        // Arrange
        Long taskId = 1L;
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        when(wrapper.get()).thenReturn(null);
        when(cacheManager.getCache("tasks")).thenReturn(tasksCache);
        when(tasksCache.get(taskId)).thenReturn(wrapper);

        // Act
        boolean result = cacheService.isTaskCached(taskId);

        // Assert
        assertFalse(result);
    }
}
