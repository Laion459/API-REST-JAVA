package com.leonardoborges.api.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for cache metrics.
 * Collects and exposes metrics for hit rate, miss rate, size, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheMetricsService {
    
    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;
    
    private final Map<String, Counter> hitCounters = new HashMap<>();
    private final Map<String, Counter> missCounters = new HashMap<>();
    private final Map<String, Timer> accessTimers = new HashMap<>();
    
    /**
     * Records a cache hit.
     */
    public void recordCacheHit(String cacheName) {
        Counter counter = hitCounters.computeIfAbsent(cacheName, 
                name -> Counter.builder("cache.hits")
                        .tag("cache", name)
                        .description("Number of cache hits")
                        .register(meterRegistry));
        counter.increment();
    }
    
    /**
     * Records a cache miss.
     */
    public void recordCacheMiss(String cacheName) {
        Counter counter = missCounters.computeIfAbsent(cacheName,
                name -> Counter.builder("cache.misses")
                        .tag("cache", name)
                        .description("Number of cache misses")
                        .register(meterRegistry));
        counter.increment();
    }
    
    /**
     * Measures cache access time.
     */
    public Timer.Sample startCacheAccessTimer(String cacheName) {
        Timer timer = accessTimers.computeIfAbsent(cacheName,
                name -> Timer.builder("cache.access.time")
                        .tag("cache", name)
                        .description("Cache access time")
                        .register(meterRegistry));
        return Timer.start(meterRegistry);
    }
    
    /**
     * Calculates and returns cache statistics.
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheStats = new HashMap<>();
                
                long hits = (long) hitCounters.getOrDefault(cacheName, 
                        Counter.builder("cache.hits").tag("cache", cacheName).register(meterRegistry)).count();
                long misses = (long) missCounters.getOrDefault(cacheName,
                        Counter.builder("cache.misses").tag("cache", cacheName).register(meterRegistry)).count();
                
                cacheStats.put("hits", hits);
                cacheStats.put("misses", misses);
                cacheStats.put("total", hits + misses);
                
                if (hits + misses > 0) {
                    double hitRate = (double) hits / (hits + misses) * 100;
                    cacheStats.put("hitRate", String.format("%.2f%%", hitRate));
                } else {
                    cacheStats.put("hitRate", "0.00%");
                }
                
                cacheStats.put("type", cache.getNativeCache().getClass().getSimpleName());
                
                stats.put(cacheName, cacheStats);
            }
        });
        
        return stats;
    }
    
    /**
     * Collects cache metrics periodically (every 5 minutes).
     */
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void collectCacheMetrics() {
        log.debug("Collecting cache metrics");
        Map<String, Object> stats = getCacheStatistics();
        stats.forEach((cacheName, cacheStats) -> {
            log.debug("Cache {} stats: {}", cacheName, cacheStats);
        });
    }
}
