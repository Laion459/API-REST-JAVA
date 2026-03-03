package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Ultra-granular cache eviction strategy.
 * Evicts only the specific cache entries affected by the operation.
 * More efficient than clearing entire cache regions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GranularCacheEvictionStrategy {
    
    private final CacheManager cacheManager;
    
    /**
     * Evicts only the specific task cache entry.
     * More granular than evicting entire cache region.
     */
    public void evictTaskCache(Long taskId) {
        var cache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASKS);
        if (cache != null) {
            cache.evict("tasks::" + taskId);
            log.debug("Evicted specific task cache entry: {}", taskId);
        }
    }
    
    /**
     * Evicts only the specific status-based cache entries.
     */
    public void evictStatusCache(Task.TaskStatus status) {
        var cache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASK_LISTS);
        if (cache != null) {
            cache.evict("taskLists::status-" + status);
            log.debug("Evicted status cache entry: {}", status);
        }
    }
    
    /**
     * Evicts only the specific user's task list cache.
     */
    public void evictUserTaskListCache(Long userId) {
        var cache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASK_LISTS);
        if (cache != null) {
            cache.evict("taskLists::user-" + userId);
            log.debug("Evicted user task list cache: {}", userId);
        }
    }
    
    /**
     * Evicts only the specific stats cache entry.
     */
    public void evictStatsCache(Long userId) {
        var cache = cacheManager.getCache(TaskConstants.CACHE_NAME_TASK_STATS);
        if (cache != null) {
            cache.evict("taskStats::user-" + userId);
            log.debug("Evicted stats cache entry: {}", userId);
        }
    }
    
    /**
     * Evicts all caches related to a specific task operation.
     * Ultra-granular: only affects related entries.
     */
    public void evictRelatedCaches(Long taskId, Long userId, Task.TaskStatus status) {
        evictTaskCache(taskId);
        evictStatusCache(status);
        evictUserTaskListCache(userId);
        evictStatsCache(userId);
    }
}
