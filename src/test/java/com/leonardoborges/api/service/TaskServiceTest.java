package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.metrics.TaskMetrics;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.repository.UserRepository;
import com.leonardoborges.api.util.TestBuilders;
import com.leonardoborges.api.util.TestSecurityUtils;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TaskService.
 * 
 * Usa @ExtendWith(MockitoExtension.class) para testes puros com mocks.
 * Não carrega contexto Spring, tornando os testes mais rápidos e isolados.
 * 
 * Boas práticas aplicadas:
 * - Testes isolados e independentes
 * - Uso de builders para criar objetos de teste
 * - Nomes descritivos de testes com @DisplayName
 * - Verificação de comportamento (verify)
 * - Testes de casos de sucesso e erro
 * - Limpeza do contexto de segurança após cada teste
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private CacheEvictionService cacheEvictionService;
    
    @Mock
    private TaskMapper taskMapper;
    
    @Mock
    private TaskValidationService taskValidationService;
    
    @Mock
    private TaskMetrics taskMetrics;
    
    @Mock
    private Timer.Sample timerSample;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TaskHistoryService taskHistoryService;

    private TaskService taskService;
    private User testUser;
    private Task task;
    private TaskRequest taskRequest;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test user and security context
        testUser = TestBuilders.buildDefaultUser();
        TestSecurityUtils.setSecurityContext(testUser);
        
        // Mock UserRepository to return test user when SecurityUtils queries it
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Initialize TaskService with real SecurityUtils
        com.leonardoborges.api.util.SecurityUtils securityUtils = 
                new com.leonardoborges.api.util.SecurityUtils(userRepository);
        
        taskService = new TaskService(
                taskRepository,
                cacheEvictionService,
                taskMapper,
                taskValidationService,
                taskMetrics,
                auditService,
                securityUtils,
                taskHistoryService
        );
        
        // Setup test data
        task = TestBuilders.buildDefaultTask();
        task.setUser(testUser);
        
        taskRequest = TestBuilders.buildDefaultTaskRequest();
        taskResponse = TestBuilders.buildDefaultTaskResponse();
        
        // Setup lenient mocks for optional operations
        setupLenientMocks();
    }
    
    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }
    
    private void setupLenientMocks() {
        // TaskMetrics mocks (optional - may not be called in all tests)
        lenient().when(taskMetrics.startTaskCreationTimer()).thenReturn(timerSample);
        lenient().when(taskMetrics.startTaskUpdateTimer()).thenReturn(timerSample);
        lenient().when(taskMetrics.startTaskRetrievalTimer()).thenReturn(timerSample);
        lenient().doNothing().when(taskMetrics).incrementTaskCreated();
        lenient().doNothing().when(taskMetrics).incrementTaskUpdated();
        lenient().doNothing().when(taskMetrics).incrementTaskDeleted();
        lenient().doNothing().when(taskMetrics).incrementTaskRetrieved();
        lenient().doNothing().when(taskMetrics).incrementTaskStatus(anyString());
        lenient().doNothing().when(taskMetrics).recordTaskCreation(any(Timer.Sample.class));
        lenient().doNothing().when(taskMetrics).recordTaskUpdate(any(Timer.Sample.class));
        lenient().doNothing().when(taskMetrics).recordTaskRetrieval(any(Timer.Sample.class));
        
        // TaskValidationService mocks
        lenient().doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        lenient().doNothing().when(taskValidationService).validateStatusTransition(any(Task.TaskStatus.class), any(Task.TaskStatus.class));
        lenient().doNothing().when(taskValidationService).validateVersionForOptimisticLocking(any(Task.class), any(TaskRequest.class));
        
        // TaskMapper mocks
        lenient().when(taskMapper.toEntity(any(TaskRequest.class))).thenReturn(task);
        lenient().when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        lenient().doNothing().when(taskMapper).updateEntityFromRequest(any(Task.class), any(TaskRequest.class));
        
        // AuditService mocks
        lenient().doNothing().when(auditService).audit(anyString(), anyString(), anyLong(), anyString());
        lenient().doNothing().when(auditService).auditWithChanges(anyString(), anyString(), anyLong(), anyString(), anyString(), anyString());
        
        // TaskHistoryService mocks
        lenient().doNothing().when(taskHistoryService).recordTaskChanges(anyLong(), any(Task.class), any(Task.class));
    }

    @Test
    @DisplayName("Deve criar tarefa com sucesso quando dados são válidos")
    void shouldCreateTask_WhenValidRequest() {
        // Arrange
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        doNothing().when(cacheEvictionService).evictAfterCreate(anyLong(), any(Task.TaskStatus.class));

        // Act
        TaskResponse response = taskService.createTask(taskRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Test Task", response.getTitle());
        assertEquals(Task.TaskStatus.PENDING, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(cacheEvictionService, times(1)).evictAfterCreate(eq(task.getId()), eq(Task.TaskStatus.PENDING));
        verify(auditService, times(1)).audit(anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Deve buscar tarefa por ID quando tarefa existe")
    void shouldGetTaskById_WhenTaskExists() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));

        // Act
        TaskResponse response = taskService.getTaskById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        verify(taskRepository, times(1)).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("Deve lançar exceção quando tarefa não é encontrada")
    void shouldThrowException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
        verify(taskRepository, times(1)).findByIdAndUser(1L, testUser);
    }
    
    @Test
    @DisplayName("Deve listar todas as tarefas com paginação")
    void shouldGetAllTasks_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Task> tasks = Arrays.asList(task);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, 1);
        
        when(taskRepository.findByUser(testUser, pageable)).thenReturn(taskPage);
        @SuppressWarnings("unchecked")
        Page<Task> pageArg = any(Page.class);
        when(taskMapper.toResponsePage(pageArg)).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Page<Task> page = (Page<Task>) invocation.getArgument(0);
            return page.map(t -> taskResponse);
        });

        // Act
        Page<TaskResponse> response = taskService.getAllTasks(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(taskRepository, times(1)).findByUser(testUser, pageable);
        verify(taskMapper, times(1)).toResponsePage(any(Page.class));
    }

    @Test
    @DisplayName("Deve atualizar tarefa com sucesso quando dados são válidos")
    void shouldUpdateTask_WhenValidRequest() {
        // Arrange
        TaskRequest updateRequest = TestBuilders.defaultTaskRequest()
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(2)
                .version(0L)
                .build();
        
        Task updatedTask = TestBuilders.defaultTask()
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(2)
                .version(1L)
                .user(testUser)
                .build();
        
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toResponse(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            return TestBuilders.defaultTaskResponse()
                    .id(t.getId())
                    .title(t.getTitle())
                    .description(t.getDescription())
                    .status(t.getStatus())
                    .priority(t.getPriority())
                    .version(t.getVersion())
                    .build();
        });
        doNothing().when(cacheEvictionService).evictAfterUpdate(anyLong(), any(Task.TaskStatus.class), any(Task.TaskStatus.class));

        // Act
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Task", response.getTitle());
        assertEquals(Task.TaskStatus.IN_PROGRESS, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(cacheEvictionService, times(1)).evictAfterUpdate(
                eq(1L), eq(Task.TaskStatus.PENDING), eq(Task.TaskStatus.IN_PROGRESS));
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar tarefa inexistente")
    void shouldThrowException_WhenUpdatingNonExistentTask() {
        // Arrange
        TaskRequest updateRequest = TestBuilders.defaultTaskRequest()
                .title("Updated Task")
                .version(0L)
                .build();
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, updateRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    @DisplayName("Deve deletar tarefa com soft delete")
    void shouldDeleteTask_WithSoftDelete() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cacheEvictionService).evictAfterDelete(anyLong(), any(Task.TaskStatus.class));

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository, times(1)).save(argThat(t -> 
            t.getDeleted() != null && t.getDeleted() && "testuser".equals(t.getDeletedBy())
        ));
        verify(cacheEvictionService, times(1)).evictAfterDelete(eq(1L), eq(Task.TaskStatus.PENDING));
        verify(auditService, times(1)).audit(anyString(), anyString(), anyLong(), anyString());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar tarefa inexistente")
    void shouldThrowException_WhenDeletingNonExistentTask() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve filtrar tarefas por status")
    void shouldGetTasksByStatus_WhenStatusProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Task taskWithStatus = TestBuilders.buildTaskWithStatus(Task.TaskStatus.IN_PROGRESS);
        taskWithStatus.setUser(testUser);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(taskWithStatus), pageable, 1);
        
        when(taskRepository.findByUserAndStatus(testUser, Task.TaskStatus.IN_PROGRESS, pageable))
                .thenReturn(taskPage);
        @SuppressWarnings("unchecked")
        Page<Task> pageArg2 = any(Page.class);
        when(taskMapper.toResponsePage(pageArg2)).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Page<Task> page = (Page<Task>) invocation.getArgument(0);
            return page.map(t -> taskResponse);
        });

        // Act
        Page<TaskResponse> response = taskService.getTasksByStatus(Task.TaskStatus.IN_PROGRESS, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(taskRepository, times(1))
                .findByUserAndStatus(testUser, Task.TaskStatus.IN_PROGRESS, pageable);
        verify(taskMapper, times(1)).toResponsePage(any(Page.class));
    }

    @Test
    @DisplayName("Deve contar tarefas por status")
    void shouldGetTaskCountByStatus_WhenStatusProvided() {
        // Arrange
        when(taskRepository.countByUserAndStatus(testUser, Task.TaskStatus.PENDING)).thenReturn(5L);

        // Act
        Long count = taskService.getTaskCountByStatus(Task.TaskStatus.PENDING);

        // Assert
        assertEquals(5L, count);
        verify(taskRepository, times(1)).countByUserAndStatus(testUser, Task.TaskStatus.PENDING);
    }
}
