package com.leonardoborges.api.service;

import com.leonardoborges.api.config.JwtProperties;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.model.RefreshToken;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing refresh tokens with persistence and revocation support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    
    /**
     * Creates and persists a new refresh token for a user.
     * 
     * @param user The user to create the token for
     * @return The refresh token string
     */
    @Transactional
    public String createRefreshToken(User user) {
        // Revoke all existing tokens for this user (optional: can allow multiple tokens)
        // For security, we'll allow multiple tokens but can revoke all on demand
        
        String token = jwtService.generateRefreshToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles().stream()
                                .map(role -> role.name())
                                .toArray(String[]::new))
                        .build()
        );
        
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshExpiration() / 1000);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created for user: {}", user.getUsername());
        
        return token;
    }
    
    /**
     * Validates and uses a refresh token.
     * Marks the token as used and returns the token entity.
     * 
     * @param token The refresh token string
     * @return The refresh token entity
     * @throws BusinessException if token is invalid, expired, revoked, or already used
     */
    @Transactional
    public RefreshToken validateAndUseToken(String token) {
        // First validate JWT structure
        if (!jwtService.validateRefreshToken(token)) {
            throw new BusinessException("Invalid or expired refresh token");
        }
        
        // Then check database
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findValidToken(
                token, LocalDateTime.now());
        
        if (tokenOpt.isEmpty()) {
            throw new BusinessException("Refresh token not found, expired, or revoked");
        }
        
        RefreshToken refreshToken = tokenOpt.get();
        
        // Mark as used (optional: can allow reuse, but for security we mark as used)
        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);
        
        log.debug("Refresh token validated and marked as used: {}", refreshToken.getId());
        
        return refreshToken;
    }
    
    /**
     * Revokes a specific refresh token.
     * 
     * @param token The refresh token string
     * @param revokedBy Username who revoked the token
     */
    @Transactional
    public void revokeToken(String token, String revokedBy) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(token);
        
        if (tokenOpt.isPresent()) {
            RefreshToken refreshToken = tokenOpt.get();
            refreshToken.revoke(revokedBy);
            refreshTokenRepository.save(refreshToken);
            String tokenPreview = token.length() > 20 ? token.substring(0, 20) + "..." : token;
            log.info("Refresh token revoked: {} by {}", tokenPreview, revokedBy);
        }
    }
    
    /**
     * Revokes all refresh tokens for a user.
     * Useful for logout or security incidents.
     * 
     * @param user The user whose tokens should be revoked
     * @param revokedBy Username who revoked the tokens
     */
    @Transactional
    public void revokeAllUserTokens(User user, String revokedBy) {
        int revokedCount = refreshTokenRepository.revokeAllUserTokens(
                user, LocalDateTime.now(), revokedBy);
        log.info("Revoked {} refresh tokens for user: {}", revokedCount, user.getUsername());
    }
    
    /**
     * Deletes expired tokens (scheduled task).
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void deleteExpiredTokens() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(30); // Keep for 30 days after expiry
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(expiryDate);
        log.info("Deleted {} expired refresh tokens", deletedCount);
    }
    
    /**
     * Gets the number of valid tokens for a user.
     * 
     * @param user The user
     * @return Number of valid tokens
     */
    @Transactional(readOnly = true)
    public long countValidTokens(User user) {
        return refreshTokenRepository.countValidTokensByUser(user, LocalDateTime.now());
    }
}
