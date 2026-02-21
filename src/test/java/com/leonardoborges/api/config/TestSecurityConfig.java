package com.leonardoborges.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that provides a permissive security setup for testing.
 * This configuration takes precedence over SecurityConfig in test environments.
 * 
 * This approach is necessary because:
 * - We need Spring Security beans (PasswordEncoder, UserDetailsService) available
 * - But we want to disable security enforcement for easier testing
 * - SecurityConfig is excluded via @Profile("!test")
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
    
    @Bean
    @Primary
    @Order(-100)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
