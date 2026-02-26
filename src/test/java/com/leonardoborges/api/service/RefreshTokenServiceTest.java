package com.leonardoborges.api.service;

import com.leonardoborges.api.config.JwtProperties;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.model.RefreshToken;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.RefreshTokenRepository;
import com.leonardoborges.api.util.TestBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RefreshTokenService.
 * 
 * Uses @ExtendWith(MockitoExtension.class) for pure tests with mocks.
 * Does not load Spring context, making tests faster and isolated.
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Use of builders to create test objects
 * - Descriptive test names with @DisplayName
 * - Behavior verification (verify)
 * - Tests for success and error cases
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private JwtProperties jwtProperties;
    
    private RefreshTokenService refreshTokenService;
    
    private User testUser;
    private RefreshToken validToken;
    
    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                jwtService,
                jwtProperties
        );
        
        testUser = TestBuilders.buildDefaultUser();
        
        validToken = RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .used(false)
                .build();
        
        lenient().when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L); // 7 days
        lenient().when(jwtService.generateRefreshToken(any())).thenReturn("new-refresh-token");
    }
    
    @Test
    @DisplayName("Should create refresh token successfully")
    void shouldCreateRefreshToken_WhenValidUser() {
        // Arrange
        when(jwtService.generateRefreshToken(any())).thenReturn("new-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validToken);
        
        // Act
        String token = refreshTokenService.createRefreshToken(testUser);
        
        // Assert
        assertNotNull(token);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }
    
    @Test
    @DisplayName("Should validate and mark token as used")
    void shouldValidateAndUseToken_WhenValidToken() {
        // Arrange
        when(jwtService.validateRefreshToken("valid-token")).thenReturn(true);
        lenient().when(jwtService.extractUsername("valid-token")).thenReturn("testuser");
        when(refreshTokenRepository.findValidToken(eq("valid-token"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(validToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validToken);
        
        // Act
        RefreshToken result = refreshTokenService.validateAndUseToken("valid-token");
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getUsed());
        verify(refreshTokenRepository, times(1)).save(validToken);
    }
    
    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken_WhenTokenIsInvalid() {
        // Arrange
        when(jwtService.validateRefreshToken("invalid-token")).thenReturn(false);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            refreshTokenService.validateAndUseToken("invalid-token");
        });
        
        verify(refreshTokenRepository, never()).findValidToken(anyString(), any());
    }
    
    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken_WhenTokenIsExpired() {
        // Arrange
        when(jwtService.validateRefreshToken("expired-token")).thenReturn(true);
        when(refreshTokenRepository.findValidToken(eq("expired-token"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            refreshTokenService.validateAndUseToken("expired-token");
        });
    }
    
    @Test
    @DisplayName("Should revoke individual token")
    void shouldRevokeToken_WhenTokenExists() {
        // Arrange
        String tokenToRevoke = "token-to-revoke-12345678901234567890"; // Token with more than 20 characters
        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .token(tokenToRevoke)
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .used(false)
                .build();
        
        when(refreshTokenRepository.findByToken(tokenToRevoke))
                .thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(token);
        
        // Act
        refreshTokenService.revokeToken(tokenToRevoke, "admin");
        
        // Assert
        assertTrue(token.getRevoked());
        assertNotNull(token.getRevokedAt());
        verify(refreshTokenRepository, times(1)).save(token);
    }
    
    @Test
    @DisplayName("Should revoke all tokens of a user")
    void shouldRevokeAllUserTokens_WhenUserProvided() {
        // Arrange
        when(refreshTokenRepository.revokeAllUserTokens(eq(testUser), any(LocalDateTime.class), anyString()))
                .thenReturn(3);
        
        // Act
        refreshTokenService.revokeAllUserTokens(testUser, "admin");
        
        // Assert
        verify(refreshTokenRepository, times(1))
                .revokeAllUserTokens(eq(testUser), any(LocalDateTime.class), eq("admin"));
    }
    
    @Test
    @DisplayName("Should count valid tokens of a user")
    void shouldCountValidTokens_WhenUserProvided() {
        // Arrange
        when(refreshTokenRepository.countValidTokensByUser(eq(testUser), any(LocalDateTime.class)))
                .thenReturn(2L);
        
        // Act
        long count = refreshTokenService.countValidTokens(testUser);
        
        // Assert
        assertEquals(2L, count);
        verify(refreshTokenRepository, times(1))
                .countValidTokensByUser(eq(testUser), any(LocalDateTime.class));
    }
}
