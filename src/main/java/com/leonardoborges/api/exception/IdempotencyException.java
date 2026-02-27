package com.leonardoborges.api.exception;

public class IdempotencyException extends BusinessException {
    
    public IdempotencyException(String message) {
        super(message);
    }
    
    public IdempotencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
