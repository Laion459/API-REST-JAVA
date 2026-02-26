package com.leonardoborges.api.controller;

import com.leonardoborges.api.config.CorsProperties;
import com.leonardoborges.api.config.JpaAuditingConfig;
import com.leonardoborges.api.config.TestSecurityConfig;
import com.leonardoborges.api.service.CacheService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CacheController.class,
        excludeAutoConfiguration = {
                HibernateJpaAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = JpaAuditingConfig.class))
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("CacheController Tests")
class CacheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CacheService cacheService;

    @MockBean
    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        // Mock CorsProperties
        when(corsProperties.getAllowedOriginsList()).thenReturn(List.of("http://localhost:3000"));
        when(corsProperties.getAllowedMethodsList()).thenReturn(List.of("GET", "POST", "DELETE"));
        when(corsProperties.getAllowedHeadersList()).thenReturn(List.of("*"));
        when(corsProperties.getExposedHeadersList()).thenReturn(List.of("Authorization"));
        when(corsProperties.getAllowCredentials()).thenReturn(true);
        when(corsProperties.getMaxAge()).thenReturn(3600L);
    }

    @Test
    @DisplayName("Should return cache statistics")
    void shouldReturnCacheStatistics() throws Exception {
        when(cacheService.getCacheStatistics()).thenReturn("Cache stats: 100 entries");

        mockMvc.perform(get("/api/v1/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistics").value("Cache stats: 100 entries"));
    }

    @Test
    @DisplayName("Should check if task is cached")
    void shouldCheckIfTaskIsCached() throws Exception {
        when(cacheService.isTaskCached(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/cache/tasks/1/cached"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cached").value(true));
    }

    @Test
    @DisplayName("Should return false when task is not cached")
    void shouldReturnFalseWhenTaskIsNotCached() throws Exception {
        when(cacheService.isTaskCached(1L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/cache/tasks/1/cached"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cached").value(false));
    }

    @Test
    @DisplayName("Should remove task from cache")
    void shouldRemoveTaskFromCache() throws Exception {
        doNothing().when(cacheService).evictTask(1L);

        mockMvc.perform(delete("/api/v1/cache/tasks/1"))
                .andExpect(status().isNoContent());

        verify(cacheService).evictTask(1L);
    }

    @Test
    @DisplayName("Should clear statistics cache")
    void shouldClearStatisticsCache() throws Exception {
        doNothing().when(cacheService).evictAllTaskStats();

        mockMvc.perform(delete("/api/v1/cache/stats"))
                .andExpect(status().isNoContent());

        verify(cacheService).evictAllTaskStats();
    }

    @Test
    @DisplayName("Should clear all caches")
    @WithMockUser(roles = "ADMIN")
    void shouldClearAllCaches() throws Exception {
        doNothing().when(cacheService).clearAllCaches();

        mockMvc.perform(delete("/api/v1/cache/all"))
                .andExpect(status().isNoContent());

        verify(cacheService).clearAllCaches();
    }
}
