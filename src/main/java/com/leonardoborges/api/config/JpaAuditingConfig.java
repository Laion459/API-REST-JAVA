package com.leonardoborges.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class for JPA Auditing.
 * 
 * Separated from the main application class to allow selective exclusion
 * in tests that don't need database access (e.g., @WebMvcTest).
 * 
 * Best practices applied:
 * 1. Single Responsibility: Only handles JPA Auditing configuration
 * 2. Separation of Concerns: Isolated from application bootstrap
 * 3. Testability: Can be easily excluded in test contexts
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing configuration
}
