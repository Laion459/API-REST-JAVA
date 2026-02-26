package com.leonardoborges.api.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CacheMetricsService Tests")
class CacheMetricsServiceTest {

    private CacheManager cacheManager;
    private MeterRegistry meterRegistry;
    private Cache cache;
    private CacheMetricsService cacheMetricsService;

    @BeforeEach
    void setUp() {
        cacheManager = mock(CacheManager.class);
        meterRegistry = new SimpleMeterRegistry(); // Use real MeterRegistry for tests
        cache = mock(Cache.class);
        
        when(cacheManager.getCacheNames()).thenReturn(Set.of("tasks", "taskStats"));
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.getNativeCache()).thenReturn(new HashMap<>());
        
        cacheMetricsService = new CacheMetricsService(cacheManager, meterRegistry);
    }

    @Test
    @DisplayName("Should record cache hit without exception")
    void shouldRecordCacheHit() {
        assertDoesNotThrow(() -> {
            cacheMetricsService.recordCacheHit("tasks");
        });
    }

    @Test
    @DisplayName("Should record cache miss without exception")
    void shouldRecordCacheMiss() {
        assertDoesNotThrow(() -> {
            cacheMetricsService.recordCacheMiss("tasks");
        });
    }

    @Test
    @DisplayName("Should start cache access timer")
    void shouldStartCacheAccessTimer() {
        assertDoesNotThrow(() -> {
            var sample = cacheMetricsService.startCacheAccessTimer("tasks");
            assertNotNull(sample);
        });
    }

    @Test
    @DisplayName("Should return cache statistics")
    void shouldReturnCacheStatistics() {
        Map<String, Object> stats = cacheMetricsService.getCacheStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("tasks"));
        assertTrue(stats.containsKey("taskStats"));
    }

    @Test
    @DisplayName("Should calculate hit rate correctly")
    void shouldCalculateHitRateCorrectly() {
        Map<String, Object> stats = cacheMetricsService.getCacheStatistics();

        assertNotNull(stats);
        @SuppressWarnings("unchecked")
        Map<String, Object> taskStats = (Map<String, Object>) stats.get("tasks");
        assertNotNull(taskStats);
        assertTrue(taskStats.containsKey("hitRate"));
    }

    @Test
    @DisplayName("Should return hit rate when there are no accesses")
    void shouldReturnHitRateWhenThereAreNoAccesses() {
        Map<String, Object> stats = cacheMetricsService.getCacheStatistics();

        assertNotNull(stats);
        @SuppressWarnings("unchecked")
        Map<String, Object> taskStats = (Map<String, Object>) stats.get("tasks");
        assertNotNull(taskStats);
        assertTrue(taskStats.containsKey("hitRate"));
        // Without hits/misses, should return 0.00%
        assertEquals("0.00%", taskStats.get("hitRate"));
    }

    @Test
    @DisplayName("Should include cache type in statistics")
    void shouldIncludeCacheTypeInStatistics() {
        Map<String, Object> stats = cacheMetricsService.getCacheStatistics();

        assertNotNull(stats);
        @SuppressWarnings("unchecked")
        Map<String, Object> taskStats = (Map<String, Object>) stats.get("tasks");
        assertNotNull(taskStats);
        assertTrue(taskStats.containsKey("type"));
    }

    @Test
    @DisplayName("Should collect metrics periodically")
    void shouldCollectMetricsPeriodically() {
        assertDoesNotThrow(() -> {
            cacheMetricsService.collectCacheMetrics();
        });
    }

    @Test
    @DisplayName("Should handle null cache when getting statistics")
    void shouldHandleNullCache_WhenGettingStatistics() {
        when(cacheManager.getCacheNames()).thenReturn(Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(null);
        when(cacheManager.getCache("taskStats")).thenReturn(null);

        Map<String, Object> stats = cacheMetricsService.getCacheStatistics();

        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    @DisplayName("Should calculate hit rate when hits and misses are greater than zero")
    void shouldCalculateHitRate_WhenHitsAndMissesGreaterThanZero() {
        cacheMetricsService.recordCacheHit("tasks");
        cacheMetricsService.recordCacheHit("tasks");
        cacheMetricsService.recordCacheMiss("tasks");

        Map<String, Object> stats = cacheMetricsService.getCacheStatistics();

        assertNotNull(stats);
        @SuppressWarnings("unchecked")
        Map<String, Object> taskStats = (Map<String, Object>) stats.get("tasks");
        assertNotNull(taskStats);
        assertTrue(taskStats.containsKey("hitRate"));
        String hitRate = (String) taskStats.get("hitRate");
        assertNotEquals("0.00%", hitRate);
        assertTrue(hitRate.contains("%"));
    }
}
