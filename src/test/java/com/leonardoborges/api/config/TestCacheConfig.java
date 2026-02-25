package com.leonardoborges.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Test cache configuration.
 * 
 * Provides test-specific cache configuration:
 * - CacheResolver that returns NoOpCache instances
 * - KeyGenerator with mocked SecurityUtils (via TestCacheKeyGenerator)
 * 
 * This follows best practices by:
 * 1. Separating test configuration from production code
 * 2. Using mocks for dependencies that require runtime context
 * 3. Providing fallback behavior for test environments
 */
@TestConfiguration
@EnableCaching
@Import(TestCacheKeyGenerator.class)
public class TestCacheConfig implements CachingConfigurer {
    
    @Bean
    @Primary
    public CacheResolver testCacheResolver() {
        return new TestCacheResolver();
    }
    
    @Override
    public CacheResolver cacheResolver() {
        return testCacheResolver();
    }
}
