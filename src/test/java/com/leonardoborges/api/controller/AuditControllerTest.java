package com.leonardoborges.api.controller;

import com.leonardoborges.api.config.CorsProperties;
import com.leonardoborges.api.config.JpaAuditingConfig;
import com.leonardoborges.api.config.TestSecurityConfig;
import com.leonardoborges.api.model.AuditLog;
import com.leonardoborges.api.repository.AuditLogRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuditController.class,
        excludeAutoConfiguration = {
                HibernateJpaAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = JpaAuditingConfig.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AuditController Tests")
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogRepository auditLogRepository;

    @MockBean
    private CorsProperties corsProperties;
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .id(1L)
                .action("TASK_CREATED")
                .entityType("Task")
                .entityId(1L)
                .username("testuser")
                .ipAddress("192.168.1.1")
                .description("Task created")
                .success(true)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock CorsProperties
        when(corsProperties.getAllowedOriginsList()).thenReturn(List.of("http://localhost:3000"));
        when(corsProperties.getAllowedMethodsList()).thenReturn(List.of("GET", "POST"));
        when(corsProperties.getAllowedHeadersList()).thenReturn(List.of("*"));
        when(corsProperties.getExposedHeadersList()).thenReturn(List.of("Authorization"));
        when(corsProperties.getAllowCredentials()).thenReturn(true);
        when(corsProperties.getMaxAge()).thenReturn(3600L);
    }

    @Test
    @DisplayName("Should list all audit logs")
    @WithMockUser(roles = "ADMIN")
    void shouldListAllAuditLogs() throws Exception {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 50), 1);
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].action").value("TASK_CREATED"));
    }

    @Test
    @DisplayName("Should find logs by action")
    @WithMockUser(roles = "ADMIN")
    void shouldFindLogsByAction() throws Exception {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 50), 1);
        when(auditLogRepository.findByAction(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit/action/TASK_CREATED")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("TASK_CREATED"));
    }

    @Test
    @DisplayName("Should find logs by entity")
    @WithMockUser(roles = "ADMIN")
    void shouldFindLogsByEntity() throws Exception {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 50), 1);
        when(auditLogRepository.findByEntityTypeAndEntityId(anyString(), any(Long.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/audit/entity/Task/1")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].entityType").value("Task"))
                .andExpect(jsonPath("$.content[0].entityId").value(1L));
    }

    @Test
    @DisplayName("Should find logs by user")
    @WithMockUser(roles = "ADMIN")
    void shouldFindLogsByUser() throws Exception {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 50), 1);
        when(auditLogRepository.findByUsername(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit/user/testuser")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @DisplayName("Should find logs by date range")
    @WithMockUser(roles = "ADMIN")
    void shouldFindLogsByDateRange() throws Exception {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 50), 1);
        when(auditLogRepository.findByDateRange(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        mockMvc.perform(get("/api/v1/audit/date-range")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should return count of failed actions")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnCountOfFailedActions() throws Exception {
        when(auditLogRepository.countFailedActionsSince(any(LocalDateTime.class))).thenReturn(5L);

        mockMvc.perform(get("/api/v1/audit/stats/failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5L));
    }

    @Test
    @DisplayName("Should return count of failed actions with specific date")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnCountOfFailedActionsWithSpecificDate() throws Exception {
        LocalDateTime since = LocalDateTime.now().minusHours(12);
        when(auditLogRepository.countFailedActionsSince(any(LocalDateTime.class))).thenReturn(3L);

        mockMvc.perform(get("/api/v1/audit/stats/failed")
                        .param("since", since.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3L));
    }
}
