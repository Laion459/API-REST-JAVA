package com.leonardoborges.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test configuration for MVC tests (@WebMvcTest ONLY).
 * 
 * IMPORTANT: This configuration is ONLY for @WebMvcTest, NOT for @SpringBootTest.
 * 
 * Best practices applied:
 * 1. Uses @Profile("test") to ensure it only loads in test environment
 * 2. Provides mock beans for services required by controllers in @WebMvcTest
 * 3. Imports TestWebConfig to provide test-specific web configuration
 * 4. Does NOT use ComponentScan - @WebMvcTest handles component scanning
 * 5. Follows Spring Boot testing best practices
 * 
 * Note: 
 * - For @SpringBootTest: Use TestSecurityConfig directly (it doesn't import this)
 * - For @WebMvcTest: Import this config in the test class
 * - Production components with @Profile("!test") are automatically excluded
 */
@TestConfiguration
@Profile("test")
@Import(TestWebConfig.class)
public class TestMvcConfig {
    
    @MockBean
    private UserDetailsService userDetailsService;
    
    @MockBean
    private PasswordEncoder passwordEncoder;
}
