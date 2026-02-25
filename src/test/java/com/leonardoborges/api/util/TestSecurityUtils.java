package com.leonardoborges.api.util;

import com.leonardoborges.api.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for setting up security context in tests.
 * Provides methods to configure authentication for testing purposes.
 */
public class TestSecurityUtils {
    
    /**
     * Sets up a security context with a mock user for testing.
     * This is the proper way to test security-aware code without mocking SecurityUtils.
     * 
     * @param user The user to authenticate
     */
    public static void setSecurityContext(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toList()))
                .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
    
    /**
     * Sets up a security context with a username and roles.
     * 
     * @param username The username
     * @param roles The roles
     */
    public static void setSecurityContext(String username, String... roles) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .authorities(Set.of(roles).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()))
                .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
    
    /**
     * Clears the security context.
     * Should be called in @AfterEach to avoid test pollution.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
