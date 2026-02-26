package com.leonardoborges.api.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
    }

    @Test
    @DisplayName("Should handle TaskNotFoundException correctly")
    void shouldHandleTaskNotFoundException() {
        TaskNotFoundException ex = new TaskNotFoundException(1L);
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTaskNotFoundException(ex, request);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task Not Found", response.getBody().getError());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException correctly")
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource Not Found", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle ValidationException correctly")
    void shouldHandleValidationException() {
        ValidationException ex = new ValidationException("Validation failed");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Validation Failed", response.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle BusinessException correctly")
    void shouldHandleBusinessException() {
        BusinessException ex = new BusinessException("Business rule violated");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex, request);
        
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Business Rule Violation", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle OptimisticLockingException correctly")
    void shouldHandleOptimisticLockingException() {
        OptimisticLockingException ex = new OptimisticLockingException("Version conflict");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOptimisticLockingException(ex, request);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Optimistic Locking Conflict", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle OptimisticLockingFailureException correctly")
    void shouldHandleOptimisticLockingFailureException() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Version conflict");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOptimisticLockingFailureException(ex, request);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OPTIMISTIC_LOCKING_ERROR", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException correctly")
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("task", "title", "Title is required"));
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
    }

    @Test
    @DisplayName("Should handle BadCredentialsException correctly")
    void shouldHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, request);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("AUTHENTICATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException correctly")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Argument", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle IllegalStateException correctly")
    void shouldHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Illegal state");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalStateException(ex, request);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Illegal State", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle DataAccessException correctly")
    void shouldHandleDataAccessException() {
        DataAccessException ex = mock(DataAccessException.class);
        when(ex.getMessage()).thenReturn("Database error");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataAccessException(ex, request);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Database Error", response.getBody().getError());
        assertEquals("DATABASE_ERROR", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException correctly")
    void shouldHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("id", "Long");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMissingParameter(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Missing Parameter", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("id"));
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException correctly")
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Invalid JSON");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMessageNotReadable(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Request Body", response.getBody().getError());
    }

    @Test
    @DisplayName("Should handle generic Exception correctly")
    void shouldHandleGenericException() {
        Exception ex = new Exception("Unexpected error");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
    }
}
