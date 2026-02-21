package com.leonardoborges.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.leonardoborges.api.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateTask() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();

        when(taskService.createTask(any(TaskRequest.class)))
                .thenReturn(createMockTaskResponse());

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void shouldGetTaskById() throws Exception {
        when(taskService.getTaskById(1L))
                .thenReturn(createMockTaskResponse());

        mockMvc.perform(get("/api/v1/tasks/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    private com.leonardoborges.api.dto.TaskResponse createMockTaskResponse() {
        return com.leonardoborges.api.dto.TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();
    }
}
