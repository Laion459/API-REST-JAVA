package com.leonardoborges.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.leonardoborges.api.constants.TaskConstants.*;

@Configuration
@org.springframework.context.annotation.Profile("!test")
public class RateLimitConfig {

    @Bean
    public Bucket defaultBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RATE_LIMIT_REQUESTS_PER_MINUTE)
                .refillIntervally(RATE_LIMIT_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Bean
    public Bucket authBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE)
                .refillIntervally(RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Bean
    public Bucket adminBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RATE_LIMIT_ADMIN_REQUESTS_PER_MINUTE)
                .refillIntervally(RATE_LIMIT_ADMIN_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
