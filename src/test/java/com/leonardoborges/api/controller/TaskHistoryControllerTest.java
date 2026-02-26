package com.leonardoborges.api.controller;

import com.leonardoborges.api.config.CorsProperties;
import com.leonardoborges.api.config.JpaAuditingConfig;
import com.leonardoborges.api.config.TestSecurityConfig;
import com.leonardoborges.api.model.TaskHistory;
import com.leonardoborges.api.repository.TaskHistoryRepository;
import com.leonardoborges.api.service.TaskHistoryService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskHistoryController.class,
        excludeAutoConfiguration = {
                HibernateJpaAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = JpaAuditingConfig.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("TaskHistoryController Tests")
class TaskHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskHistoryRepository taskHistoryRepository;

    @MockBean
    private TaskHistoryService taskHistoryService;

    @MockBean
    private CorsProperties corsProperties;

    private TaskHistory taskHistory;

    @BeforeEach
    void setUp() {
        taskHistory = TaskHistory.builder()
                .id(1L)
                .taskId(1L)
                .fieldName("title")
                .oldValue("Old Title")
                .newValue("New Title")
                .changedBy("testuser")
                .changedAt(LocalDateTime.now())
                .build();

        // Mock CorsProperties
        when(corsProperties.getAllowedOriginsList()).thenReturn(List.of("http://localhost:3000"));
        when(corsProperties.getAllowedMethodsList()).thenReturn(List.of("GET"));
        when(corsProperties.getAllowedHeadersList()).thenReturn(List.of("*"));
        when(corsProperties.getExposedHeadersList()).thenReturn(List.of("Authorization"));
        when(corsProperties.getAllowCredentials()).thenReturn(true);
        when(corsProperties.getMaxAge()).thenReturn(3600L);
    }

    @Test
    @DisplayName("Should list history of changes for a task")
    void shouldListHistoryOfChangesForATask() throws Exception {
        Page<TaskHistory> page = new PageImpl<>(List.of(taskHistory), PageRequest.of(0, 50), 1);
        when(taskHistoryRepository.findByTaskId(anyLong(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/tasks/1/history")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].fieldName").value("title"))
                .andExpect(jsonPath("$.content[0].oldValue").value("Old Title"))
                .andExpect(jsonPath("$.content[0].newValue").value("New Title"));
    }

    @Test
    @DisplayName("Should find history of a specific field")
    void shouldFindHistoryOfASpecificField() throws Exception {
        List<TaskHistory> history = Arrays.asList(taskHistory);
        when(taskHistoryRepository.findByTaskIdAndFieldName(anyLong(), anyString())).thenReturn(history);

        mockMvc.perform(get("/api/v1/tasks/1/history/field/title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("title"))
                .andExpect(jsonPath("$[0].oldValue").value("Old Title"))
                .andExpect(jsonPath("$[0].newValue").value("New Title"));
    }

    @Test
    @DisplayName("Should find history by date range")
    void shouldFindHistoryByDateRange() throws Exception {
        Page<TaskHistory> page = new PageImpl<>(List.of(taskHistory), PageRequest.of(0, 50), 1);
        when(taskHistoryRepository.findByTaskIdAndDateRange(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        mockMvc.perform(get("/api/v1/tasks/1/history/date-range")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }
}
