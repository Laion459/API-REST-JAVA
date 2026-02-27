package com.leonardoborges.api.domain.valueobject;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.exception.ValidationException;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Value Object representing a Task Title.
 * Encapsulates business rules for task titles following Domain-Driven Design principles.
 */
@EqualsAndHashCode
public final class TaskTitle implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final String value;
    
    private TaskTitle(String value) {
        this.value = value;
    }
    
    /**
     * Creates a TaskTitle from a string value.
     * Validates the title according to business rules.
     * 
     * @param value The title string
     * @return A valid TaskTitle instance
     * @throws ValidationException if the title is invalid
     */
    public static TaskTitle of(String value) {
        validate(value);
        return new TaskTitle(value.trim());
    }
    
    /**
     * Validates the title according to business rules.
     */
    private static void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Task title cannot be null or empty");
        }
        
        String trimmed = value.trim();
        if (trimmed.length() < TaskConstants.TITLE_MIN_LENGTH) {
            throw new ValidationException(
                    String.format("Task title must be at least %d characters long", 
                            TaskConstants.TITLE_MIN_LENGTH));
        }
        
        if (trimmed.length() > TaskConstants.TITLE_MAX_LENGTH) {
            throw new ValidationException(
                    String.format("Task title must not exceed %d characters", 
                            TaskConstants.TITLE_MAX_LENGTH));
        }
    }
    
    /**
     * Returns the string value of the title.
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
