package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cache eviction strategy for task creation.
 * Invalidates lists and statistics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateCacheEvictionStrategy implements CacheEvictionStrategy {
    
    private final CacheService cacheService;
    
    @Override
    public void evict(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus) {
        log.debug("Executing create cache eviction strategy for task: {}", taskId);
        cacheService.evictTaskLists();
        if (newStatus != null) {
            cacheService.evictTaskStats(newStatus.name());
        }
    }
}
