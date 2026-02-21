package com.leonardoborges.api.exception;

/**
 * Generic exception for resources not found.
 * Can be used for any resource type.
 */
public class ResourceNotFoundException extends BaseException {
    
    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
    
    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s not found with id: %d", resourceType, id), ERROR_CODE);
    }
    
    public ResourceNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
