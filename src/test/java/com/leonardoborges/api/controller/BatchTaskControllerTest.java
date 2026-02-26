package com.leonardoborges.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonardoborges.api.config.CorsProperties;
import com.leonardoborges.api.config.JpaAuditingConfig;
import com.leonardoborges.api.config.TestSecurityConfig;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.BatchTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BatchTaskController.class,
        excludeAutoConfiguration = {
                HibernateJpaAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = JpaAuditingConfig.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("BatchTaskController Tests")
class BatchTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BatchTaskService batchTaskService;

    @MockBean
    private CorsProperties corsProperties;

    private TaskRequest taskRequest;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();

        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();

        // Mock CorsProperties
        when(corsProperties.getAllowedOriginsList()).thenReturn(List.of("http://localhost:3000"));
        when(corsProperties.getAllowedMethodsList()).thenReturn(List.of("GET", "POST", "PUT", "DELETE"));
        when(corsProperties.getAllowedHeadersList()).thenReturn(List.of("*"));
        when(corsProperties.getExposedHeadersList()).thenReturn(List.of("Authorization"));
        when(corsProperties.getAllowCredentials()).thenReturn(true);
        when(corsProperties.getMaxAge()).thenReturn(3600L);
    }

    @Test
    @DisplayName("Should create multiple tasks in batch")
    void shouldCreateMultipleTasksInBatch() throws Exception {
        List<TaskRequest> requests = Arrays.asList(taskRequest, taskRequest);
        List<TaskResponse> responses = Arrays.asList(taskResponse, taskResponse);

        when(batchTaskService.createBatch(anyList())).thenReturn(responses);

        BatchTaskController.BatchCreateRequest batchRequest = new BatchTaskController.BatchCreateRequest();
        batchRequest.setTasks(requests);

        mockMvc.perform(post("/api/v1/tasks/batch/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @DisplayName("Should update multiple tasks in batch")
    void shouldUpdateMultipleTasksInBatch() throws Exception {
        Map<Long, TaskRequest> updates = new HashMap<>();
        updates.put(1L, taskRequest);
        updates.put(2L, taskRequest);

        List<TaskResponse> responses = Arrays.asList(taskResponse, taskResponse);
        when(batchTaskService.updateBatch(any(Map.class))).thenReturn(responses);

        BatchTaskController.BatchUpdateRequest batchRequest = new BatchTaskController.BatchUpdateRequest();
        batchRequest.setUpdates(updates);

        mockMvc.perform(put("/api/v1/tasks/batch/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Should delete multiple tasks in batch")
    void shouldDeleteMultipleTasksInBatch() throws Exception {
        List<Long> taskIds = Arrays.asList(1L, 2L);
        doNothing().when(batchTaskService).deleteBatch(anyList());

        BatchTaskController.BatchDeleteRequest batchRequest = new BatchTaskController.BatchDeleteRequest();
        batchRequest.setTaskIds(taskIds);

        mockMvc.perform(delete("/api/v1/tasks/batch/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isNoContent());

        verify(batchTaskService).deleteBatch(anyList());
    }

    @Test
    @DisplayName("Should return error 400 when task list is empty")
    void shouldReturnError400WhenTaskListIsEmpty() throws Exception {
        BatchTaskController.BatchCreateRequest batchRequest = new BatchTaskController.BatchCreateRequest();
        batchRequest.setTasks(List.of());

        mockMvc.perform(post("/api/v1/tasks/batch/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isBadRequest());
    }
}
