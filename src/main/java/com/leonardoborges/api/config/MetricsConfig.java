package com.leonardoborges.api.config;

import com.leonardoborges.api.metrics.TaskMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom metrics initialization.
 */
@Configuration
@RequiredArgsConstructor
public class MetricsConfig {
    
    private final TaskMetrics taskMetrics;
    
    @Bean
    public CommandLineRunner initializeMetrics() {
        return args -> {
            taskMetrics.initializeMetrics();
        };
    }
}
