package com.leonardoborges.api.exception;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessException extends BaseException {
    
    private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";
    
    public BusinessException(String message) {
        super(message, ERROR_CODE);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
