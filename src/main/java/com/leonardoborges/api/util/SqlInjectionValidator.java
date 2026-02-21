package com.leonardoborges.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for SQL injection prevention.
 * Validates input to prevent SQL injection attacks.
 * 
 * Note: This is a defense-in-depth measure. JPA/Hibernate already provides
 * protection through parameterized queries, but this adds an extra layer.
 */
@Component
@Slf4j
public class SqlInjectionValidator {
    
    // Patterns for common SQL injection attempts
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|onerror|onload)" +
            ".*(from|into|table|database|schema|procedure|function|trigger|view|index|set|where)" +
            "|('|(\\-\\-)|(;)|(\\|\\|)|(\\+)|(/\\*)|(\\*/)|(xp_)|(sp_))" +
            "|(or|and).*=.*=" +
            "|(or|and).*'1'='1'" +
            "|(or|and).*'1'='1'" +
            "|(\\bor\\b.*\\d+.*=.*\\d+)" +
            "|(\\band\\b.*\\d+.*=.*\\d+)" +
            "|(update.*set)" +
            "|(delete.*from)"
    );
    
    private static final Pattern DANGEROUS_CHARS = Pattern.compile(
            "[;'\"\\\\]|(--)|(/\\*)|(\\*/)|(xp_)|(sp_)"
    );
    
    /**
     * Validates that a string does not contain SQL injection patterns.
     * 
     * @param input The input string to validate
     * @return true if input is safe, false if potential SQL injection detected
     */
    public boolean isSafe(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            log.warn("Potential SQL injection detected in input: {}", sanitizeForLog(input));
            return false;
        }
        
        // Check for dangerous characters in suspicious contexts
        if (DANGEROUS_CHARS.matcher(input).find() && input.length() > 10) {
            // Allow short strings with quotes (might be legitimate)
            log.debug("Dangerous characters detected in input, but length is acceptable");
        }
        
        return true;
    }
    
    /**
     * Validates and sanitizes a string, removing potential SQL injection patterns.
     * 
     * @param input The input string
     * @return Sanitized string, or null if input is null
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        
        if (!isSafe(input)) {
            log.warn("Input contained SQL injection patterns, sanitizing: {}", sanitizeForLog(input));
            // Remove dangerous patterns
            String sanitized = SQL_INJECTION_PATTERN.matcher(input).replaceAll("");
            return sanitized.trim();
        }
        
        return input;
    }
    
    /**
     * Validates a sort field name to prevent SQL injection.
     * Only allows alphanumeric characters and underscores.
     * 
     * @param sortField The sort field name
     * @return true if valid, false otherwise
     */
    public boolean isValidSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return false;
        }
        
        // Only allow alphanumeric and underscore
        return sortField.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * Validates a table/column name to prevent SQL injection.
     * 
     * @param name The name to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidIdentifier(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Only allow alphanumeric and underscore, must start with letter
        return name.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }
    
    /**
     * Sanitizes a string for logging (removes sensitive patterns).
     * 
     * @param input The input to sanitize
     * @return Sanitized string safe for logging
     */
    private String sanitizeForLog(String input) {
        if (input == null) {
            return "null";
        }
        if (input.length() > 100) {
            return input.substring(0, 100) + "...";
        }
        return input.replaceAll("[\r\n]", "");
    }
}
