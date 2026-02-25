package com.leonardoborges.api.service;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.TaskHistory;
import com.leonardoborges.api.repository.TaskHistoryRepository;
import com.leonardoborges.api.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TaskHistoryService.
 * 
 * Usa @ExtendWith(MockitoExtension.class) para testes puros com mocks.
 * Não carrega contexto Spring, tornando os testes mais rápidos e isolados.
 * 
 * Boas práticas aplicadas:
 * - Testes isolados e independentes
 * - Nomes descritivos de testes com @DisplayName
 * - Verificação de comportamento (verify)
 * - Testes de casos de sucesso e erro
 */
@ExtendWith(MockitoExtension.class)
class TaskHistoryServiceTest {
    
    @Mock
    private TaskHistoryRepository taskHistoryRepository;
    
    @Mock
    private SecurityUtils securityUtils;
    
    private TaskHistoryService taskHistoryService;
    
    @BeforeEach
    void setUp() {
        taskHistoryService = new TaskHistoryService(
                taskHistoryRepository,
                securityUtils
        );
        
        lenient().when(securityUtils.getCurrentUsername()).thenReturn("testuser");
    }
    
    @Test
    @DisplayName("Deve registrar mudança de campo individual")
    void shouldRecordFieldChange_WhenFieldChanged() {
        // Arrange
        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        taskHistoryService.recordFieldChange(1L, "title", "Old Title", "New Title");
        
        // Assert
        verify(taskHistoryRepository, times(1)).save(any(TaskHistory.class));
    }
    
    @Test
    @DisplayName("Deve registrar múltiplas mudanças de campos")
    void shouldRecordTaskChanges_WhenMultipleFieldsChanged() {
        // Arrange
        Task oldTask = createTask(1L, "Old Title", "Old Description", Task.TaskStatus.PENDING, 1);
        Task newTask = createTask(1L, "New Title", "New Description", Task.TaskStatus.IN_PROGRESS, 2);
        
        when(taskHistoryRepository.save(any(TaskHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        taskHistoryService.recordTaskChanges(1L, oldTask, newTask);
        
        verify(taskHistoryRepository, atLeast(3)).save(any(TaskHistory.class));
    }
    
    @Test
    @DisplayName("Não deve registrar histórico quando campos não mudaram")
    void shouldNotRecordUnchangedFields_WhenNoChanges() {
        // Arrange
        Task task = createTask(1L, "Same Title", "Same Description", Task.TaskStatus.PENDING, 1);
        
        // Act
        taskHistoryService.recordTaskChanges(1L, task, task);
        
        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }
    
    @Test
    @DisplayName("Deve buscar histórico de tarefa ordenado por data")
    void shouldGetTaskHistory_OrderedByDate() {
        // Arrange
        TaskHistory history1 = TaskHistory.builder()
                .id(1L)
                .taskId(1L)
                .fieldName("title")
                .oldValue("Old")
                .newValue("New")
                .build();
        
        TaskHistory history2 = TaskHistory.builder()
                .id(2L)
                .taskId(1L)
                .fieldName("status")
                .oldValue("PENDING")
                .newValue("IN_PROGRESS")
                .build();
        
        when(taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(1L))
                .thenReturn(Arrays.asList(history1, history2));
        
        // Act
        List<TaskHistory> history = taskHistoryService.getTaskHistory(1L);
        
        // Assert
        assertEquals(2, history.size());
        verify(taskHistoryRepository, times(1)).findByTaskIdOrderByChangedAtDesc(1L);
    }
    
    /**
     * Helper method para criar objetos Task de teste.
     */
    private Task createTask(Long id, String title, String description, Task.TaskStatus status, Integer priority) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        return task;
    }
}
