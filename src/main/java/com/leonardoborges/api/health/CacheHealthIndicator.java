package com.leonardoborges.api.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator for cache system.
 * Checks if cache is properly configured and operational.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBean(CacheManager.class)
public class CacheHealthIndicator implements HealthIndicator {
    
    private final CacheManager cacheManager;
    
    @Override
    public Health health() {
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            Map<String, Object> cacheDetails = new HashMap<>();
            
            for (String cacheName : cacheNames) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheInfo = new HashMap<>();
                    cacheInfo.put("name", cacheName);
                    cacheInfo.put("status", "Available");
                    cacheDetails.put(cacheName, cacheInfo);
                } else {
                    cacheDetails.put(cacheName, Map.of("status", "Not Available"));
                }
            }
            
            if (cacheDetails.isEmpty()) {
                return Health.down()
                        .withDetail("cache", "No caches configured")
                        .withDetail("status", "Unavailable")
                        .build();
            }
            
            return Health.up()
                    .withDetail("cache", "Operational")
                    .withDetail("status", "Available")
                    .withDetail("caches", cacheDetails)
                    .withDetail("totalCaches", cacheNames.size())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("cache", "Error")
                    .withDetail("status", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
