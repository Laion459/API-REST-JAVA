package com.leonardoborges.api.service;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.TaskHistory;
import com.leonardoborges.api.repository.TaskHistoryRepository;
import com.leonardoborges.api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service para gerenciar histórico de mudanças de tasks.
 * Registra todas as alterações de campos para auditoria completa.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskHistoryService {
    
    private final TaskHistoryRepository taskHistoryRepository;
    private final SecurityUtils securityUtils;
    
    /**
     * Registra uma mudança de campo de forma assíncrona.
     * 
     * @param taskId ID da task
     * @param fieldName Nome do campo alterado
     * @param oldValue Valor anterior
     * @param newValue Novo valor
     */
    @Async
    @Transactional
    public void recordFieldChange(Long taskId, String fieldName, String oldValue, String newValue) {
        try {
            String changedBy = securityUtils.getCurrentUsername();
            
            TaskHistory history = TaskHistory.builder()
                    .taskId(taskId)
                    .fieldName(fieldName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .changedBy(changedBy)
                    .changedAt(LocalDateTime.now())
                    .build();
            
            taskHistoryRepository.save(history);
            log.debug("Task history recorded: taskId={}, field={}", taskId, fieldName);
        } catch (Exception e) {
            log.error("Failed to record task history: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Registra múltiplas mudanças de uma task.
     * 
     * @param taskId ID da task
     * @param oldTask Task antes das mudanças
     * @param newTask Task após as mudanças
     */
    @Async
    @Transactional
    public void recordTaskChanges(Long taskId, Task oldTask, Task newTask) {
        if (oldTask == null || newTask == null) {
            return;
        }
        
        if (!equalsOrNull(oldTask.getTitle(), newTask.getTitle())) {
            recordFieldChange(taskId, "title", 
                    oldTask.getTitle(), newTask.getTitle());
        }
        
        if (!equalsOrNull(oldTask.getDescription(), newTask.getDescription())) {
            recordFieldChange(taskId, "description", 
                    oldTask.getDescription(), newTask.getDescription());
        }
        
        if (oldTask.getStatus() != newTask.getStatus()) {
            recordFieldChange(taskId, "status", 
                    oldTask.getStatus() != null ? oldTask.getStatus().name() : null,
                    newTask.getStatus() != null ? newTask.getStatus().name() : null);
        }
        
        if (!equalsOrNull(oldTask.getPriority(), newTask.getPriority())) {
            recordFieldChange(taskId, "priority", 
                    oldTask.getPriority() != null ? oldTask.getPriority().toString() : null,
                    newTask.getPriority() != null ? newTask.getPriority().toString() : null);
        }
    }
    
    private boolean equalsOrNull(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    /**
     * Obtém histórico completo de uma task ordenado por data.
     * 
     * @param taskId ID da task
     * @return Lista de histórico ordenada por data (mais recente primeiro)
     */
    @Transactional(readOnly = true)
    public List<TaskHistory> getTaskHistory(Long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId);
    }
}
