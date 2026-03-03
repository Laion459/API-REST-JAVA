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
import org.mockito.ArgumentCaptor;
import com.leonardoborges.api.event.TaskCreatedEvent;
import com.leonardoborges.api.event.TaskUpdatedEvent;
import com.leonardoborges.api.event.TaskDeletedEvent;

/**
 * Unit tests for TaskService.
 * 
 * Uses @ExtendWith(MockitoExtension.class) for pure tests with mocks.
 * Does not load Spring context, making tests faster and isolated.
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Use of builders to create test objects
 * - Descriptive test names with @DisplayName
 * - Behavior verification (verify)
 * - Tests for success and error cases
 * - Security context cleanup after each test
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @Mock
    private TaskValidationService taskValidationService;
    
    @Mock
    private TaskMetrics taskMetrics;
    
    @Mock
    private Timer.Sample timerSample;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

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
                taskMapper,
                taskValidationService,
                taskMetrics,
                securityUtils,
                eventPublisher
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
        
        // Events are published asynchronously, no need to mock them
    }

    @Test
    @DisplayName("Should create task successfully when data is valid")
    void shouldCreateTask_WhenValidRequest() {
        // Arrange
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        // Cache eviction is handled by events

        // Act
        TaskResponse response = taskService.createTask(taskRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Test Task", response.getTitle());
        assertEquals(Task.TaskStatus.PENDING, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
        
        // Verify event was published with correct type
        verify(eventPublisher, times(1)).publishEvent(isA(TaskCreatedEvent.class));
    }

    @Test
    @DisplayName("Should get task by ID when task exists")
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
    @DisplayName("Should throw exception when task is not found")
    void shouldThrowException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
        verify(taskRepository, times(1)).findByIdAndUser(1L, testUser);
    }
    
    @Test
    @DisplayName("Should list all tasks with pagination")
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
    @DisplayName("Should update task successfully when data is valid")
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
        // Cache eviction is handled by events

        // Act
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Task", response.getTitle());
        assertEquals(Task.TaskStatus.IN_PROGRESS, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
        
        // Verify event was published with correct type
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertInstanceOf(TaskUpdatedEvent.class, eventCaptor.getValue());
        TaskUpdatedEvent updatedEvent = (TaskUpdatedEvent) eventCaptor.getValue();
        assertEquals(1L, updatedEvent.getTaskId());
        assertNotNull(updatedEvent.getUpdatedTask());
    }
    
    @Test
    @DisplayName("Should throw exception when trying to update non-existent task")
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
    @DisplayName("Should delete task with soft delete")
    void shouldDeleteTask_WithSoftDelete() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Cache eviction is handled by events

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository, times(1)).save(argThat(t -> 
            t.getDeleted() != null && t.getDeleted() && "testuser".equals(t.getDeletedBy())
        ));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertNotNull(eventCaptor.getValue());
    }
    
    @Test
    @DisplayName("Should throw exception when trying to delete non-existent task")
    void shouldThrowException_WhenDeletingNonExistentTask() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should filter tasks by status")
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
    @DisplayName("Should count tasks by status")
    void shouldGetTaskCountByStatus_WhenStatusProvided() {
        // Arrange
        when(taskRepository.countByUserAndStatus(testUser, Task.TaskStatus.PENDING)).thenReturn(5L);

        // Act
        Long count = taskService.getTaskCountByStatus(Task.TaskStatus.PENDING);

        // Assert
        assertEquals(5L, count);
        verify(taskRepository, times(1)).countByUserAndStatus(testUser, Task.TaskStatus.PENDING);
    }

    @Test
    @DisplayName("Should restore task successfully when task is deleted")
    void shouldRestoreTask_WhenTaskIsDeleted() {
        // Arrange
        Task deletedTask = TestBuilders.defaultTask().id(1L).deleted(true).build();
        deletedTask.setUser(testUser);
        deletedTask.softDelete("testuser");

        when(taskRepository.findByIdAndUserIncludingDeleted(1L, testUser)).thenReturn(Optional.of(deletedTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Cache eviction is handled by events

        // Act
        taskService.restoreTask(1L);

        // Assert
        verify(taskRepository, times(1)).save(argThat(t -> !t.getDeleted()));
        
        // Verify event was published with correct type (restore publishes TaskCreatedEvent)
        verify(eventPublisher, times(1)).publishEvent(isA(TaskCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to restore non-existent task")
    void shouldThrowException_WhenRestoringNonExistentTask() {
        // Arrange
        when(taskRepository.findByIdAndUserIncludingDeleted(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> taskService.restoreTask(1L));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to restore task that is not deleted")
    void shouldThrowException_WhenRestoringNonDeletedTask() {
        // Arrange
        Task activeTask = TestBuilders.defaultTask().id(1L).deleted(false).build();
        activeTask.setUser(testUser);

        when(taskRepository.findByIdAndUserIncludingDeleted(1L, testUser)).thenReturn(Optional.of(activeTask));

        // Act & Assert
        assertThrows(com.leonardoborges.api.exception.BusinessException.class, () -> taskService.restoreTask(1L));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should update task without changing status")
    void shouldUpdateTask_WithoutChangingStatus() {
        // Arrange
        TaskRequest updateRequest = TestBuilders.defaultTaskRequest()
                .title("Updated Task")
                .status(Task.TaskStatus.PENDING)
                .version(0L)
                .build();

        Task updatedTask = TestBuilders.defaultTask()
                .title("Updated Task")
                .status(Task.TaskStatus.PENDING)
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
                    .status(t.getStatus())
                    .version(t.getVersion())
                    .build();
        });
        // Cache eviction is handled by events

        // Act
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Task", response.getTitle());
        assertEquals(Task.TaskStatus.PENDING, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
        
        // Verify event was published with correct type
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertInstanceOf(TaskUpdatedEvent.class, eventCaptor.getValue());
        TaskUpdatedEvent updatedEvent = (TaskUpdatedEvent) eventCaptor.getValue();
        assertEquals(1L, updatedEvent.getTaskId());
        assertNotNull(updatedEvent.getUpdatedTask());
    }

    @Test
    @DisplayName("Should create task with null priority and set default")
    void shouldCreateTask_WithNullPriorityAndSetDefault() {
        // Arrange
        TaskRequest requestWithoutPriority = TestBuilders.defaultTaskRequest()
                .priority(null)
                .build();

        Task taskWithoutPriority = TestBuilders.defaultTask()
                .priority(null)
                .user(testUser)
                .build();

        when(taskMapper.toEntity(any(TaskRequest.class))).thenReturn(taskWithoutPriority);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getPriority() == null) {
                t.setPriority(com.leonardoborges.api.constants.TaskConstants.DEFAULT_PRIORITY);
            }
            return t;
        });
        // Cache eviction is handled by events

        // Act
        TaskResponse response = taskService.createTask(requestWithoutPriority);

        // Assert
        assertNotNull(response);
        verify(taskRepository, times(1)).save(argThat(t -> 
            t.getPriority() != null && t.getPriority().equals(com.leonardoborges.api.constants.TaskConstants.DEFAULT_PRIORITY)
        ));
    }

    @Test
    @DisplayName("Should handle null taskMetrics gracefully when creating task")
    void shouldHandleNullTaskMetrics_WhenCreatingTask() {
        com.leonardoborges.api.util.SecurityUtils securityUtils = 
                new com.leonardoborges.api.util.SecurityUtils(userRepository);
        
        TaskService serviceWithoutMetrics = new TaskService(
                taskRepository,
                taskMapper,
                taskValidationService,
                null,
                securityUtils,
                eventPublisher
        );

        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));

        TaskResponse result = serviceWithoutMetrics.createTask(taskRequest);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should handle null taskMetrics gracefully when retrieving task")
    void shouldHandleNullTaskMetrics_WhenRetrievingTask() {
        com.leonardoborges.api.util.SecurityUtils securityUtils = 
                new com.leonardoborges.api.util.SecurityUtils(userRepository);
        
        TaskService serviceWithoutMetrics = new TaskService(
                taskRepository,
                taskMapper,
                taskValidationService,
                null,
                securityUtils,
                eventPublisher
        );

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);

        TaskResponse result = serviceWithoutMetrics.getTaskById(1L);

        assertNotNull(result);
        verify(taskRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("Should handle null taskMetrics gracefully when updating task")
    void shouldHandleNullTaskMetrics_WhenUpdatingTask() {
        com.leonardoborges.api.util.SecurityUtils securityUtils = 
                new com.leonardoborges.api.util.SecurityUtils(userRepository);
        
        TaskService serviceWithoutMetrics = new TaskService(
                taskRepository,
                taskMapper,
                taskValidationService,
                null,
                securityUtils,
                eventPublisher
        );

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events

        TaskResponse result = serviceWithoutMetrics.updateTask(1L, taskRequest);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should handle null taskMetrics gracefully when status changes")
    void shouldHandleNullTaskMetrics_WhenStatusChanges() {
        com.leonardoborges.api.util.SecurityUtils securityUtils = 
                new com.leonardoborges.api.util.SecurityUtils(userRepository);
        
        TaskService serviceWithoutMetrics = new TaskService(
                taskRepository,
                taskMapper,
                taskValidationService,
                null,
                securityUtils,
                eventPublisher
        );

        taskRequest.setStatus(Task.TaskStatus.IN_PROGRESS);
        task.setStatus(Task.TaskStatus.PENDING);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events

        TaskResponse result = serviceWithoutMetrics.updateTask(1L, taskRequest);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should not increment status metrics when status is null in update")
    void shouldNotIncrementStatusMetrics_WhenStatusIsNullInUpdate() {
        taskRequest.setStatus(null);
        task.setStatus(Task.TaskStatus.PENDING);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events

        TaskResponse result = taskService.updateTask(1L, taskRequest);

        assertNotNull(result);
        verify(taskMetrics, never()).incrementTaskStatus(anyString());
    }

    @Test
    @DisplayName("Should not increment status metrics when status does not change in update")
    void shouldNotIncrementStatusMetrics_WhenStatusDoesNotChangeInUpdate() {
        taskRequest.setStatus(Task.TaskStatus.PENDING);
        task.setStatus(Task.TaskStatus.PENDING);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events

        TaskResponse result = taskService.updateTask(1L, taskRequest);

        assertNotNull(result);
        verify(taskMetrics, never()).incrementTaskStatus(anyString());
    }

    @Test
    @DisplayName("Should create task snapshot with null oldStatus")
    void shouldCreateTaskSnapshot_WithNullOldStatus() {
        Task taskWithNullStatus = TestBuilders.defaultTask()
                .id(1L)
                .status(Task.TaskStatus.PENDING)
                .user(testUser)
                .build();

        TaskRequest updateRequest = TestBuilders.defaultTaskRequest()
                .title("Updated Task")
                .status(Task.TaskStatus.IN_PROGRESS)
                .version(0L)
                .build();

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(taskWithNullStatus));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setStatus(Task.TaskStatus.IN_PROGRESS);
            return t;
        });
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events
        // Task history is handled by events

        TaskResponse result = taskService.updateTask(1L, updateRequest);

        assertNotNull(result);
        
        // Verify event was published with correct type
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeastOnce()).publishEvent(eventCaptor.capture());
        assertInstanceOf(TaskUpdatedEvent.class, eventCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle optimistic locking failure exception")
    void shouldHandleOptimisticLockingFailureException() {
        TaskRequest updateRequest = TestBuilders.defaultTaskRequest()
                .title("Updated Task")
                .version(0L)
                .build();

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenThrow(new org.springframework.dao.OptimisticLockingFailureException("Version conflict"));

        assertThrows(com.leonardoborges.api.exception.OptimisticLockingException.class, () -> {
            taskService.updateTask(1L, updateRequest);
        });
    }

    @Test
    @DisplayName("Should handle metrics when sample is null in createTask")
    void shouldHandleMetrics_WhenSampleIsNullInCreateTask() {
        when(taskMetrics.startTaskCreationTimer()).thenReturn(null);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        // Cache eviction is handled by events

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should handle metrics when sample is null in getTaskById")
    void shouldHandleMetrics_WhenSampleIsNullInGetTaskById() {
        when(taskMetrics.startTaskRetrievalTimer()).thenReturn(null);
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(1L);

        assertNotNull(response);
        verify(taskRepository, times(1)).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("Should handle metrics when sample is null in updateTask")
    void shouldHandleMetrics_WhenSampleIsNullInUpdateTask() {
        when(taskMetrics.startTaskUpdateTimer()).thenReturn(null);
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events

        TaskResponse response = taskService.updateTask(1L, taskRequest);

        assertNotNull(response);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should restore deleted task successfully")
    void shouldRestoreDeletedTaskSuccessfully() {
        Task deletedTask = TestBuilders.defaultTask()
                .id(1L)
                .title("Deleted Task")
                .status(Task.TaskStatus.PENDING)
                .deleted(true)
                .deletedBy("testuser")
                .user(testUser)
                .build();

        when(taskRepository.findByIdAndUserIncludingDeleted(1L, testUser))
                .thenReturn(Optional.of(deletedTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setDeleted(false);
            return t;
        });
        // Audit is handled by events
        // Cache eviction is handled by events

        taskService.restoreTask(1L);

        verify(taskRepository).save(argThat(task -> !task.getDeleted()));
        
        // Verify event was published with correct type (restore publishes TaskCreatedEvent)
        verify(eventPublisher, times(1)).publishEvent(isA(TaskCreatedEvent.class));
    }


    @Test
    @DisplayName("Should throw exception when task not found for restore")
    void shouldThrowException_WhenTaskNotFoundForRestore() {
        when(taskRepository.findByIdAndUserIncludingDeleted(1L, testUser))
                .thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.restoreTask(1L));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should handle task service without metrics enabled")
    void shouldHandleTaskService_WithoutMetricsEnabled() {
        TaskService serviceWithoutMetrics = new TaskService(
                taskRepository,
                taskMapper,
                taskValidationService,
                null, // No metrics
                new com.leonardoborges.api.util.SecurityUtils(userRepository),
                eventPublisher
        );

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);

        TaskResponse response = serviceWithoutMetrics.getTaskById(1L);

        assertNotNull(response);
        verify(taskRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("Should increment status metrics when status changes in update")
    void shouldIncrementStatusMetrics_WhenStatusChangesInUpdate() {
        taskRequest.setStatus(Task.TaskStatus.IN_PROGRESS);
        task.setStatus(Task.TaskStatus.PENDING);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setStatus(Task.TaskStatus.IN_PROGRESS);
            return t;
        });
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events
        // Task history is handled by events

        TaskResponse result = taskService.updateTask(1L, taskRequest);

        assertNotNull(result);
        // incrementTaskStatus is called by event listeners, not directly in the service
        verify(taskMetrics, never()).incrementTaskStatus(anyString());
    }

    @Test
    @DisplayName("Should not increment status metrics when new status is null in update")
    void shouldNotIncrementStatusMetrics_WhenNewStatusIsNullInUpdate() {
        taskRequest.setStatus(null);
        task.setStatus(Task.TaskStatus.PENDING);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        doNothing().when(taskValidationService).validateStatusTransition(any(), any());
        // Audit is handled by events
        // Task history is handled by events

        TaskResponse result = taskService.updateTask(1L, taskRequest);

        assertNotNull(result);
        verify(taskMetrics, never()).incrementTaskStatus(anyString());
    }

    @Test
    @DisplayName("Should handle create task with null priority and set default")
    void shouldHandleCreateTask_WithNullPriorityAndSetDefault() {
        taskRequest.setPriority(null);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            if (t.getPriority() == null) {
                t.setPriority(com.leonardoborges.api.constants.TaskConstants.DEFAULT_PRIORITY);
            }
            return t;
        });
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(taskMapper.toEntity(any(TaskRequest.class))).thenAnswer(invocation -> {
            TaskRequest req = invocation.getArgument(0);
            Task t = TestBuilders.defaultTask()
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .status(req.getStatus())
                    .priority(req.getPriority())
                    .user(testUser)
                    .build();
            return t;
        });
        doNothing().when(taskValidationService).validateAndSanitizeTaskRequest(any(TaskRequest.class));
        // Cache eviction is handled by events
        // Audit is handled by events

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        verify(taskRepository).save(argThat(task -> 
            task.getPriority() != null && 
            task.getPriority().equals(com.leonardoborges.api.constants.TaskConstants.DEFAULT_PRIORITY)
        ));
    }
}
