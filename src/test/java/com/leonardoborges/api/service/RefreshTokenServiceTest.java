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
 * Testes unitários para RefreshTokenService.
 * 
 * Usa @ExtendWith(MockitoExtension.class) para testes puros com mocks.
 * Não carrega contexto Spring, tornando os testes mais rápidos e isolados.
 * 
 * Boas práticas aplicadas:
 * - Testes isolados e independentes
 * - Uso de builders para criar objetos de teste
 * - Nomes descritivos de testes com @DisplayName
 * - Verificação de comportamento (verify)
 * - Testes de casos de sucesso e erro
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
    @DisplayName("Deve criar refresh token com sucesso")
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
    @DisplayName("Deve validar e marcar token como usado")
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
    @DisplayName("Deve rejeitar token inválido")
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
    @DisplayName("Deve rejeitar token expirado")
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
    @DisplayName("Deve revogar token individual")
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
    @DisplayName("Deve revogar todos os tokens de um usuário")
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
    @DisplayName("Deve contar tokens válidos de um usuário")
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
