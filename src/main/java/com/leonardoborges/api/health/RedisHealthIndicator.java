package com.leonardoborges.api.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Redis connectivity.
 * Checks if Redis is accessible and responsive.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedisTemplate.class)
public class RedisHealthIndicator implements HealthIndicator {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Health health() {
        try {
            // Simple ping to check Redis connectivity
            var connection = redisTemplate.getConnectionFactory().getConnection();
            String pong = connection.ping();
            connection.close();
            
            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("redis", "Connected")
                        .withDetail("status", "Available")
                        .withDetail("check", "PING executed successfully")
                        .build();
            }
            
            return Health.down()
                    .withDetail("redis", "Unknown")
                    .withDetail("status", "Unavailable")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("redis", "Disconnected")
                    .withDetail("status", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
