package com.leonardoborges.api.config;

import com.leonardoborges.api.constants.TaskConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    @Profile("!redis")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            TaskConstants.CACHE_NAME_TASKS, 
            TaskConstants.CACHE_NAME_TASK_STATS
        );
    }
}
