package com.leonardoborges.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.TaskService;
import com.leonardoborges.api.util.SortParameterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.leonardoborges.api.config.CorsProperties;
import com.leonardoborges.api.config.JpaAuditingConfig;
import com.leonardoborges.api.config.TestSecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class, 
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, 
                classes = JpaAuditingConfig.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("TaskController Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private SortParameterValidator sortParameterValidator;

    @MockBean
    private CorsProperties corsProperties;

    private TaskResponse taskResponse;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(0L)
                .build();

        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();
    }

    @Test
    @DisplayName("Should create a new task successfully")
    void shouldCreateNewTaskSuccessfully() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return error 400 when invalid data is sent")
    void shouldReturnError400WhenInvalidDataIsSent() throws Exception {
        TaskRequest invalidRequest = TaskRequest.builder()
                .title("") // Empty title
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should find task by ID successfully")
    void shouldFindTaskByIdSuccessfully() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("Should list all tasks with pagination")
    void shouldListAllTasksWithPagination() throws Exception {
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        Page<TaskResponse> page = new PageImpl<>(tasks, PageRequest.of(0, 20), 1);
        
        when(sortParameterValidator.validateAndNormalizeTaskSort(any(Pageable.class), anyString(), any()))
                .thenReturn(PageRequest.of(0, 20));
        when(taskService.getAllTasks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Should list tasks by status")
    void shouldListTasksByStatus() throws Exception {
        List<TaskResponse> tasks = Arrays.asList(taskResponse);
        Page<TaskResponse> page = new PageImpl<>(tasks, PageRequest.of(0, 20), 1);
        
        when(sortParameterValidator.validateAndNormalizeTaskSort(any(Pageable.class), anyString(), any()))
                .thenReturn(PageRequest.of(0, 20));
        when(taskService.getTasksByStatus(eq(Task.TaskStatus.PENDING), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks/status/PENDING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return task statistics")
    void shouldReturnTaskStatistics() throws Exception {
        when(taskService.getTaskCountByStatus(Task.TaskStatus.PENDING)).thenReturn(5L);
        when(taskService.getTaskCountByStatus(Task.TaskStatus.IN_PROGRESS)).thenReturn(3L);
        when(taskService.getTaskCountByStatus(Task.TaskStatus.COMPLETED)).thenReturn(10L);
        when(taskService.getTaskCountByStatus(Task.TaskStatus.CANCELLED)).thenReturn(1L);

        mockMvc.perform(get("/api/v1/tasks/stats/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(5))
                .andExpect(jsonPath("$.in_progress").value(3))
                .andExpect(jsonPath("$.completed").value(10))
                .andExpect(jsonPath("$.cancelled").value(1));
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTaskSuccessfully() throws Exception {
        TaskResponse updatedResponse = TaskResponse.builder()
                .id(1L)
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(2)
                .version(1L)
                .build();

        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(updatedResponse);

        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(2)
                .build();

        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
