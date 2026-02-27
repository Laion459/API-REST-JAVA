package com.leonardoborges.api.service;

import com.leonardoborges.api.exception.ValidationException;
import com.leonardoborges.api.util.InputSanitizer;
import com.leonardoborges.api.util.SqlInjectionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Base Validation Service Tests")
class BaseValidationServiceTest {

    @Mock
    private SqlInjectionValidator sqlInjectionValidator;

    @Mock
    private InputSanitizer inputSanitizer;

    private BaseValidationService baseValidationService;

    @BeforeEach
    void setUp() {
        baseValidationService = new BaseValidationService(sqlInjectionValidator, inputSanitizer);
    }

    @Test
    @DisplayName("Should validate not null or empty")
    void shouldValidateNotNullOrEmpty() {
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateNotNullOrEmpty(null, "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateNotNullOrEmpty("", "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateNotNullOrEmpty("   ", "field"));
        assertDoesNotThrow(() -> baseValidationService.validateNotNullOrEmpty("value", "field"));
    }

    @Test
    @DisplayName("Should validate not empty if present")
    void shouldValidateNotEmptyIfPresent() {
        assertDoesNotThrow(() -> baseValidationService.validateNotEmptyIfPresent(null, "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateNotEmptyIfPresent("", "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateNotEmptyIfPresent("   ", "field"));
        assertDoesNotThrow(() -> baseValidationService.validateNotEmptyIfPresent("value", "field"));
    }

    @Test
    @DisplayName("Should validate SQL injection safety")
    void shouldValidateSqlInjectionSafe() {
        when(sqlInjectionValidator.isSafe(anyString())).thenReturn(true);
        assertDoesNotThrow(() -> baseValidationService.validateSqlInjectionSafe("safe", "field"));
        
        when(sqlInjectionValidator.isSafe(anyString())).thenReturn(false);
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateSqlInjectionSafe("unsafe", "field"));
        
        assertDoesNotThrow(() -> baseValidationService.validateSqlInjectionSafe(null, "field"));
    }

    @Test
    @DisplayName("Should sanitize and truncate")
    void shouldSanitizeAndTruncate() {
        when(inputSanitizer.sanitizeAndTruncate(anyString(), anyInt())).thenReturn("sanitized");
        String result = baseValidationService.sanitizeAndTruncate("value", 100);
        assertEquals("sanitized", result);
        
        assertNull(baseValidationService.sanitizeAndTruncate(null, 100));
    }

    @Test
    @DisplayName("Should validate not negative")
    void shouldValidateNotNegative() {
        assertDoesNotThrow(() -> baseValidationService.validateNotNegative(null, "field"));
        assertDoesNotThrow(() -> baseValidationService.validateNotNegative(0, "field"));
        assertDoesNotThrow(() -> baseValidationService.validateNotNegative(10, "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateNotNegative(-1, "field"));
    }

    @Test
    @DisplayName("Should validate in range")
    void shouldValidateInRange() {
        assertDoesNotThrow(() -> baseValidationService.validateInRange(null, 0, 100, "field"));
        assertDoesNotThrow(() -> baseValidationService.validateInRange(50, 0, 100, "field"));
        assertDoesNotThrow(() -> baseValidationService.validateInRange(0, 0, 100, "field"));
        assertDoesNotThrow(() -> baseValidationService.validateInRange(100, 0, 100, "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateInRange(-1, 0, 100, "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateInRange(101, 0, 100, "field"));
    }

    @Test
    @DisplayName("Should validate length")
    void shouldValidateLength() {
        assertDoesNotThrow(() -> baseValidationService.validateLength(null, 0, 100, "field"));
        assertDoesNotThrow(() -> baseValidationService.validateLength("abc", 0, 100, "field"));
        assertDoesNotThrow(() -> baseValidationService.validateLength("a", 1, 100, "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateLength("", 1, 100, "field"));
        assertThrows(ValidationException.class, 
                () -> baseValidationService.validateLength("a".repeat(101), 0, 100, "field"));
    }
}
