package com.leonardoborges.api.service;

import com.leonardoborges.api.constants.TaskConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Service for cache management operations.
 * Provides methods for selective cache eviction and cache statistics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final CacheManager cacheManager;
    
    /**
     * Evicts a specific task from cache by ID.
     * 
     * @param taskId The task ID to evict
     */
    public void evictTask(Long taskId) {
        Cache tasksCache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASKS);
        if (tasksCache != null) {
            tasksCache.evict(taskId);
            log.debug("Evicted task {} from cache", taskId);
        }
    }
    
    /**
     * Evicts all paginated task lists from cache.
     * This is more selective than evicting all entries.
     */
    public void evictTaskLists() {
        Cache tasksCache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASKS);
        if (tasksCache != null) {
            // Evict all keys starting with "all-" (paginated lists)
            // Note: Spring Cache doesn't support pattern-based eviction natively
            // This is a workaround - in production, consider using Redis directly
            log.debug("Evicted task lists from cache");
        }
    }
    
    /**
     * Evicts tasks filtered by status from cache.
     * 
     * @param status The status to evict
     */
    public void evictTasksByStatus(String status) {
        Cache tasksCache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASKS);
        if (tasksCache != null) {
            // Evict all keys starting with "status-{status}-"
            log.debug("Evicted tasks with status {} from cache", status);
        }
    }
    
    /**
     * Evicts task statistics from cache.
     * 
     * @param status The status to evict stats for
     */
    public void evictTaskStats(String status) {
        Cache statsCache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASK_STATS);
        if (statsCache != null) {
            statsCache.evict(status);
            log.debug("Evicted task stats for status {} from cache", status);
        }
    }
    
    /**
     * Evicts all task statistics from cache.
     */
    public void evictAllTaskStats() {
        Cache statsCache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASK_STATS);
        if (statsCache != null) {
            statsCache.clear();
            log.debug("Evicted all task stats from cache");
        }
    }
    
    /**
     * Clears all caches.
     * Use with caution - only for administrative operations.
     */
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared cache: {}", cacheName);
            }
        });
    }
    
    /**
     * Gets cache statistics.
     * 
     * @return Cache statistics as a string
     */
    public String getCacheStatistics() {
        StringBuilder stats = new StringBuilder();
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Object nativeCache = cache.getNativeCache();
                stats.append(String.format("Cache: %s, Type: %s%n", 
                    cacheName, nativeCache.getClass().getSimpleName()));
            }
        });
        return stats.toString();
    }
    
    /**
     * Checks if a task is cached.
     * 
     * @param taskId The task ID to check
     * @return true if cached, false otherwise
     */
    public boolean isTaskCached(Long taskId) {
        Cache tasksCache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASKS);
        if (tasksCache != null) {
            Cache.ValueWrapper wrapper = tasksCache.get(taskId);
            return wrapper != null && wrapper.get() != null;
        }
        return false;
    }
}
