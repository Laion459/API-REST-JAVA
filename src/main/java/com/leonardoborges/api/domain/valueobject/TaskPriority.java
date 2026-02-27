package com.leonardoborges.api.domain.valueobject;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.exception.ValidationException;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Value Object representing a Task Priority.
 * Encapsulates business rules for task priorities following Domain-Driven Design principles.
 */
@EqualsAndHashCode
public final class TaskPriority implements Serializable, Comparable<TaskPriority> {
    
    private static final long serialVersionUID = 1L;
    private static final int MIN_PRIORITY = 0;
    private static final int MAX_PRIORITY = 100;
    
    private final int value;
    
    private TaskPriority(int value) {
        this.value = value;
    }
    
    /**
     * Creates a TaskPriority from an integer value.
     * Validates the priority according to business rules.
     * 
     * @param value The priority value
     * @return A valid TaskPriority instance
     * @throws ValidationException if the priority is invalid
     */
    public static TaskPriority of(int value) {
        validate(value);
        return new TaskPriority(value);
    }
    
    /**
     * Creates a TaskPriority with the default priority.
     * 
     * @return A TaskPriority with default value
     */
    public static TaskPriority defaultPriority() {
        return new TaskPriority(TaskConstants.DEFAULT_PRIORITY);
    }
    
    /**
     * Creates a TaskPriority from an Integer (nullable).
     * Returns default priority if null.
     * 
     * @param value The priority value (nullable)
     * @return A valid TaskPriority instance
     */
    public static TaskPriority ofNullable(Integer value) {
        if (value == null) {
            return defaultPriority();
        }
        return of(value);
    }
    
    /**
     * Validates the priority according to business rules.
     */
    private static void validate(int value) {
        if (value < MIN_PRIORITY) {
            throw new ValidationException(
                    String.format("Task priority cannot be negative (minimum: %d)", MIN_PRIORITY));
        }
        
        if (value > MAX_PRIORITY) {
            throw new ValidationException(
                    String.format("Task priority cannot exceed %d (maximum: %d)", MAX_PRIORITY, MAX_PRIORITY));
        }
    }
    
    /**
     * Returns the integer value of the priority.
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Checks if this priority is higher than another.
     */
    public boolean isHigherThan(TaskPriority other) {
        return this.value > other.value;
    }
    
    /**
     * Checks if this priority is lower than another.
     */
    public boolean isLowerThan(TaskPriority other) {
        return this.value < other.value;
    }
    
    @Override
    public int compareTo(TaskPriority other) {
        return Integer.compare(this.value, other.value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
