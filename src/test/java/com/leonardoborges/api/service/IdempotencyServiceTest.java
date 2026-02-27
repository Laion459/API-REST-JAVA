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
class IdempotencyServiceTest {
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @InjectMocks
    private IdempotencyService idempotencyService;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    void shouldReturnFalseForNonDuplicateRequest() {
        String idempotencyKey = "test-key";
        String requestHash = "hash123";
        
        when(valueOperations.get("idempotency:" + idempotencyKey)).thenReturn(null);
        
        assertFalse(idempotencyService.isDuplicateRequest(idempotencyKey, requestHash));
    }
    
    @Test
    void shouldReturnTrueForDuplicateRequest() {
        String idempotencyKey = "test-key";
        String requestHash = "hash123";
        
        when(valueOperations.get("idempotency:" + idempotencyKey)).thenReturn(requestHash);
        
        assertTrue(idempotencyService.isDuplicateRequest(idempotencyKey, requestHash));
    }
    
    @Test
    void shouldReturnFalseForDifferentRequestHash() {
        String idempotencyKey = "test-key";
        String requestHash = "hash123";
        String differentHash = "hash456";
        
        when(valueOperations.get("idempotency:" + idempotencyKey)).thenReturn(differentHash);
        
        assertFalse(idempotencyService.isDuplicateRequest(idempotencyKey, requestHash));
    }
    
    @Test
    void shouldReturnFalseForNullKey() {
        assertFalse(idempotencyService.isDuplicateRequest(null, "hash123"));
    }
    
    @Test
    void shouldStoreRequest() {
        String idempotencyKey = "test-key";
        String requestHash = "hash123";
        
        idempotencyService.storeRequest(idempotencyKey, requestHash);
        
        verify(valueOperations).set(eq("idempotency:" + idempotencyKey), eq(requestHash),
                eq(24L), eq(TimeUnit.HOURS));
    }
    
    @Test
    void shouldStoreRequestWithCustomTtl() {
        String idempotencyKey = "test-key";
        String requestHash = "hash123";
        long ttlHours = 48L;
        
        idempotencyService.storeRequest(idempotencyKey, requestHash, ttlHours);
        
        verify(valueOperations).set(eq("idempotency:" + idempotencyKey), eq(requestHash),
                eq(ttlHours), eq(TimeUnit.HOURS));
    }
    
    @Test
    void shouldNotStoreNullKey() {
        idempotencyService.storeRequest(null, "hash123");
        
        verifyNoInteractions(valueOperations);
    }
    
    @Test
    void shouldGenerateRequestHash() {
        String testObject = "test";
        String hash = idempotencyService.generateRequestHash(testObject);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }
    
    @Test
    void shouldGenerateEmptyHashForNull() {
        String hash = idempotencyService.generateRequestHash(null);
        
        assertEquals("", hash);
    }
}
