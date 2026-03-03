package com.leonardoborges.api.security;

import com.leonardoborges.api.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for JWT secret rotation preparation.
 * Provides infrastructure for automatic secret rotation.
 * 
 * Note: Full rotation requires coordination with token invalidation.
 * This service provides the foundation for rotation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtSecretRotationService {
    
    private final JwtProperties jwtProperties;
    
    // Atomic reference for thread-safe secret management
    private final AtomicReference<String> currentSecret = new AtomicReference<>();
    private final AtomicReference<String> previousSecret = new AtomicReference<>();
    private final AtomicReference<LocalDateTime> lastRotation = new AtomicReference<>();
    
    /**
     * Initializes the service with current secret.
     */
    public void initialize() {
        String secret = jwtProperties.getSecret();
        currentSecret.set(secret);
        lastRotation.set(LocalDateTime.now());
        log.info("JWT secret rotation service initialized");
    }
    
    /**
     * Gets the current active secret.
     */
    public String getCurrentSecret() {
        return currentSecret.get();
    }
    
    /**
     * Gets the previous secret (for token validation during rotation).
     */
    public String getPreviousSecret() {
        return previousSecret.get();
    }
    
    /**
     * Prepares for secret rotation.
     * In a full implementation, this would:
     * 1. Generate new secret
     * 2. Store previous secret for token validation
     * 3. Update configuration
     * 4. Invalidate old tokens (via TokenBlacklistService)
     */
    public void prepareRotation() {
        log.info("Preparing JWT secret rotation");
        // Full implementation would:
        // 1. Generate new secret
        // 2. Move current to previous
        // 3. Update current
        // 4. Schedule token invalidation
    }
    
    /**
     * Checks if rotation is needed based on policy.
     * Rotation policy: every 90 days (configurable).
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void checkRotationNeeded() {
        LocalDateTime lastRot = lastRotation.get();
        if (lastRot != null && lastRot.plusDays(90).isBefore(LocalDateTime.now())) {
            log.warn("JWT secret rotation recommended (last rotation: {})", lastRot);
            // In production, this would trigger rotation process
        }
    }
    
    /**
     * Validates if a token was signed with current or previous secret.
     * Allows graceful rotation without invalidating all tokens immediately.
     */
    public boolean isValidSecret(String secret) {
        String current = currentSecret.get();
        String previous = previousSecret.get();
        return secret.equals(current) || (previous != null && secret.equals(previous));
    }
}
