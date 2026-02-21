package com.leonardoborges.api.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
@org.springframework.context.annotation.Profile("!test")
public class RateLimitFilter extends OncePerRequestFilter {

    private final Bucket defaultBucket;
    private final Bucket authBucket;
    private final Bucket adminBucket;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Bucket bucket = resolveBucket(request);
        
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for {} from IP: {}", 
                    request.getRequestURI(), getClientIpAddress(request));
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format(
                            "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"retryAfter\":%d}",
                            getRetryAfterSeconds(bucket)
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Bucket resolveBucket(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Auth endpoints have stricter limits
        if (path.startsWith("/api/v1/auth/")) {
            return authBucket;
        }
        
        // Admin endpoints have higher limits
        if (path.startsWith("/api/v1/cache/")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && hasAdminRole(auth)) {
                return adminBucket;
            }
        }
        
        return defaultBucket;
    }

    private boolean hasAdminRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private long getRetryAfterSeconds(Bucket bucket) {
        if (bucket == null) {
            return 60;
        }
        
        long availableTokens = bucket.getAvailableTokens();
        if (availableTokens > 0) {
            return 0;
        }
        
        long refillPeriodSeconds = 60;
        long estimatedWaitTime = refillPeriodSeconds;
        
        try {
            if (bucket.tryConsume(0)) {
                return 0;
            }
        } catch (Exception e) {
            log.debug("Error checking bucket availability: {}", e.getMessage());
        }
        
        return estimatedWaitTime;
    }
}
