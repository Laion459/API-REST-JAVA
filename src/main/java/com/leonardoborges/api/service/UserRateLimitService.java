package com.leonardoborges.api.service;

import com.leonardoborges.api.constants.TaskConstants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing user-based rate limiting.
 * Uses Redis for distributed rate limiting across multiple instances.
 * Falls back to in-memory buckets if Redis is unavailable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // In-memory fallback for when Redis is unavailable
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    
    private static final String REDIS_KEY_PREFIX = "rate_limit:user:";
    private static final int DEFAULT_USER_LIMIT = TaskConstants.RATE_LIMIT_REQUESTS_PER_MINUTE;
    private static final int AUTH_USER_LIMIT = TaskConstants.RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE;
    private static final int ADMIN_USER_LIMIT = TaskConstants.RATE_LIMIT_ADMIN_REQUESTS_PER_MINUTE;
    
    /**
     * Checks if the user has exceeded the rate limit for the given endpoint.
     * 
     * @param userId The user ID
     * @param endpoint The endpoint path
     * @return true if rate limit is exceeded, false otherwise
     */
    public boolean isRateLimitExceeded(Long userId, String endpoint) {
        if (userId == null) {
            return false; // No rate limiting for unauthenticated users (handled by IP-based filter)
        }
        
        Bucket bucket = getOrCreateBucket(userId, endpoint);
        return !bucket.tryConsume(1);
    }
    
    /**
     * Gets or creates a rate limit bucket for the user and endpoint.
     * 
     * @param userId The user ID
     * @param endpoint The endpoint path
     * @return The rate limit bucket
     */
    private Bucket getOrCreateBucket(Long userId, String endpoint) {
        int limit = getLimitForEndpoint(endpoint);
        String key = REDIS_KEY_PREFIX + userId + ":" + endpoint;
        
        try {
            // Try to use Redis for distributed rate limiting
            return getOrCreateRedisBucket(key, limit);
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to in-memory rate limiting for user: {}", userId, e);
            // Fallback to in-memory bucket
            return getOrCreateInMemoryBucket(key, limit);
        }
    }
    
    /**
     * Gets or creates a Redis-based bucket for distributed rate limiting.
     */
    private Bucket getOrCreateRedisBucket(String key, int limit) {
        // For distributed rate limiting with Redis, we would use Bucket4j Redis integration
        // For now, we'll use a simple counter approach with Redis
        String countKey = key + ":count";
        String resetKey = key + ":reset";
        
        Long currentCount = redisTemplate.opsForValue().increment(countKey);
        
        if (currentCount == 1) {
            // First request, set expiration
            redisTemplate.expire(countKey, Duration.ofMinutes(1));
            redisTemplate.opsForValue().set(resetKey, System.currentTimeMillis());
        }
        
        if (currentCount != null && currentCount > limit) {
            return createRejectedBucket();
        }
        
        return createAllowedBucket();
    }
    
    /**
     * Gets or creates an in-memory bucket (fallback).
     */
    private Bucket getOrCreateInMemoryBucket(String key, int limit) {
        return userBuckets.computeIfAbsent(key, k -> createBucket(limit));
    }
    
    /**
     * Creates a bucket with the specified limit.
     */
    private Bucket createBucket(int limit) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillIntervally(limit, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
    
    /**
     * Creates a bucket that always rejects requests (for rate limit exceeded).
     */
    private Bucket createRejectedBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(0)
                .refillIntervally(0, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
    
    /**
     * Creates a bucket that always allows requests.
     */
    private Bucket createAllowedBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(Integer.MAX_VALUE)
                .refillIntervally(Integer.MAX_VALUE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
    
    /**
     * Gets the rate limit for the given endpoint.
     */
    private int getLimitForEndpoint(String endpoint) {
        if (endpoint.startsWith("/api/v1/auth/")) {
            return AUTH_USER_LIMIT;
        }
        
        if (endpoint.startsWith("/api/v1/cache/")) {
            return ADMIN_USER_LIMIT;
        }
        
        return DEFAULT_USER_LIMIT;
    }
    
    /**
     * Resets the rate limit for a user.
     * 
     * @param userId The user ID
     */
    public void resetRateLimit(Long userId) {
        if (userId == null) {
            return;
        }
        
        String pattern = REDIS_KEY_PREFIX + userId + ":*";
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
        } catch (Exception e) {
            log.warn("Failed to reset rate limit in Redis for user: {}", userId, e);
        }
        
        // Also clear in-memory buckets
        userBuckets.entrySet().removeIf(entry -> entry.getKey().startsWith(REDIS_KEY_PREFIX + userId + ":"));
    }
}
