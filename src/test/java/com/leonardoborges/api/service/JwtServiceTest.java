package com.leonardoborges.api.service;

import com.leonardoborges.api.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private String validSecret;

    @BeforeEach
    void setUp() {
        validSecret = "test-secret-key-for-testing-purposes-only-minimum-32-chars";
        lenient().when(jwtProperties.getSecret()).thenReturn(validSecret);
        lenient().when(jwtProperties.getExpiration()).thenReturn(86400000L); // 24 hours
        lenient().when(jwtProperties.getRefreshExpiration()).thenReturn(604800000L); // 7 days

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    @DisplayName("Should generate token successfully")
    void shouldGenerateTokenSuccessfully() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void shouldGenerateRefreshTokenSuccessfully() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    @DisplayName("Should generate token with extra claims")
    void shouldGenerateTokenWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");

        String token = jwtService.generateToken(userDetails, extraClaims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationDateFromToken() {
        String token = jwtService.generateToken(userDetails);
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should identify refresh token correctly")
    void shouldIdentifyRefreshTokenCorrectly() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        boolean isRefresh = jwtService.isRefreshToken(refreshToken);

        assertTrue(isRefresh);
    }

    @Test
    @DisplayName("Should return false for normal access token")
    void shouldReturnFalseForNormalAccessToken() {
        String accessToken = jwtService.generateToken(userDetails);
        boolean isRefresh = jwtService.isRefreshToken(accessToken);

        assertFalse(isRefresh);
    }

    @Test
    @DisplayName("Should validate valid refresh token")
    void shouldValidateValidRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        boolean isValid = jwtService.validateRefreshToken(refreshToken);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should throw exception when secret is null")
    void shouldThrowExceptionWhenSecretIsNull() {
        when(jwtProperties.getSecret()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> {
            jwtService.generateToken(userDetails);
        });
    }

    @Test
    @DisplayName("Should throw exception when secret is too short")
    void shouldThrowExceptionWhenSecretIsTooShort() {
        when(jwtProperties.getSecret()).thenReturn("short");

        assertThrows(IllegalStateException.class, () -> {
            jwtService.generateToken(userDetails);
        });
    }

    @Test
    @DisplayName("Should extract custom claim from token")
    void shouldExtractCustomClaimFromToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        String token = jwtService.generateToken(userDetails, extraClaims);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("Should return false when isRefreshToken encounters exception")
    void shouldReturnFalse_WhenIsRefreshTokenEncountersException() {
        String invalidToken = "invalid.token.here";

        boolean result = jwtService.isRefreshToken(invalidToken);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when validateRefreshToken encounters exception")
    void shouldReturnFalse_WhenValidateRefreshTokenEncountersException() {
        String invalidToken = "invalid.token.here";

        boolean result = jwtService.validateRefreshToken(invalidToken);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when validateRefreshToken token is expired")
    void shouldReturnFalse_WhenValidateRefreshTokenTokenIsExpired() {
        when(jwtProperties.getRefreshExpiration()).thenReturn(-1000L);
        String expiredToken = jwtService.generateRefreshToken(userDetails);

        boolean result = jwtService.validateRefreshToken(expiredToken);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when token is not a refresh token but validateRefreshToken is called")
    void shouldReturnFalse_WhenTokenIsNotRefreshTokenButValidateRefreshTokenIsCalled() {
        String accessToken = jwtService.generateToken(userDetails);

        boolean result = jwtService.validateRefreshToken(accessToken);

        assertFalse(result);
    }
}
