package com.leonardoborges.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Rate Limit Service Tests")
class UserRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private UserRateLimitService userRateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        userRateLimitService = new UserRateLimitService(redisTemplate);
    }

    @Test
    @DisplayName("Should return false when user is null")
    void shouldReturnFalseWhenUserIsNull() {
        boolean exceeded = userRateLimitService.isRateLimitExceeded(null, "/api/v1/tasks");
        assertFalse(exceeded);
    }

    @Test
    @DisplayName("Should return false when rate limit is not exceeded")
    void shouldReturnFalseWhenRateLimitNotExceeded() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/tasks");
        assertFalse(exceeded);
    }

    @Test
    @DisplayName("Should return true when rate limit is exceeded")
    void shouldReturnTrueWhenRateLimitExceeded() {
        when(valueOperations.increment(anyString())).thenReturn(61L); // Exceeds default limit of 60
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/tasks");
        assertTrue(exceeded);
    }

    @Test
    @DisplayName("Should use auth limit for auth endpoints")
    void shouldUseAuthLimitForAuthEndpoints() {
        when(valueOperations.increment(anyString())).thenReturn(6L); // Exceeds auth limit of 5
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/auth/login");
        assertTrue(exceeded);
    }

    @Test
    @DisplayName("Should use admin limit for admin endpoints")
    void shouldUseAdminLimitForAdminEndpoints() {
        when(valueOperations.increment(anyString())).thenReturn(201L); // Exceeds admin limit of 200
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/cache/clear");
        assertTrue(exceeded);
    }

    @Test
    @DisplayName("Should reset rate limit for user")
    void shouldResetRateLimitForUser() {
        userRateLimitService.resetRateLimit(1L);
        
        verify(redisTemplate, atLeastOnce()).delete(any());
    }

    @Test
    @DisplayName("Should handle Redis failure gracefully")
    void shouldHandleRedisFailureGracefully() {
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis unavailable"));
        
        // Should fall back to in-memory rate limiting
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/tasks");
        // First call should not exceed (fallback creates new bucket)
        assertFalse(exceeded);
    }
}
