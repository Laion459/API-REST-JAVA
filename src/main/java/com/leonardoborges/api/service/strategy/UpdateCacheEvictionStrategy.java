package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.CacheService;
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
    
    @Override
    public void evict(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus) {
        log.debug("Executing update cache eviction strategy for task: {}", taskId);
        
        cacheService.evictTask(taskId);
        cacheService.evictTaskLists();
        
        if (oldStatus != null) {
            cacheService.evictTaskStats(oldStatus.name());
            cacheService.evictTasksByStatus(oldStatus.name());
        }
        
        if (newStatus != null && oldStatus != null && !oldStatus.equals(newStatus)) {
            cacheService.evictTasksByStatus(newStatus.name());
            cacheService.evictTaskStats(newStatus.name());
        }
    }
}
