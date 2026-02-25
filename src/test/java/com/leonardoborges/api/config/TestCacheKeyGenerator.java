package com.leonardoborges.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Method;

/**
 * Test cache key generator configuration.
 * 
 * Provides a simple KeyGenerator for tests that doesn't depend on SecurityUtils.
 * This prevents SpEL evaluation errors and avoids mocking SecurityUtils which
 * could interfere with other components that need the real SecurityUtils.
 * 
 * Best practices applied:
 * 1. Uses @Profile("test") to ensure it only loads in test environment
 * 2. Uses @Primary to override the production KeyGenerator in tests
 * 3. Simple implementation without external dependencies
 * 4. Follows Spring Boot testing best practices
 */
@TestConfiguration
@Profile("test")
public class TestCacheKeyGenerator {
    
    @Bean("taskCacheKeyGenerator")
    @Primary
    public KeyGenerator taskCacheKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                // Generate a simple key based on method name and parameters
                // This doesn't require SecurityUtils, making tests simpler
                StringBuilder key = new StringBuilder();
                key.append(method.getName());
                
                if (params != null && params.length > 0) {
                    for (Object param : params) {
                        if (param != null) {
                            key.append("_").append(param.toString());
                        }
                    }
                }
                
                return key.toString();
            }
        };
    }
}
