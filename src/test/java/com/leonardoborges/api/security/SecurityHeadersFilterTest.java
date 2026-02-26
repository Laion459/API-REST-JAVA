package com.leonardoborges.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityHeadersFilter Tests")
class SecurityHeadersFilterTest {

    @InjectMocks
    private SecurityHeadersFilter securityHeadersFilter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Should add all security headers to response")
    void shouldAddAllSecurityHeadersToResponse() throws ServletException, IOException {
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
        verify(response).setHeader(eq("Content-Security-Policy"), argThat(header -> header.contains("default-src 'self'")));
        verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        verify(response).setHeader(eq("Permissions-Policy"), argThat(header -> header.contains("geolocation=()")));
        verify(response).setHeader("X-Permitted-Cross-Domain-Policies", "none");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should add HSTS header when request is secure")
    void shouldAddHstsHeaderWhenRequestIsSecure() throws ServletException, IOException {
        when(request.isSecure()).thenReturn(true);

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should add HSTS header when X-Forwarded-Proto is https")
    void shouldAddHstsHeaderWhenXForwardedProtoIsHttps() throws ServletException, IOException {
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn("https");

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not add HSTS header when request is not secure and no X-Forwarded-Proto")
    void shouldNotAddHstsHeaderWhenRequestIsNotSecureAndNoXForwardedProto() throws ServletException, IOException {
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response, never()).setHeader(eq("Strict-Transport-Security"), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should add Content-Security-Policy with correct directives")
    void shouldAddContentSecurityPolicyWithCorrectDirectives() throws ServletException, IOException {
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("Content-Security-Policy"), argThat(header -> 
                header.contains("default-src 'self'") &&
                header.contains("script-src 'self' 'unsafe-inline' 'unsafe-eval'") &&
                header.contains("style-src 'self' 'unsafe-inline'") &&
                header.contains("img-src 'self' data: https:") &&
                header.contains("frame-ancestors 'none'")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should add Permissions-Policy with correct restrictions")
    void shouldAddPermissionsPolicyWithCorrectRestrictions() throws ServletException, IOException {
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("Permissions-Policy"), argThat(header -> 
                header.contains("geolocation=()") &&
                header.contains("microphone=()") &&
                header.contains("camera=()") &&
                header.contains("payment=()")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should continue filter chain after adding headers")
    void shouldContinueFilterChainAfterAddingHeaders() throws ServletException, IOException {
        when(request.isSecure()).thenReturn(false);
        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
