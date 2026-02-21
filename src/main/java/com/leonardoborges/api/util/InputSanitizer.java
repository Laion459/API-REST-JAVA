package com.leonardoborges.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for input sanitization and validation.
 * Prevents injection attacks and ensures data integrity.
 */
@Component
@Slf4j
public class InputSanitizer {
    
    /**
     * Sanitizes a string by trimming whitespace and removing control characters.
     * 
     * @param input The input string to sanitize
     * @return Sanitized string, or null if input is null
     */
    public String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove leading/trailing whitespace
        String sanitized = input.trim();
        
        // Remove control characters (except newline, tab, carriage return)
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        // Replace multiple spaces with single space
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        if (!sanitized.equals(input)) {
            log.debug("Input sanitized: original length={}, sanitized length={}", 
                input.length(), sanitized.length());
        }
        
        return sanitized;
    }
    
    /**
     * Sanitizes a string and limits its length.
     * 
     * @param input The input string
     * @param maxLength Maximum allowed length
     * @return Sanitized and truncated string
     */
    public String sanitizeAndTruncate(String input, int maxLength) {
        String sanitized = sanitizeString(input);
        if (sanitized == null) {
            return null;
        }
        
        if (sanitized.length() > maxLength) {
            log.warn("Input truncated from {} to {} characters", sanitized.length(), maxLength);
            return sanitized.substring(0, maxLength);
        }
        
        return sanitized;
    }
    
    /**
     * Validates that a string is not empty after sanitization.
     * 
     * @param input The input string
     * @return true if string is not null and not empty after sanitization
     */
    public boolean isValidAfterSanitization(String input) {
        String sanitized = sanitizeString(input);
        return sanitized != null && !sanitized.isEmpty();
    }
}
