package com.leonardoborges.api.validation;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.model.Task;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskRequestValidator Tests")
class TaskRequestValidatorTest {
    
    private TaskRequestValidator validator;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;
    
    @BeforeEach
    void setUp() {
        validator = new TaskRequestValidator();
        validator.initialize(mock(ValidTaskRequest.class));
        
        lenient().doNothing().when(context).disableDefaultConstraintViolation();
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
        lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }
    
    @Test
    void shouldReturnTrueForNullRequest() {
        assertTrue(validator.isValid(null, context));
    }
    
    @Test
    void shouldReturnTrueForValidRequest() {
        TaskRequest request = TaskRequest.builder()
                .title("Valid Title")
                .description("Valid Description")
                .status(Task.TaskStatus.PENDING)
                .priority(5)
                .build();
        
        assertTrue(validator.isValid(request, context));
    }
    
    @Test
    void shouldReturnFalseForCompletedTaskWithHighPriority() {
        TaskRequest request = TaskRequest.builder()
                .title("Completed Task")
                .status(Task.TaskStatus.COMPLETED)
                .priority(8)
                .build();
        
        assertFalse(validator.isValid(request, context));
        verify(context).buildConstraintViolationWithTemplate("Completed tasks should not have high priority");
    }
    
    @Test
    void shouldReturnTrueForCompletedTaskWithLowPriority() {
        TaskRequest request = TaskRequest.builder()
                .title("Completed Task")
                .status(Task.TaskStatus.COMPLETED)
                .priority(3)
                .build();
        
        assertTrue(validator.isValid(request, context));
    }
    
    @Test
    void shouldReturnFalseWhenTitleEqualsDescription() {
        TaskRequest request = TaskRequest.builder()
                .title("Same Text")
                .description("Same Text")
                .status(Task.TaskStatus.PENDING)
                .priority(5)
                .build();
        
        assertFalse(validator.isValid(request, context));
        verify(context).buildConstraintViolationWithTemplate("Task title and description should be different");
    }
    
    @Test
    void shouldReturnFalseForNegativePriority() {
        TaskRequest request = TaskRequest.builder()
                .title("Valid Title")
                .priority(-1)
                .build();
        
        assertFalse(validator.isValid(request, context));
        verify(violationBuilder).addPropertyNode("priority");
    }
    
    @Test
    void shouldReturnFalseForPriorityAboveMax() {
        TaskRequest request = TaskRequest.builder()
                .title("Valid Title")
                .priority(11)
                .build();
        
        assertFalse(validator.isValid(request, context));
        verify(violationBuilder).addPropertyNode("priority");
    }
    
    @Test
    void shouldReturnTrueForValidPriorityRange() {
        TaskRequest request = TaskRequest.builder()
                .title("Valid Title")
                .priority(5)
                .build();
        
        assertTrue(validator.isValid(request, context));
    }
}
