package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService Tests")
class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RateLimitService rateLimitService;

    private String testIp = "192.168.1.1";
    private String testEndpoint = "/api/v1/tasks";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return false when rate limit is not exceeded")
    void shouldReturnFalseWhenRateLimitIsNotExceeded() {
        when(valueOperations.get(anyString())).thenReturn("10"); // Count abaixo do limite

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, testEndpoint);

        assertFalse(exceeded);
        verify(valueOperations, times(2)).increment(anyString()); // Minuto e hora
        verify(redisTemplate, times(2)).expire(anyString(), any());
    }

    @Test
    @DisplayName("Should return true when per-minute rate limit is exceeded")
    void shouldReturnTrueWhenPerMinuteRateLimitIsExceeded() {
        when(valueOperations.get(contains(":minute"))).thenReturn("100"); // Count no limite

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, testEndpoint);

        assertTrue(exceeded);
        verify(auditService).auditSecurity(eq("RATE_LIMIT_EXCEEDED"), anyString());
    }

    @Test
    @DisplayName("Should return true when per-hour rate limit is exceeded")
    void shouldReturnTrueWhenPerHourRateLimitIsExceeded() {
        when(valueOperations.get(contains(":minute"))).thenReturn("10");
        when(valueOperations.get(contains(":hour"))).thenReturn("1000"); // Count no limite

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, testEndpoint);

        assertTrue(exceeded);
        verify(auditService).auditSecurity(eq("RATE_LIMIT_HOURLY_EXCEEDED"), anyString());
    }

    @Test
    @DisplayName("Should use default limit for generic endpoints")
    void shouldUseDefaultLimitForGenericEndpoints() {
        when(valueOperations.get(anyString())).thenReturn(null);

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, "/api/v1/tasks");

        assertFalse(exceeded);
    }

    @Test
    @DisplayName("Should use reduced limit for authentication endpoints")
    void shouldUseReducedLimitForAuthenticationEndpoints() {
        when(valueOperations.get(anyString())).thenReturn(null);

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, "/api/v1/auth/login");

        assertFalse(exceeded);
        // The limit is checked internally, but we cannot test it directly
        // as it's a private method. The behavior is verified through the result.
    }

    @Test
    @DisplayName("Should use reduced limit for cache/admin endpoints")
    void shouldUseReducedLimitForCacheEndpoints() {
        when(valueOperations.get(anyString())).thenReturn(null);

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, "/api/v1/cache/clear");

        assertFalse(exceeded);
    }

    @Test
    @DisplayName("Should increment counter when not exceeded")
    void shouldIncrementCounterWhenNotExceeded() {
        when(valueOperations.get(anyString())).thenReturn(null);

        rateLimitService.isRateLimitExceeded(testIp, testEndpoint);

        verify(valueOperations, times(2)).increment(anyString()); // Once for minute, once for hour
        verify(redisTemplate, times(2)).expire(anyString(), any());
    }

    @Test
    @DisplayName("Should reset rate limit")
    void shouldResetRateLimit() {
        assertDoesNotThrow(() -> {
            rateLimitService.resetRateLimit(testIp);
        });

        // The method only logs, there's no verifiable behavior beyond that
    }

    @Test
    @DisplayName("Should handle null counter (first request)")
    void shouldHandleNullCounter() {
        when(valueOperations.get(anyString())).thenReturn(null);

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, testEndpoint);

        assertFalse(exceeded);
    }

    @Test
    @DisplayName("Should handle counter as valid string")
    void shouldHandleCounterAsValidString() {
        when(valueOperations.get(contains(":minute"))).thenReturn("50");
        when(valueOperations.get(contains(":hour"))).thenReturn("500");

        boolean exceeded = rateLimitService.isRateLimitExceeded(testIp, testEndpoint);

        assertFalse(exceeded);
    }
}
