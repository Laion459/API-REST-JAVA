package com.leonardoborges.api.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter Tests")
class RateLimitFilterTest {

    @Mock
    private Bucket defaultBucket;

    @Mock
    private Bucket authBucket;

    @Mock
    private Bucket adminBucket;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        SecurityContextHolder.clearContext();
        // Create filter manually to ensure mocks are injected in correct order
        rateLimitFilter = new RateLimitFilter(defaultBucket, authBucket, adminBucket);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
        lenient().when(response.getContentType()).thenReturn("application/json");
    }

    @Test
    @DisplayName("Should continue filter chain when rate limit is not exceeded")
    void shouldContinueFilterChainWhenRateLimitIsNotExceeded() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(defaultBucket.tryConsume(1)).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Should return 429 when rate limit is exceeded")
    void shouldReturn429WhenRateLimitIsExceeded() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(defaultBucket.tryConsume(1)).thenReturn(false);
        when(defaultBucket.getAvailableTokens()).thenReturn(0L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("Rate limit exceeded"));
    }

    @Test
    @DisplayName("Should use auth bucket for auth endpoints")
    void shouldUseAuthBucketForAuthEndpoints() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(authBucket.tryConsume(1)).thenReturn(true);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("X-Real-IP")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(authBucket).tryConsume(1);
        verify(defaultBucket, never()).tryConsume(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use admin bucket for admin endpoints when user has ADMIN role")
    void shouldUseAdminBucketForAdminEndpointsWhenUserHasAdminRole() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/cache/stats");
        when(adminBucket.tryConsume(1)).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        Collection<? extends GrantedAuthority> authorities = (Collection<? extends GrantedAuthority>) List.of(adminAuthority);
        doReturn(authorities).when(auth).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(auth);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(adminBucket).tryConsume(1);
        verify(defaultBucket, never()).tryConsume(anyInt());
        verify(authBucket, never()).tryConsume(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use default bucket for admin endpoints when user does not have ADMIN role")
    void shouldUseDefaultBucketForAdminEndpointsWhenUserDoesNotHaveAdminRole() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/cache/stats");
        when(defaultBucket.tryConsume(1)).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        GrantedAuthority userAuthority = new SimpleGrantedAuthority("ROLE_USER");
        Collection<? extends GrantedAuthority> authorities = (Collection<? extends GrantedAuthority>) List.of(userAuthority);
        doReturn(authorities).when(auth).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(auth);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(defaultBucket).tryConsume(1);
        verify(adminBucket, never()).tryConsume(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use default bucket for admin endpoints when no authentication")
    void shouldUseDefaultBucketForAdminEndpointsWhenNoAuthentication() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/cache/stats");
        when(defaultBucket.tryConsume(1)).thenReturn(true);
        SecurityContextHolder.clearContext();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(defaultBucket).tryConsume(1);
        verify(adminBucket, never()).tryConsume(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header")
    void shouldExtractClientIpFromXForwardedForHeader() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        when(defaultBucket.tryConsume(1)).thenReturn(false);
        when(defaultBucket.getAvailableTokens()).thenReturn(0L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(request, atLeastOnce()).getHeader("X-Forwarded-For");
        // The filter should log with the first IP from X-Forwarded-For when rate limit is exceeded
    }

    @Test
    @DisplayName("Should extract client IP from X-Real-IP header when X-Forwarded-For is not present")
    void shouldExtractClientIpFromXRealIpHeaderWhenXForwardedForIsNotPresent() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.2");
        when(defaultBucket.tryConsume(1)).thenReturn(false);
        when(defaultBucket.getAvailableTokens()).thenReturn(0L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(request, atLeastOnce()).getHeader("X-Forwarded-For");
        verify(request, atLeastOnce()).getHeader("X-Real-IP");
    }

    @Test
    @DisplayName("Should use remote address when no proxy headers are present")
    void shouldUseRemoteAddressWhenNoProxyHeadersArePresent() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.3");
        when(defaultBucket.tryConsume(1)).thenReturn(false);
        when(defaultBucket.getAvailableTokens()).thenReturn(0L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(request, atLeastOnce()).getHeader("X-Forwarded-For");
        verify(request, atLeastOnce()).getHeader("X-Real-IP");
        verify(request, atLeastOnce()).getRemoteAddr();
    }

    @Test
    @DisplayName("Should include retryAfter in error response")
    void shouldIncludeRetryAfterInErrorResponse() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(defaultBucket.tryConsume(1)).thenReturn(false);
        when(defaultBucket.getAvailableTokens()).thenReturn(0L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"retryAfter\""));
        assertTrue(responseBody.contains("Rate limit exceeded"));
        verify(request, atLeastOnce()).getHeader("X-Forwarded-For");
        verify(request, atLeastOnce()).getHeader("X-Real-IP");
        verify(request, atLeastOnce()).getRemoteAddr();
    }

    @Test
    @DisplayName("Should return retry after when bucket has available tokens")
    void shouldReturnRetryAfter_WhenBucketHasAvailableTokens() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(defaultBucket.tryConsume(1)).thenReturn(false);
        when(defaultBucket.getAvailableTokens()).thenReturn(5L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"retryAfter\""));
        verify(response).setStatus(429);
    }

    @Test
    @DisplayName("Should handle exception in getRetryAfterSeconds")
    void shouldHandleException_InGetRetryAfterSeconds() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(defaultBucket.tryConsume(1)).thenReturn(false);
        when(defaultBucket.getAvailableTokens()).thenReturn(0L);
        when(defaultBucket.tryConsume(0)).thenThrow(new RuntimeException("Bucket error"));

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"retryAfter\""));
        verify(response).setStatus(429);
    }
}
