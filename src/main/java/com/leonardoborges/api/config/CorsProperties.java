package com.leonardoborges.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for CORS settings.
 * Allows CORS to be configured via application.yml instead of hardcoded values.
 */
@Component
@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
public class CorsProperties {
    
    /**
     * Allowed origins (comma-separated list).
     * Default: localhost origins for development.
     * In production, should be set to specific frontend domains.
     */
    private String allowedOrigins = "http://localhost:3000,http://localhost:8080";
    
    /**
     * Allowed HTTP methods.
     */
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD";
    
    /**
     * Allowed headers.
     */
    private String allowedHeaders = "*";
    
    /**
     * Exposed headers in CORS responses.
     */
    private String exposedHeaders = "Authorization,Content-Type,X-Refresh-Token";
    
    /**
     * Max age for preflight requests (in seconds).
     */
    private Long maxAge = 3600L;
    
    /**
     * Whether to allow credentials (cookies, authorization headers).
     */
    private Boolean allowCredentials = true;
    
    /**
     * Parses allowed origins from comma-separated string to List.
     */
    public List<String> getAllowedOriginsList() {
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
    
    /**
     * Parses allowed methods from comma-separated string to List.
     */
    public List<String> getAllowedMethodsList() {
        if (allowedMethods == null || allowedMethods.trim().isEmpty()) {
            return List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
        }
        return Arrays.stream(allowedMethods.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
    
    /**
     * Parses allowed headers from comma-separated string to List.
     */
    public List<String> getAllowedHeadersList() {
        if (allowedHeaders == null || allowedHeaders.trim().isEmpty() || "*".equals(allowedHeaders)) {
            return List.of("*");
        }
        return Arrays.stream(allowedHeaders.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
    
    /**
     * Parses exposed headers from comma-separated string to List.
     */
    public List<String> getExposedHeadersList() {
        if (exposedHeaders == null || exposedHeaders.trim().isEmpty()) {
            return List.of("Authorization", "Content-Type");
        }
        return Arrays.stream(exposedHeaders.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
