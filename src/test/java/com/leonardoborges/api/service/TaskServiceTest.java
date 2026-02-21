package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.metrics.TaskMetrics;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.util.InputSanitizer;
import com.leonardoborges.api.util.SqlInjectionValidator;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private InputSanitizer inputSanitizer;
    
    @Mock
    private TaskMetrics taskMetrics;
    
    @Mock
    private Timer.Sample timerSample;
    
    @Mock
    private SqlInjectionValidator sqlInjectionValidator;
    
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();
        
        // Mock TaskMetrics with lenient to avoid unnecessary stubbing errors
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
        
        // Mock InputSanitizer with lenient
        lenient().when(inputSanitizer.sanitizeAndTruncate(anyString(), anyInt())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock SqlInjectionValidator
        lenient().when(sqlInjectionValidator.isSafe(anyString())).thenReturn(true);
        
        // Mock AuditService
        lenient().doNothing().when(auditService).audit(anyString(), anyString(), anyLong(), anyString());
        lenient().doNothing().when(auditService).auditWithChanges(anyString(), anyString(), anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldCreateTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        assertEquals("Test Task", response.getTitle());
        assertEquals(Task.TaskStatus.PENDING, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void shouldGetTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .build();

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, updateRequest));
    }
    
    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
    }

    @Test
    void shouldGetAllTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(task), pageable, 1);
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);

        Page<TaskResponse> response = taskService.getAllTasks(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(taskRepository, times(1)).findAll(pageable);
    }

    @Test
    void shouldUpdateTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        doNothing().when(cacheService).evictTask(anyLong());
        doNothing().when(cacheService).evictTaskLists();
        doNothing().when(cacheService).evictTasksByStatus(anyString());
        doNothing().when(cacheService).evictTaskStats(anyString());

        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(2)
                .build();

        TaskResponse response = taskService.updateTask(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Task", response.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void shouldDeleteTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).deleteById(1L);
        doNothing().when(cacheService).evictTask(anyLong());
        doNothing().when(cacheService).evictTaskLists();
        doNothing().when(cacheService).evictTasksByStatus(anyString());
        doNothing().when(cacheService).evictTaskStats(anyString());

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }
}
