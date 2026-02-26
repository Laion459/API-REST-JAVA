package com.leonardoborges.api.service;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.strategy.CacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.CreateCacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.DeleteCacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.UpdateCacheEvictionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service that manages cache eviction strategies using Strategy Pattern.
 * Centralizes the decision logic about which strategy to use.
 */
@Service
@RequiredArgsConstructor
public class CacheEvictionService {
    
    private final CreateCacheEvictionStrategy createStrategy;
    private final UpdateCacheEvictionStrategy updateStrategy;
    private final DeleteCacheEvictionStrategy deleteStrategy;
    
    /**
     * Executes eviction after task creation.
     */
    public void evictAfterCreate(Long taskId, Task.TaskStatus status) {
        createStrategy.evict(taskId, null, status);
    }
    
    /**
     * Executes eviction after task update.
     */
    public void evictAfterUpdate(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus) {
        updateStrategy.evict(taskId, oldStatus, newStatus);
    }
    
    /**
     * Executes eviction after task deletion.
     */
    public void evictAfterDelete(Long taskId, Task.TaskStatus status) {
        deleteStrategy.evict(taskId, status, null);
    }
}
