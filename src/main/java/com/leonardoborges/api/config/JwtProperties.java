package com.leonardoborges.api.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for JWT settings.
 * Validates that JWT secret is properly configured, especially in production.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
@Slf4j
public class JwtProperties {
    
    private final Environment environment;
    
    public JwtProperties(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * JWT secret key for signing tokens.
     * MUST be at least 32 characters long for HS256 algorithm.
     * REQUIRED in production - should be set via JWT_SECRET environment variable.
     */
    private String secret;
    
    /**
     * JWT access token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms).
     */
    private Long expiration = 86400000L;
    
    /**
     * JWT refresh token expiration time in milliseconds.
     * Default: 7 days (604800000 ms).
     */
    private Long refreshExpiration = 604800000L;
    
    /**
     * Validates JWT configuration on startup.
     * In production, secret must be provided and be at least 32 characters.
     */
    @PostConstruct
    public void validate() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isProduction = java.util.Arrays.asList(activeProfiles).contains("prod") || 
                              java.util.Arrays.asList(activeProfiles).contains("production");
        boolean isTest = java.util.Arrays.asList(activeProfiles).contains("test");
        
        // Skip validation in test profile
        if (isTest) {
            if (!StringUtils.hasText(secret)) {
                // Set a default test secret if not provided
                this.secret = "test-secret-key-for-testing-purposes-only-minimum-32-chars";
            }
            return;
        }
        
        if (!StringUtils.hasText(secret) || secret.length() < 32) {
            String message = String.format(
                    "JWT secret is not properly configured! " +
                    "Current length: %d, Required: at least 32 characters. " +
                    "Set JWT_SECRET environment variable with a secure random string.",
                    secret != null ? secret.length() : 0
            );
            
            if (isProduction) {
                log.error(message);
                throw new IllegalStateException(
                        "JWT_SECRET is required in production and must be at least 32 characters long. " +
                        "Please set the JWT_SECRET environment variable."
                );
            } else {
                log.warn(message);
                log.warn("Using default/weak secret in development. This is NOT secure for production!");
            }
        } else {
            log.info("JWT secret configured successfully (length: {})", secret.length());
        }
        
        if (expiration != null && expiration < 60000) {
            log.warn("JWT expiration is very short ({} ms). Consider using at least 1 hour (3600000 ms).", expiration);
        }
        
        if (refreshExpiration != null && refreshExpiration <= expiration) {
            log.warn("Refresh token expiration ({}) should be longer than access token expiration ({}).", 
                    refreshExpiration, expiration);
        }
    }
}
