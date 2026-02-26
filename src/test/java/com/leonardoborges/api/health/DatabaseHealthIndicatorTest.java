package com.leonardoborges.api.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseHealthIndicator Tests")
class DatabaseHealthIndicatorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DatabaseHealthIndicator databaseHealthIndicator;

    @Test
    @DisplayName("Should return UP when database is connected")
    void shouldReturnUp_WhenDatabaseIsConnected() {
        // Arrange
        Map<String, Object> result = Map.of("status", 1);
        when(jdbcTemplate.queryForMap("SELECT 1 as status")).thenReturn(result);

        // Act
        Health health = databaseHealthIndicator.health();

        // Assert
        assertEquals(Health.status("UP").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("database"));
        assertTrue(health.getDetails().containsKey("status"));
        assertEquals("Connected", health.getDetails().get("status"));
    }

    @Test
    @DisplayName("Should return DOWN when query result is null")
    void shouldReturnDown_WhenQueryResultIsNull() {
        // Arrange
        when(jdbcTemplate.queryForMap("SELECT 1 as status")).thenReturn(null);

        // Act
        Health health = databaseHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("database"));
    }

    @Test
    @DisplayName("Should return DOWN when query result does not contain status")
    void shouldReturnDown_WhenQueryResultDoesNotContainStatus() {
        // Arrange
        Map<String, Object> result = Map.of("other", "value");
        when(jdbcTemplate.queryForMap("SELECT 1 as status")).thenReturn(result);

        // Act
        Health health = databaseHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("database"));
    }

    @Test
    @DisplayName("Should return DOWN when exception occurs")
    void shouldReturnDown_WhenExceptionOccurs() {
        // Arrange
        when(jdbcTemplate.queryForMap("SELECT 1 as status"))
                .thenThrow(new RuntimeException("Connection error"));

        // Act
        Health health = databaseHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
        assertTrue(health.getDetails().containsKey("status"));
        assertEquals("Disconnected", health.getDetails().get("status"));
    }

    @Test
    @DisplayName("Should include database type in health response")
    void shouldIncludeDatabaseType_InHealthResponse() {
        // Arrange
        Map<String, Object> result = Map.of("status", 1);
        when(jdbcTemplate.queryForMap("SELECT 1 as status")).thenReturn(result);

        // Act
        Health health = databaseHealthIndicator.health();

        // Assert
        assertTrue(health.getDetails().containsKey("database"));
        assertEquals("PostgreSQL", health.getDetails().get("database"));
    }
}
