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
 * Testes unitários para CacheService.
 * 
 * Usa @ExtendWith(MockitoExtension.class) para testes puros com mocks.
 * Não carrega contexto Spring, tornando os testes mais rápidos e isolados.
 * 
 * Boas práticas aplicadas:
 * - Testes isolados e independentes
 * - Nomes descritivos de testes com @DisplayName
 * - Verificação de comportamento (verify)
 * - Testes de casos de sucesso e erro (null cache)
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
    @DisplayName("Deve invalidar cache de tarefa individual")
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
    @DisplayName("Deve invalidar cache de estatísticas por status")
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
    @DisplayName("Deve limpar todas as estatísticas de cache")
    void shouldEvictAllTaskStats_WhenCalled() {
        // Arrange
        when(cacheManager.getCache("taskStats")).thenReturn(statsCache);
        
        // Act
        cacheService.evictAllTaskStats();

        // Assert
        verify(statsCache, times(1)).clear();
    }

    @Test
    @DisplayName("Deve lidar com cache nulo sem lançar exceção")
    void shouldHandleNullCache_WithoutThrowingException() {
        // Arrange
        when(cacheManager.getCache("tasks")).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> cacheService.evictTask(1L));
    }

    @Test
    @DisplayName("Deve verificar se tarefa está em cache")
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
    @DisplayName("Deve retornar false quando tarefa não está em cache")
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
    @DisplayName("Deve retornar estatísticas de cache")
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
}
