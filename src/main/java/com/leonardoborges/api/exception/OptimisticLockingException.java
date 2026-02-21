package com.leonardoborges.api.exception;

/**
 * Exception thrown when an optimistic locking conflict occurs.
 * This happens when trying to update an entity that has been modified by another transaction.
 */
public class OptimisticLockingException extends BaseException {
    
    private static final String ERROR_CODE = "OPTIMISTIC_LOCKING_ERROR";
    
    public OptimisticLockingException(String message) {
        super(message, ERROR_CODE);
    }
    
    public OptimisticLockingException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
