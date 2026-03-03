package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.CacheService;
import com.leonardoborges.api.service.strategy.GranularCacheEvictionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cache eviction strategy for task updates.
 * Invalidates individual task, lists, and affected statistics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateCacheEvictionStrategy implements CacheEvictionStrategy {
    
    private final CacheService cacheService;
    private final GranularCacheEvictionStrategy granularStrategy;
    
    @Override
    public void evict(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus) {
        log.debug("Executing update cache eviction strategy for task: {}", taskId);
        
        // Use granular eviction for better performance
        // Granular strategy evicts only specific entries
        cacheService.evictTask(taskId);
        
        // Evict status-specific caches
        if (oldStatus != null) {
            granularStrategy.evictStatusCache(oldStatus);
            cacheService.evictTaskStats(oldStatus.name());
        }
        
        if (newStatus != null && oldStatus != null && !oldStatus.equals(newStatus)) {
            granularStrategy.evictStatusCache(newStatus);
            cacheService.evictTaskStats(newStatus.name());
        }
        
        // Fallback to full list eviction if needed
        cacheService.evictTaskLists();
    }
}
