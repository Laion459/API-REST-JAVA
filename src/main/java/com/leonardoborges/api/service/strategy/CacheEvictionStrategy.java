package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;

/**
 * Strategy interface for different cache eviction strategies.
 * Allows different approaches based on operation type.
 */
public interface CacheEvictionStrategy {
    
    /**
     * Executes the eviction strategy after an operation.
     * 
     * @param taskId ID of the affected task
     * @param oldStatus Previous status (can be null for creation)
     * @param newStatus New status (can be null for deletion)
     */
    void evict(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus);
}
