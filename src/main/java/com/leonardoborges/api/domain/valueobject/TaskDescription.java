package com.leonardoborges.api.domain.valueobject;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.exception.ValidationException;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Value Object representing a Task Description.
 * Encapsulates business rules for task descriptions following Domain-Driven Design principles.
 */
@EqualsAndHashCode
public final class TaskDescription implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final String value;
    
    private TaskDescription(String value) {
        this.value = value;
    }
    
    /**
     * Creates a TaskDescription from a string value.
     * Validates the description according to business rules.
     * 
     * @param value The description string (nullable)
     * @return A valid TaskDescription instance, or null if value is null/empty
     */
    public static TaskDescription of(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        validate(value);
        return new TaskDescription(value.trim());
    }
    
    /**
     * Validates the description according to business rules.
     */
    private static void validate(String value) {
        if (value != null && value.length() > TaskConstants.DESCRIPTION_MAX_LENGTH) {
            throw new ValidationException(
                    String.format("Task description must not exceed %d characters", 
                            TaskConstants.DESCRIPTION_MAX_LENGTH));
        }
    }
    
    /**
     * Returns the string value of the description.
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
