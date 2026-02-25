package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Estratégia de eviction para deleção de tasks.
 * Invalida task individual, listas e estatísticas do status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteCacheEvictionStrategy implements CacheEvictionStrategy {
    
    private final CacheService cacheService;
    
    @Override
    public void evict(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus) {
        log.debug("Executing delete cache eviction strategy for task: {}", taskId);
        
        cacheService.evictTask(taskId);
        cacheService.evictTaskLists();
        
        if (oldStatus != null) {
            cacheService.evictTasksByStatus(oldStatus.name());
            cacheService.evictTaskStats(oldStatus.name());
        }
    }
}
