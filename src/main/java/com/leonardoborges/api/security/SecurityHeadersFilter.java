package com.leonardoborges.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to add security headers to all HTTP responses.
 * Implements OWASP security best practices.
 */
@Component
@Order(0)
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String X_FRAME_OPTIONS = "X-Frame-Options";
    private static final String X_XSS_PROTECTION = "X-XSS-Protection";
    private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    private static final String REFERRER_POLICY = "Referrer-Policy";
    private static final String PERMISSIONS_POLICY = "Permissions-Policy";
    private static final String X_PERMITTED_CROSS_DOMAIN_POLICIES = "X-Permitted-Cross-Domain-Policies";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Prevent MIME type sniffing
        response.setHeader(X_CONTENT_TYPE_OPTIONS, "nosniff");

        // Prevent clickjacking
        response.setHeader(X_FRAME_OPTIONS, "DENY");

        // Enable XSS protection (legacy browsers)
        response.setHeader(X_XSS_PROTECTION, "1; mode=block");

        // HSTS - Force HTTPS (only in production)
        if (request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))) {
            response.setHeader(STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains; preload");
        }

        // Content Security Policy
        response.setHeader(CONTENT_SECURITY_POLICY, 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none';");

        // Referrer Policy
        response.setHeader(REFERRER_POLICY, "strict-origin-when-cross-origin");

        // Permissions Policy (formerly Feature-Policy)
        response.setHeader(PERMISSIONS_POLICY, 
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "gyroscope=(), " +
                "speaker=()");

        // Cross-domain policies
        response.setHeader(X_PERMITTED_CROSS_DOMAIN_POLICIES, "none");

        // Remove server header (if possible)
        // Note: This might need to be configured at server level (Tomcat/Nginx)

        filterChain.doFilter(request, response);
    }
}
