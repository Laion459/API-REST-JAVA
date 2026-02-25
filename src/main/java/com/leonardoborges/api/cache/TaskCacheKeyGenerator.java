package com.leonardoborges.api.cache;

import com.leonardoborges.api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Custom key generator for task-related cache operations.
 * 
 * This generator creates cache keys that include the current user's username,
 * ensuring proper cache isolation per user. This is a best practice because:
 * 
 * 1. **Testability**: Can be easily mocked or tested independently
 * 2. **Maintainability**: Centralizes key generation logic
 * 3. **Flexibility**: Can handle edge cases (e.g., no security context in tests)
 * 4. **Type Safety**: Avoids SpEL evaluation errors at runtime
 * 
 * Usage in @Cacheable/@CachePut annotations:
 * @Cacheable(value = "tasks", keyGenerator = "taskCacheKeyGenerator")
 */
@Component("taskCacheKeyGenerator")
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile("!test")
public class TaskCacheKeyGenerator implements KeyGenerator {
    
    private final SecurityUtils securityUtils;
    
    @Override
    public Object generate(Object target, Method method, Object... params) {
        try {
            String username = securityUtils.getCurrentUsername();
            return buildKey(method.getName(), username, params);
        } catch (Exception e) {
            // Fallback for test environments or when security context is not available
            log.warn("Could not get current username for cache key generation, using fallback: {}", e.getMessage());
            return buildKey(method.getName(), "anonymous", params);
        }
    }
    
    private String buildKey(String methodName, String username, Object... params) {
        StringBuilder key = new StringBuilder();
        key.append(methodName).append("_").append(username);
        
        if (params != null && params.length > 0) {
            for (Object param : params) {
                if (param != null) {
                    key.append("_").append(param.toString());
                }
            }
        }
        
        return key.toString();
    }
}
