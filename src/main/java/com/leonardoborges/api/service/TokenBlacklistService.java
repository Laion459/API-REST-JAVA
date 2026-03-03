package com.leonardoborges.api.service;

import com.leonardoborges.api.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private final RedisTemplate<String, String> redisTemplate;
    
    public void blacklistToken(String token, long expirationTimeMillis) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        String key = BLACKLIST_PREFIX + token;
        long ttlSeconds = expirationTimeMillis / 1000;
        
        redisTemplate.opsForValue().set(key, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
        log.debug("Token blacklisted: {}", LogSanitizer.sanitizeToken(token));
    }
    
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token removed from blacklist: {}", LogSanitizer.sanitizeToken(token));
    }
}
