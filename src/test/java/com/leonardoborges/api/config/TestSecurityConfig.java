package com.leonardoborges.api.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test security configuration marker class.
 * 
 * Spring Security web autoconfiguration is excluded in application-test.yml,
 * which is the recommended Spring Boot best practice for tests that don't need
 * security. This class serves as a marker for test imports.
 * 
 * Benefits of this approach:
 * - Simpler: No need to configure a full SecurityFilterChain for tests
 * - Faster: Tests run faster without security overhead
 * - Cleaner: Follows Spring Boot's recommended testing patterns
 * - Maintainable: Less test-specific security code to maintain
 * 
 * For tests that need security, use @WithMockUser or @WithUserDetails annotations.
 */
@TestConfiguration
public class TestSecurityConfig {
    // Empty - security is disabled via application-test.yml exclusions
    // This follows Spring Boot best practices for test configuration
}
