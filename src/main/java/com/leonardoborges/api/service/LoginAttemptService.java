package com.leonardoborges.api.service;

import com.leonardoborges.api.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing login attempts and account lockout.
 * Implements protection against brute force attacks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOGIN_ATTEMPT_KEY_PREFIX = "login:attempts:";
    private static final String ACCOUNT_LOCKED_KEY_PREFIX = "account:locked:";
    
    /**
     * Records a failed login attempt.
     * 
     * @param username The username that failed to login
     */
    public void recordFailedAttempt(String username) {
        String key = LOGIN_ATTEMPT_KEY_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().increment(key);
        
        if (attempts == 1) {
            // Set expiration for the counter (reset after lockout duration)
            redisTemplate.expire(key, Duration.ofMinutes(SecurityConstants.ACCOUNT_LOCKOUT_DURATION_MINUTES));
        }
        
        if (attempts != null && attempts >= SecurityConstants.MAX_LOGIN_ATTEMPTS) {
            lockAccount(username);
            log.warn("Account locked due to too many failed login attempts: {}", username);
        } else {
            log.debug("Failed login attempt {} for user: {}", attempts, username);
        }
    }
    
    /**
     * Records a successful login and clears failed attempts.
     * 
     * @param username The username that successfully logged in
     */
    public void recordSuccessfulLogin(String username) {
        String key = LOGIN_ATTEMPT_KEY_PREFIX + username;
        redisTemplate.delete(key);
        redisTemplate.delete(ACCOUNT_LOCKED_KEY_PREFIX + username);
        log.debug("Cleared login attempts for user: {}", username);
    }
    
    /**
     * Checks if an account is locked.
     * 
     * @param username The username to check
     * @return true if the account is locked, false otherwise
     */
    public boolean isAccountLocked(String username) {
        String key = ACCOUNT_LOCKED_KEY_PREFIX + username;
        Boolean locked = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(locked);
    }
    
    /**
     * Locks an account for the configured duration.
     * 
     * @param username The username to lock
     */
    private void lockAccount(String username) {
        String key = ACCOUNT_LOCKED_KEY_PREFIX + username;
        redisTemplate.opsForValue().set(key, "locked", 
            SecurityConstants.ACCOUNT_LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * Gets the number of remaining login attempts before lockout.
     * 
     * @param username The username to check
     * @return The number of remaining attempts, or MAX_LOGIN_ATTEMPTS if no attempts recorded
     */
    public int getRemainingAttempts(String username) {
        String key = LOGIN_ATTEMPT_KEY_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().get(key) != null ? 
            Long.valueOf(redisTemplate.opsForValue().get(key).toString()) : 0L;
        
        if (attempts == null || attempts == 0) {
            return SecurityConstants.MAX_LOGIN_ATTEMPTS;
        }
        
        return Math.max(0, SecurityConstants.MAX_LOGIN_ATTEMPTS - attempts.intValue());
    }
}
