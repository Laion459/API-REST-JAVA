package com.leonardoborges.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for WebConfig dependencies.
 * 
 * Provides test beans for web-related configurations that are required
 * by @WebMvcTest but not automatically loaded.
 * 
 * Best practices applied:
 * 1. Uses @Profile("test") to ensure it only loads in test environment
 * 2. Uses @Primary to override production CorsProperties in tests
 * 3. Provides minimal configuration needed for tests
 * 4. Follows Spring Boot testing best practices
 */
@TestConfiguration
@Profile("test")
public class TestWebConfig {
    
    @Bean
    @Primary
    public CorsProperties corsProperties() {
        CorsProperties props = new CorsProperties();
        // Set default values for tests
        props.setAllowedOrigins("http://localhost:3000,http://localhost:8080");
        props.setAllowedMethods("GET,POST,PUT,DELETE,PATCH,OPTIONS");
        props.setAllowedHeaders("*");
        props.setExposedHeaders("Authorization,Content-Type");
        props.setAllowCredentials(true);
        props.setMaxAge(3600L);
        return props;
    }
}
