package com.leonardoborges.api.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisHealthIndicator Tests")
class RedisHealthIndicatorTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection connection;

    @InjectMocks
    private RedisHealthIndicator redisHealthIndicator;

    @BeforeEach
    void setUp() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
    }

    @Test
    @DisplayName("Should return UP when Redis responds with PONG")
    void shouldReturnUp_WhenRedisRespondsWithPong() {
        // Arrange
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        // Act
        Health health = redisHealthIndicator.health();

        // Assert
        assertEquals(Health.status("UP").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("redis"));
        assertTrue(health.getDetails().containsKey("status"));
        assertEquals("Available", health.getDetails().get("status"));
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Should return DOWN when Redis responds with non-PONG")
    void shouldReturnDown_WhenRedisRespondsWithNonPong() {
        // Arrange
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("ERROR");

        // Act
        Health health = redisHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("redis"));
        assertTrue(health.getDetails().containsKey("status"));
        assertEquals("Unavailable", health.getDetails().get("status"));
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Should return DOWN when exception occurs")
    void shouldReturnDown_WhenExceptionOccurs() {
        // Arrange
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("Connection error"));

        // Act
        Health health = redisHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
        assertTrue(health.getDetails().containsKey("status"));
        assertEquals("Unavailable", health.getDetails().get("status"));
    }

    @Test
    @DisplayName("Should close connection after ping")
    void shouldCloseConnection_AfterPing() {
        // Arrange
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        // Act
        redisHealthIndicator.health();

        // Assert
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Should handle connection close exception")
    void shouldHandleConnectionCloseException() {
        // Arrange
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");
        doThrow(new RuntimeException("Close error")).when(connection).close();

        // Act
        Health health = redisHealthIndicator.health();

        // Assert
        assertEquals(Health.status("DOWN").build().getStatus(), health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }
}
