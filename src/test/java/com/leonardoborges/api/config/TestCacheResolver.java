package com.leonardoborges.api.config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.NoOpCache;

import java.util.Collection;

/**
 * Test cache resolver that returns NoOpCache instances.
 * This prevents SpEL evaluation errors while satisfying Spring's requirement
 * that at least one cache be provided per cache operation.
 */
public class TestCacheResolver implements CacheResolver {
    
    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        // Return a NoOpCache for each cache name specified in the annotation
        // This satisfies Spring's requirement without evaluating SpEL
        return context.getOperation().getCacheNames().stream()
                .map(NoOpCache::new)
                .toList();
    }
}
