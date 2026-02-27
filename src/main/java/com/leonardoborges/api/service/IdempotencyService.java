package com.leonardoborges.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {
    
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final long DEFAULT_TTL_HOURS = 24;
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isDuplicateRequest(String idempotencyKey, String requestHash) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return false;
        }
        
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        String existingHash = redisTemplate.opsForValue().get(key);
        
        if (existingHash != null) {
            boolean isDuplicate = existingHash.equals(requestHash);
            if (isDuplicate) {
                log.debug("Duplicate request detected for idempotency key: {}", idempotencyKey);
            }
            return isDuplicate;
        }
        
        return false;
    }
    
    public void storeRequest(String idempotencyKey, String requestHash) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return;
        }
        
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, requestHash, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Stored idempotency key: {}", idempotencyKey);
    }
    
    public void storeRequest(String idempotencyKey, String requestHash, long ttlHours) {
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return;
        }
        
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, requestHash, ttlHours, TimeUnit.HOURS);
        log.debug("Stored idempotency key: {} with TTL: {} hours", idempotencyKey, ttlHours);
    }
    
    public String generateRequestHash(Object request) {
        if (request == null) {
            return "";
        }
        return String.valueOf(request.hashCode());
    }
}
