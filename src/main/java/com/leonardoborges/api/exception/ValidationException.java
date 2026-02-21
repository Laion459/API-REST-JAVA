package com.leonardoborges.api.exception;

import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends BaseException {
    
    private static final String ERROR_CODE = "VALIDATION_ERROR";
    private final Map<String, String> validationErrors;
    
    public ValidationException(String message) {
        super(message, ERROR_CODE);
        this.validationErrors = null;
    }
    
    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message, ERROR_CODE);
        this.validationErrors = validationErrors;
    }
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
