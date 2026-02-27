package com.leonardoborges.api.service;

import com.leonardoborges.api.dto.TaskFilterRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.service.TaskValidationService;
import com.leonardoborges.api.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Filter Tests")
class TaskServiceFilterTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TaskMapper taskMapper;
    
    @Mock
    private TaskValidationService taskValidationService;
    
    @Mock
    private com.leonardoborges.api.metrics.TaskMetrics taskMetrics;
    
    @Mock
    private SecurityUtils securityUtils;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private TaskService taskService;
    
    private User testUser;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        pageable = PageRequest.of(0, 20);
        
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
    }
    
    @Test
    @DisplayName("Should filter tasks by status")
    void shouldFilterTasksByStatus() {
        TaskFilterRequest filters = TaskFilterRequest.builder()
                .status(Task.TaskStatus.PENDING)
                .build();
        
        Page<Task> taskPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(taskRepository.findTasksWithFilters(eq(testUser), eq(filters), any(Pageable.class)))
                .thenReturn(taskPage);
        
        Page<Task> taskPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        Page<TaskResponse> responsePage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(taskRepository.findTasksWithFilters(eq(testUser), eq(filters), any(Pageable.class)))
                .thenReturn(taskPage);
        when(taskMapper.toResponsePage(taskPage)).thenReturn(responsePage);
        
        Page<TaskResponse> result = taskService.getTasksWithFilters(filters, pageable);
        
        assertNotNull(result);
        verify(taskRepository).findTasksWithFilters(eq(testUser), eq(filters), any(Pageable.class));
    }
    
    @Test
    @DisplayName("Should filter tasks by priority range")
    void shouldFilterTasksByPriorityRange() {
        TaskFilterRequest filters = TaskFilterRequest.builder()
                .minPriority(5)
                .maxPriority(10)
                .build();
        
        Page<Task> taskPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        Page<TaskResponse> responsePage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(taskRepository.findTasksWithFilters(eq(testUser), eq(filters), any(Pageable.class)))
                .thenReturn(taskPage);
        when(taskMapper.toResponsePage(taskPage)).thenReturn(responsePage);
        
        Page<TaskResponse> result = taskService.getTasksWithFilters(filters, pageable);
        
        assertNotNull(result);
        verify(taskRepository).findTasksWithFilters(eq(testUser), eq(filters), any(Pageable.class));
    }
    
    @Test
    @DisplayName("Should filter tasks by text search")
    void shouldFilterTasksByTextSearch() {
        TaskFilterRequest filters = TaskFilterRequest.builder()
                .titleContains("test")
                .descriptionContains("feature")
                .build();
        
        Page<Task> taskPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        Page<TaskResponse> responsePage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(taskRepository.findTasksWithFilters(eq(testUser), eq(filters), any(Pageable.class)))
                .thenReturn(taskPage);
        when(taskMapper.toResponsePage(taskPage)).thenReturn(responsePage);
        
        Page<TaskResponse> result = taskService.getTasksWithFilters(filters, pageable);
        
        assertNotNull(result);
        verify(taskRepository).findTasksWithFilters(eq(testUser), eq(filters), any(Pageable.class));
    }
}
