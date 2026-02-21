package com.leonardoborges.api.config;

import com.leonardoborges.api.metrics.TaskMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for custom metrics initialization.
 */
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class MetricsConfig {
    
    private final TaskMetrics taskMetrics;
    
    @Bean
    @ConditionalOnProperty(name = "app.metrics.enabled", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner initializeMetrics() {
        return args -> {
            taskMetrics.initializeMetrics();
        };
    }
}
