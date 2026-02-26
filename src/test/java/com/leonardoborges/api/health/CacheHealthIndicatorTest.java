package com.leonardoborges.api.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheHealthIndicator Tests")
class CacheHealthIndicatorTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache1;

    @Mock
    private Cache cache2;

    @InjectMocks
    private CacheHealthIndicator cacheHealthIndicator;

    @BeforeEach
    void setUp() {
        lenient().when(cache1.getNativeCache()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());
        lenient().when(cache2.getNativeCache()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());
    }

    @Test
    @DisplayName("Should return UP when caches are available")
    void shouldReturnUp_WhenCachesAreAvailable() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(cache1);
        when(cacheManager.getCache("taskStats")).thenReturn(cache2);

        // Act
        Health health = cacheHealthIndicator.health();

        // Assert
        assertEquals(Health.status("UP").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("cache"));
        assertTrue(health.getDetails().containsKey("totalCaches"));
    }

    @Test
    @DisplayName("Should return DOWN when no caches configured")
    void shouldReturnDown_WhenNoCachesConfigured() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(Set.of());

        // Act
        Health health = cacheHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("cache"));
    }

    @Test
    @DisplayName("Should return UP when cache is null but cache name exists")
    void shouldReturnUp_WhenCacheIsNullButCacheNameExists() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(Set.of("tasks"));
        when(cacheManager.getCache("tasks")).thenReturn(null);

        // Act
        Health health = cacheHealthIndicator.health();

        // Assert
        assertEquals(Health.status("UP").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("caches"));
    }

    @Test
    @DisplayName("Should return DOWN when exception occurs")
    void shouldReturnDown_WhenExceptionOccurs() {
        // Arrange
        when(cacheManager.getCacheNames()).thenThrow(new RuntimeException("Cache error"));

        // Act
        Health health = cacheHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("Should include cache details in health response")
    void shouldIncludeCacheDetails_InHealthResponse() {
        // Arrange
        when(cacheManager.getCacheNames()).thenReturn(Set.of("tasks", "taskStats"));
        when(cacheManager.getCache("tasks")).thenReturn(cache1);
        when(cacheManager.getCache("taskStats")).thenReturn(cache2);

        // Act
        Health health = cacheHealthIndicator.health();

        // Assert
        assertTrue(health.getDetails().containsKey("caches"));
        assertTrue(health.getDetails().containsKey("totalCaches"));
        assertEquals(2, health.getDetails().get("totalCaches"));
    }
}
