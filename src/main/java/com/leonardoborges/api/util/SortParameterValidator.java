package com.leonardoborges.api.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Utility class for validating and normalizing sort parameters.
 * Prevents invalid sort fields from causing errors and SQL injection.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SortParameterValidator {
    
    private final SqlInjectionValidator sqlInjectionValidator;
    
    private static final Set<String> VALID_TASK_SORT_FIELDS = Set.of(
            "id", "title", "description", "status", "priority", 
            "createdAt", "updatedAt", "version"
    );
    
    /**
     * Validates and normalizes sort parameters for Task entities.
     * Returns a valid Pageable with default sort if invalid fields are detected.
     * 
     * @param pageable The original Pageable with potentially invalid sort
     * @param defaultSortField The default sort field to use if validation fails
     * @param defaultDirection The default sort direction (ASC or DESC)
     * @return A valid Pageable with corrected sort parameters
     */
    public Pageable validateAndNormalizeTaskSort(Pageable pageable, String defaultSortField, Sort.Direction defaultDirection) {
        if (pageable == null || !pageable.getSort().isSorted()) {
            return pageable;
        }
        
        try {
            boolean hasInvalidSort = pageable.getSort().stream()
                    .anyMatch(order -> {
                        String property = order.getProperty();
                        // Check for SQL injection first
                        if (!sqlInjectionValidator.isValidSortField(property)) {
                            log.warn("Invalid sort field detected (potential SQL injection): {}", property);
                            return true;
                        }
                        // Then check if it's a valid field
                        return !VALID_TASK_SORT_FIELDS.contains(property);
                    });
            
            if (hasInvalidSort) {
                log.warn("Invalid sort parameter detected, using default sort: {} {}", 
                        defaultSortField, defaultDirection);
                return PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(defaultDirection, defaultSortField)
                );
            }
            
            return pageable;
        } catch (Exception e) {
            log.warn("Error validating sort parameters, using default: {}", e.getMessage());
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(defaultDirection, defaultSortField)
            );
        }
    }
    
    /**
     * Validates that a sort field is allowed for Task entities.
     * 
     * @param field The sort field to validate
     * @return true if the field is valid, false otherwise
     */
    public boolean isValidTaskSortField(String field) {
        if (field == null) {
            return false;
        }
        // Check for SQL injection first
        if (!sqlInjectionValidator.isValidSortField(field)) {
            return false;
        }
        return VALID_TASK_SORT_FIELDS.contains(field);
    }
}
