package com.leonardoborges.api.security;

import com.leonardoborges.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private com.leonardoborges.api.service.TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;
    private String validToken = "valid-jwt-token";
    private String bearerToken = "Bearer " + validToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("encodedPassword")
                .roles("USER")
                .build();
    }

    @Test
    @DisplayName("Should continue filter chain when no Authorization header")
    void shouldContinueFilterChainWhenNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header does not start with Bearer")
    void shouldContinueFilterChainWhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Invalid token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should set authentication when valid JWT token is provided")
    void shouldSetAuthenticationWhenValidJwtTokenIsProvided() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.validateToken(validToken, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtService).validateToken(validToken, userDetails);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("Should not set authentication when username is null")
    void shouldNotSetAuthenticationWhenUsernameIsNull() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should not set authentication when authentication already exists")
    void shouldNotSetAuthenticationWhenAuthenticationAlreadyExists() throws ServletException, IOException {
        org.springframework.security.core.Authentication existingAuth = mock(org.springframework.security.core.Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should not set authentication when token is invalid")
    void shouldNotSetAuthenticationWhenTokenIsInvalid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.validateToken(validToken, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtService).validateToken(validToken, userDetails);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should continue filter chain when exception occurs during authentication")
    void shouldContinueFilterChainWhenExceptionOccursDuringAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenThrow(new RuntimeException("Token parsing error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should extract JWT token correctly from Bearer header")
    void shouldExtractJwtTokenCorrectlyFromBearerHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(jwtService.extractUsername(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.validateToken(validToken, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUsername(validToken);
        verify(jwtService, never()).extractUsername(bearerToken);
    }
}
