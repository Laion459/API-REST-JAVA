package com.leonardoborges.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that provides a permissive security setup for testing.
 * 
 * Best practices applied:
 * 1. Uses @Profile("test") to ensure it only loads in test environment
 * 2. Uses @Primary and @Order(-1) to override production SecurityConfig
 * 3. Provides a simple, permissive security chain for tests
 * 4. Uses @EnableWebSecurity because production SecurityConfig has @Profile("!test")
 * 5. Only overrides SecurityFilterChain to be permissive for tests
 * 
 * This approach follows Spring Boot best practices:
 * - Production components use @Profile("!test") to exclude themselves from tests
 * - Test configuration uses @Profile("test") to only load in tests
 * - Only overrides what's necessary (SecurityFilterChain)
 * - Maintains security context for @WithMockUser to work properly
 */
@TestConfiguration
@Profile("test")
@EnableWebSecurity
@EnableMethodSecurity
@Import({TestDataSourceConfig.class, TestR2dbcConfig.class, TestCacheConfig.class})
public class TestSecurityConfig {
    
    @Bean
    @Primary
    @Order(-1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
