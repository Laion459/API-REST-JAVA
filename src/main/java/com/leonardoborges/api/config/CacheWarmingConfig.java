package com.leonardoborges.api.config;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Configuration for cache warming strategies.
 * Pre-loads frequently accessed data into cache on application startup.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CacheWarmingConfig {
    
    private final TaskRepository taskRepository;
    
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
        log.debug("Warming up task statistics cache");
        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            try {
                taskRepository.countByStatus(status);
                log.debug("Warmed up stats for status: {}", status);
            } catch (Exception e) {
                log.debug("Could not warm up stats for status {}: {}", status, e.getMessage());
            }
        }
    }
    
    private void warmUpTaskLists() {
        log.debug("Warming up task lists cache");
        try {
            Pageable firstPage = PageRequest.of(0, 20);
            taskRepository.findAll(firstPage);
            log.debug("Warmed up first page of tasks");
            
            // Warm up tasks by status
            for (Task.TaskStatus status : Task.TaskStatus.values()) {
                try {
                    taskRepository.findByStatus(status, firstPage);
                    log.debug("Warmed up tasks with status: {}", status);
                } catch (Exception e) {
                    log.debug("Could not warm up tasks for status {}: {}", status, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Could not warm up task lists: {}", e.getMessage());
        }
    }
}
