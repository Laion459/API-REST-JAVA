package com.leonardoborges.api.validation;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.model.Task;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskRequestValidator implements ConstraintValidator<ValidTaskRequest, TaskRequest> {
    
    @Override
    public void initialize(ValidTaskRequest constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(TaskRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        
        boolean isValid = true;
        context.disableDefaultConstraintViolation();
        
        if (request.getStatus() != null && request.getPriority() != null) {
            if (request.getStatus() == Task.TaskStatus.COMPLETED && request.getPriority() > 5) {
                context.buildConstraintViolationWithTemplate(
                    "Completed tasks should not have high priority")
                    .addConstraintViolation();
                isValid = false;
            }
        }
        
        if (request.getTitle() != null && request.getDescription() != null) {
            if (request.getTitle().equals(request.getDescription())) {
                context.buildConstraintViolationWithTemplate(
                    "Task title and description should be different")
                    .addConstraintViolation();
                isValid = false;
            }
        }
        
        if (request.getPriority() != null && request.getPriority() < 0) {
            context.buildConstraintViolationWithTemplate(
                "Priority cannot be negative")
                .addPropertyNode("priority")
                .addConstraintViolation();
            isValid = false;
        }
        
        if (request.getPriority() != null && request.getPriority() > 10) {
            context.buildConstraintViolationWithTemplate(
                "Priority cannot exceed 10")
                .addPropertyNode("priority")
                .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
}
