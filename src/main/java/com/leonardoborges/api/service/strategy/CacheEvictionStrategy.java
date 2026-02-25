package com.leonardoborges.api.service.strategy;

import com.leonardoborges.api.model.Task;

/**
 * Strategy interface para diferentes estratégias de eviction de cache.
 * Permite diferentes abordagens baseadas no tipo de operação.
 */
public interface CacheEvictionStrategy {
    
    /**
     * Executa a estratégia de eviction após uma operação.
     * 
     * @param taskId ID da task afetada
     * @param oldStatus Status anterior (pode ser null para criação)
     * @param newStatus Status novo (pode ser null para deleção)
     */
    void evict(Long taskId, Task.TaskStatus oldStatus, Task.TaskStatus newStatus);
}
