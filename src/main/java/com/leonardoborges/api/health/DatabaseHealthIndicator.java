package com.leonardoborges.api.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Custom health indicator for database connectivity.
 * Checks if the database is accessible and responsive.
 */
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public Health health() {
        try {
            // Simple query to check database connectivity
            Map<String, Object> result = jdbcTemplate.queryForMap("SELECT 1 as status");
            
            if (result != null && result.containsKey("status")) {
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Connected")
                        .withDetail("check", "SELECT 1 executed successfully")
                        .build();
            }
            
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Unknown")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Disconnected")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
