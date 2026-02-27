package com.leonardoborges.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    void shouldBlacklistToken() {
        String token = "test-token";
        long expirationTime = 3600000L;
        
        tokenBlacklistService.blacklistToken(token, expirationTime);
        
        verify(valueOperations).set(eq("blacklist:token:" + token), eq("blacklisted"), 
                eq(3600L), eq(TimeUnit.SECONDS));
    }
    
    @Test
    void shouldNotBlacklistNullToken() {
        tokenBlacklistService.blacklistToken(null, 3600000L);
        
        verifyNoInteractions(valueOperations);
    }
    
    @Test
    void shouldNotBlacklistEmptyToken() {
        tokenBlacklistService.blacklistToken("", 3600000L);
        
        verifyNoInteractions(valueOperations);
    }
    
    @Test
    void shouldReturnTrueForBlacklistedToken() {
        String token = "test-token";
        String key = "blacklist:token:" + token;
        
        when(redisTemplate.hasKey(key)).thenReturn(true);
        
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void shouldReturnFalseForNonBlacklistedToken() {
        String token = "test-token";
        String key = "blacklist:token:" + token;
        
        when(redisTemplate.hasKey(key)).thenReturn(false);
        
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void shouldReturnFalseForNullToken() {
        assertFalse(tokenBlacklistService.isTokenBlacklisted(null));
    }
    
    @Test
    void shouldRemoveTokenFromBlacklist() {
        String token = "test-token";
        String key = "blacklist:token:" + token;
        
        tokenBlacklistService.removeFromBlacklist(token);
        
        verify(redisTemplate).delete(key);
    }
    
    @Test
    void shouldNotRemoveNullToken() {
        tokenBlacklistService.removeFromBlacklist(null);
        
        verifyNoInteractions(redisTemplate);
    }
}
