package com.leonardoborges.api.service;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.strategy.CacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.CreateCacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.DeleteCacheEvictionStrategy;
import com.leonardoborges.api.service.strategy.UpdateCacheEvictionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service que gerencia estratégias de eviction de cache usando Strategy Pattern.
 * Centraliza a lógica de decisão sobre qual estratégia usar.
 */
@Service
@RequiredArgsConstructor
public class CacheEvictionService {
    
    private final CreateCacheEvictionStrategy createStrategy;
    private final UpdateCacheEvictionStrategy updateStrategy;
    private final DeleteCacheEvictionStrategy deleteStrategy;
    
    /**
     * Executa eviction após criação de task.
     */
    public void evictAfterCreate(Long taskId, Task.TaskStatus status) {
        createStrategy.evict(taskId, null, status);
    }
    
    /**
     * Executa eviction após atualização de task.
     */
    public void evictAfterUpdate(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus) {
        updateStrategy.evict(taskId, oldStatus, newStatus);
    }
    
    /**
     * Executa eviction após deleção de task.
     */
    public void evictAfterDelete(Long taskId, Task.TaskStatus status) {
        deleteStrategy.evict(taskId, status, null);
    }
}
