package com.leonardoborges.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for cache warming strategies.
 * Note: Cache warming is disabled as it requires user context for data isolation.
 * Cache will be populated naturally as users access their data.
 */
@Configuration
@Slf4j
public class CacheWarmingConfig {
    
    /**
     * Cache warming on application startup.
     * Only runs in production profile to avoid unnecessary load in development.
     */
    @Bean
    @Profile("prod")
    public CommandLineRunner cacheWarmer() {
        return args -> {
            log.info("Starting cache warming...");
            
            try {
                // Warm up task statistics for all statuses
                warmUpTaskStats();
                
                // Warm up first page of tasks (most frequently accessed)
                warmUpTaskLists();
                
                log.info("Cache warming completed successfully");
            } catch (Exception e) {
                log.warn("Cache warming failed, but application will continue: {}", e.getMessage());
            }
        };
    }
    
    private void warmUpTaskStats() {
        log.debug("Task statistics cache warming skipped - requires user context");
    }
    
    private void warmUpTaskLists() {
        log.debug("Task lists cache warming skipped - requires user context");
    }
}
