package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service para operações em lote (batch) de tasks.
 * Otimizado para processar múltiplas operações de forma eficiente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchTaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskValidationService taskValidationService;
    private final CacheEvictionService cacheEvictionService;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;
    
    /**
     * Cria múltiplas tasks em uma única transação.
     * 
     * @param requests Lista de requests para criação
     * @return Lista de tasks criadas
     */
    @Transactional
    public List<TaskResponse> createBatch(List<TaskRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Batch request cannot be empty");
        }
        
        if (requests.size() > 100) {
            throw new BusinessException("Batch size cannot exceed 100 tasks");
        }
        
        log.info("Creating batch of {} tasks", requests.size());
        User currentUser = securityUtils.getCurrentUser();
        
        List<Task> tasks = requests.stream()
                .map(request -> {
                    taskValidationService.validateAndSanitizeTaskRequest(request);
                    Task task = taskMapper.toEntity(request);
                    task.setUser(currentUser);
                    return task;
                })
                .collect(Collectors.toList());
        
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        log.info("Batch created successfully: {} tasks", savedTasks.size());
        
        // Audit
        savedTasks.forEach(task -> 
                auditService.audit("TASK_CREATED", "Task", task.getId(), 
                        String.format("Batch creation: Title: %s", task.getTitle())));
        
        // Evict cache
        savedTasks.forEach(task -> 
                cacheEvictionService.evictAfterCreate(task.getId(), task.getStatus()));
        
        return savedTasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Atualiza múltiplas tasks em uma única transação.
     * 
     * @param updates Map de taskId -> TaskRequest
     * @return Lista de tasks atualizadas
     */
    @Transactional
    public List<TaskResponse> updateBatch(java.util.Map<Long, TaskRequest> updates) {
        if (updates == null || updates.isEmpty()) {
            throw new BusinessException("Batch update cannot be empty");
        }
        
        if (updates.size() > 100) {
            throw new BusinessException("Batch size cannot exceed 100 tasks");
        }
        
        log.info("Updating batch of {} tasks", updates.size());
        User currentUser = securityUtils.getCurrentUser();
        
        Set<Long> taskIds = updates.keySet();
        List<Task> tasks = taskRepository.findAllById(taskIds).stream()
                .filter(task -> task.getUser().getId().equals(currentUser.getId()))
                .filter(task -> !task.getDeleted())
                .collect(Collectors.toList());
        
        if (tasks.size() != updates.size()) {
            throw new BusinessException("Some tasks were not found or do not belong to the user");
        }
        
        tasks.forEach(task -> {
            TaskRequest request = updates.get(task.getId());
            if (request != null) {
                Task.TaskStatus oldStatus = task.getStatus();
                taskValidationService.validateAndSanitizeTaskRequest(request);
                taskValidationService.validateStatusTransition(oldStatus, request.getStatus());
                taskMapper.updateEntityFromRequest(task, request);
                
                // Evict cache
                cacheEvictionService.evictAfterUpdate(task.getId(), oldStatus, request.getStatus());
            }
        });
        
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        log.info("Batch updated successfully: {} tasks", savedTasks.size());
        
        // Audit
        savedTasks.forEach(task -> 
                auditService.audit("TASK_UPDATED", "Task", task.getId(), 
                        String.format("Batch update: Title: %s", task.getTitle())));
        
        return savedTasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Deleta múltiplas tasks em uma única transação (soft delete).
     * 
     * @param taskIds Lista de IDs de tasks para deletar
     */
    @Transactional
    public void deleteBatch(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            throw new BusinessException("Batch delete cannot be empty");
        }
        
        if (taskIds.size() > 100) {
            throw new BusinessException("Batch size cannot exceed 100 tasks");
        }
        
        log.info("Deleting batch of {} tasks", taskIds.size());
        User currentUser = securityUtils.getCurrentUser();
        
        List<Task> tasks = taskRepository.findAllById(taskIds).stream()
                .filter(task -> task.getUser().getId().equals(currentUser.getId()))
                .filter(task -> !task.getDeleted())
                .collect(Collectors.toList());
        
        if (tasks.size() != taskIds.size()) {
            throw new BusinessException("Some tasks were not found or do not belong to the user");
        }
        
        String username = currentUser.getUsername();
        tasks.forEach(task -> {
            task.softDelete(username);
            cacheEvictionService.evictAfterDelete(task.getId(), task.getStatus());
        });
        
        taskRepository.saveAll(tasks);
        log.info("Batch deleted successfully: {} tasks", tasks.size());
        
        // Audit
        tasks.forEach(task -> 
                auditService.audit("TASK_DELETED", "Task", task.getId(), 
                        String.format("Batch deletion: Title: %s", task.getTitle())));
    }
}
