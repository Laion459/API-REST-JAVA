package com.leonardoborges.api.service;

import com.leonardoborges.api.exception.ValidationException;
import com.leonardoborges.api.util.InputSanitizer;
import com.leonardoborges.api.util.SqlInjectionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Base validation service providing common validation logic.
 * Reduces code duplication across validation services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaseValidationService {
    
    private final SqlInjectionValidator sqlInjectionValidator;
    private final InputSanitizer inputSanitizer;
    
    /**
     * Validates that a string field is not null or empty.
     * 
     * @param value The value to validate
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if the value is null or empty
     */
    public void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Validates that a string field is not null or empty (nullable version).
     * 
     * @param value The value to validate
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if the value is empty (but allows null)
     */
    public void validateNotEmptyIfPresent(String value, String fieldName) {
        if (value != null && value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
    
    /**
     * Validates SQL injection safety for a string field.
     * 
     * @param value The value to validate
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if SQL injection is detected
     */
    public void validateSqlInjectionSafe(String value, String fieldName) {
        if (value != null && !sqlInjectionValidator.isSafe(value)) {
            throw new ValidationException("Invalid input detected in " + fieldName + " field");
        }
    }
    
    /**
     * Sanitizes and truncates a string field.
     * 
     * @param value The value to sanitize
     * @param maxLength The maximum length
     * @return The sanitized and truncated value
     */
    public String sanitizeAndTruncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return inputSanitizer.sanitizeAndTruncate(value, maxLength);
    }
    
    /**
     * Validates that a numeric value is not negative.
     * 
     * @param value The value to validate
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if the value is negative
     */
    public void validateNotNegative(Integer value, String fieldName) {
        if (value != null && value < 0) {
            throw new ValidationException(fieldName + " cannot be negative");
        }
    }
    
    /**
     * Validates that a numeric value is within a range.
     * 
     * @param value The value to validate
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive)
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if the value is out of range
     */
    public void validateInRange(Integer value, int min, int max, String fieldName) {
        if (value != null && (value < min || value > max)) {
            throw new ValidationException(
                    String.format("%s must be between %d and %d (inclusive)", fieldName, min, max));
        }
    }
    
    /**
     * Validates that a string length is within a range.
     * 
     * @param value The value to validate
     * @param minLength The minimum length (inclusive)
     * @param maxLength The maximum length (inclusive)
     * @param fieldName The name of the field (for error messages)
     * @throws ValidationException if the length is out of range
     */
    public void validateLength(String value, int minLength, int maxLength, String fieldName) {
        if (value == null) {
            return;
        }
        
        int length = value.length();
        if (length < minLength || length > maxLength) {
            throw new ValidationException(
                    String.format("%s must be between %d and %d characters", fieldName, minLength, maxLength));
        }
    }
}
