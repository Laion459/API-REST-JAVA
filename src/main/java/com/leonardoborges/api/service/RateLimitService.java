package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.constants.TaskConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for IP-based rate limiting using Redis.
 * Provides per-IP rate limiting in addition to endpoint-based limits.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final AuditService auditService;
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:ip:";
    private static final int IP_RATE_LIMIT_PER_MINUTE = 100;
    private static final int IP_RATE_LIMIT_PER_HOUR = 1000;
    
    /**
     * Checks if an IP address has exceeded rate limits.
     * 
     * @param ipAddress The IP address to check
     * @param endpoint The endpoint being accessed
     * @return true if rate limit is exceeded, false otherwise
     */
    public boolean isRateLimitExceeded(String ipAddress, String endpoint) {
        String key = RATE_LIMIT_PREFIX + ipAddress + ":" + endpoint;
        
        // Check per-minute limit
        String countStr = redisTemplate.opsForValue().get(key + ":minute");
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        if (count >= getLimitForEndpoint(endpoint)) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", ipAddress, endpoint);
            auditService.auditSecurity("RATE_LIMIT_EXCEEDED", 
                    String.format("IP: %s, Endpoint: %s", ipAddress, endpoint));
            return true;
        }
        
        // Increment counter
        redisTemplate.opsForValue().increment(key + ":minute");
        redisTemplate.expire(key + ":minute", Duration.ofMinutes(1));
        
        // Check per-hour limit
        String hourKey = key + ":hour";
        String hourCountStr = redisTemplate.opsForValue().get(hourKey);
        int hourCount = hourCountStr != null ? Integer.parseInt(hourCountStr) : 0;
        
        if (hourCount >= IP_RATE_LIMIT_PER_HOUR) {
            log.warn("Hourly rate limit exceeded for IP: {} on endpoint: {}", ipAddress, endpoint);
            auditService.auditSecurity("RATE_LIMIT_HOURLY_EXCEEDED", 
                    String.format("IP: %s, Endpoint: %s", ipAddress, endpoint));
            return true;
        }
        
        redisTemplate.opsForValue().increment(hourKey);
        redisTemplate.expire(hourKey, Duration.ofHours(1));
        
        return false;
    }
    
    /**
     * Gets the rate limit for a specific endpoint.
     * 
     * @param endpoint The endpoint
     * @return The rate limit per minute
     */
    private int getLimitForEndpoint(String endpoint) {
        if (endpoint.startsWith("/api/v1/auth/")) {
            return TaskConstants.RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE;
        }
        if (endpoint.startsWith("/api/v1/cache/")) {
            return TaskConstants.RATE_LIMIT_ADMIN_REQUESTS_PER_MINUTE;
        }
        return IP_RATE_LIMIT_PER_MINUTE;
    }
    
    /**
     * Resets rate limit for an IP address.
     * Useful for whitelisted IPs or after security review.
     * 
     * @param ipAddress The IP address
     */
    public void resetRateLimit(String ipAddress) {
        String pattern = RATE_LIMIT_PREFIX + ipAddress + ":*";
        // Note: Redis doesn't support pattern deletion directly, 
        // this would need to be implemented with SCAN in production
        log.info("Rate limit reset requested for IP: {}", ipAddress);
    }
}
