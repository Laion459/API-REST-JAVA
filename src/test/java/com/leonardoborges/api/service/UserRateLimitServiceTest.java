package com.leonardoborges.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
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
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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
        when(valueOperations.increment(contains(":count"))).thenReturn(1L);
        when(redisTemplate.expire(anyString(), any())).thenReturn(true);
        doNothing().when(valueOperations).set(anyString(), any());
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/tasks");
        assertFalse(exceeded);
    }

    @Test
    @DisplayName("Should return true when rate limit is exceeded")
    void shouldReturnTrueWhenRateLimitExceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(contains(":count"))).thenReturn(61L);
        lenient().when(redisTemplate.expire(anyString(), any())).thenReturn(true);
        lenient().doNothing().when(valueOperations).set(anyString(), any());
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/tasks");
        assertTrue(exceeded, "Rate limit should be exceeded when count is 61");
    }

    @Test
    @DisplayName("Should use auth limit for auth endpoints")
    void shouldUseAuthLimitForAuthEndpoints() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(contains(":count"))).thenReturn(6L);
        lenient().when(redisTemplate.expire(anyString(), any())).thenReturn(true);
        lenient().doNothing().when(valueOperations).set(anyString(), any());
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/auth/login");
        assertTrue(exceeded, "Auth rate limit should be exceeded when count is 6");
    }

    @Test
    @DisplayName("Should use admin limit for admin endpoints")
    void shouldUseAdminLimitForAdminEndpoints() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(contains(":count"))).thenReturn(201L);
        lenient().when(redisTemplate.expire(anyString(), any())).thenReturn(true);
        lenient().doNothing().when(valueOperations).set(anyString(), any());
        
        boolean exceeded = userRateLimitService.isRateLimitExceeded(1L, "/api/v1/cache/clear");
        assertTrue(exceeded, "Admin rate limit should be exceeded when count is 201");
    }

    @Test
    @DisplayName("Should reset rate limit for user")
    void shouldResetRateLimitForUser() {
        Set<String> keys = Collections.singleton("rate_limit:user:1:/api/v1/tasks");
        when(redisTemplate.keys(anyString())).thenReturn(keys);
        when(redisTemplate.delete(any(Set.class))).thenReturn(1L);
        
        userRateLimitService.resetRateLimit(1L);
        
        verify(redisTemplate).keys(anyString());
        verify(redisTemplate).delete(any(Set.class));
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
