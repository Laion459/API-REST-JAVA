package com.leonardoborges.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SqlInjectionValidator.
 * 
 * Validates SQL Injection protection:
 * - Detection of SQL injection patterns
 * - Detection of dangerous keywords
 * - Sanitization of dangerous inputs
 * - Validation of sort fields
 * - Validation of identifiers
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Descriptive test names
 * - Coverage of known attack cases
 * - Validation of valid and invalid cases
 */
class SqlInjectionValidatorTest {
    
    private SqlInjectionValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new SqlInjectionValidator();
    }
    
    @Test
    @DisplayName("Should accept safe inputs")
    void shouldAcceptSafeInput_WhenNoInjectionPatterns() {
        // Assert
        assertTrue(validator.isSafe("Normal task title"));
        assertTrue(validator.isSafe("Task 123"));
        assertTrue(validator.isSafe("Hello World"));
        assertTrue(validator.isSafe(null));
        assertTrue(validator.isSafe(""));
    }
    
    @Test
    @DisplayName("Should detect SQL injection patterns")
    void shouldDetectSqlInjectionPatterns_WhenPresent() {
        // Assert
        assertFalse(validator.isSafe("'; DROP TABLE tasks; --"));
        assertFalse(validator.isSafe("' OR '1'='1"));
        assertFalse(validator.isSafe("' OR 1=1 --"));
        assertFalse(validator.isSafe("'; INSERT INTO users VALUES('admin', 'pass'); --"));
        assertFalse(validator.isSafe("' UNION SELECT * FROM users --"));
    }
    
    @Test
    @DisplayName("Should detect dangerous keywords")
    void shouldDetectDangerousKeywords_WhenPresent() {
        // Assert
        assertFalse(validator.isSafe("SELECT * FROM tasks"));
        assertFalse(validator.isSafe("DROP TABLE tasks"));
        assertFalse(validator.isSafe("INSERT INTO tasks VALUES"));
        assertFalse(validator.isSafe("UPDATE tasks SET title='hack' WHERE id=1"));
        assertFalse(validator.isSafe("DELETE FROM tasks"));
    }
    
    @Test
    @DisplayName("Should sanitize dangerous inputs")
    void shouldSanitizeDangerousInput_WhenInjectionDetected() {
        // Arrange
        String dangerousInput = "'; DROP TABLE tasks; --";

        // Act
        String safe = validator.sanitize(dangerousInput);

        // Assert
        assertNotNull(safe);
        assertFalse(safe.contains("DROP"));
        assertFalse(safe.contains("TABLE"));
    }
    
    @Test
    @DisplayName("Should validate safe sort fields")
    void shouldValidateSortFields_WhenSafe() {
        // Assert
        assertTrue(validator.isValidSortField("id"));
        assertTrue(validator.isValidSortField("title"));
        assertTrue(validator.isValidSortField("createdAt"));
        assertTrue(validator.isValidSortField("status_priority"));
    }
    
    @Test
    @DisplayName("Should reject dangerous sort fields")
    void shouldRejectSortFields_WhenDangerous() {
        // Assert
        assertFalse(validator.isValidSortField("'; DROP TABLE --"));
        assertFalse(validator.isValidSortField("id; DELETE"));
        assertFalse(validator.isValidSortField("id' OR '1'='1"));
        assertFalse(validator.isValidSortField(null));
        assertFalse(validator.isValidSortField(""));
    }
    
    @Test
    @DisplayName("Should validate safe identifiers")
    void shouldValidateIdentifiers_WhenSafe() {
        // Assert
        assertTrue(validator.isValidIdentifier("tasks"));
        assertTrue(validator.isValidIdentifier("user_id"));
        assertTrue(validator.isValidIdentifier("Task123"));
    }
    
    @Test
    @DisplayName("Should reject dangerous identifiers")
    void shouldRejectIdentifiers_WhenDangerous() {
        // Assert
        assertFalse(validator.isValidIdentifier("123tasks"));
        assertFalse(validator.isValidIdentifier("tasks-table"));
        assertFalse(validator.isValidIdentifier("tasks; DROP"));
        assertFalse(validator.isValidIdentifier(null));
        assertFalse(validator.isValidIdentifier(""));
    }

    @Test
    @DisplayName("Should handle dangerous characters in long strings")
    void shouldHandleDangerousCharacters_WhenLongString() {
        String longStringWithDangerousChars = "A".repeat(15) + "'; DROP TABLE tasks; --";
        
        boolean result = validator.isSafe(longStringWithDangerousChars);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle dangerous characters in short strings")
    void shouldHandleDangerousCharacters_WhenShortString() {
        String shortStringWithDangerousChars = "test'";
        
        boolean result = validator.isSafe(shortStringWithDangerousChars);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should sanitize input when not safe")
    void shouldSanitizeInput_WhenNotSafe() {
        String dangerousInput = "SELECT * FROM tasks";
        
        String sanitized = validator.sanitize(dangerousInput);
        
        assertNotNull(sanitized);
        assertNotEquals(dangerousInput, sanitized);
    }

    @Test
    @DisplayName("Should return input when safe in sanitize")
    void shouldReturnInput_WhenSafeInSanitize() {
        String safeInput = "Normal task title";
        
        String result = validator.sanitize(safeInput);
        
        assertEquals(safeInput, result);
    }

    @Test
    @DisplayName("Should handle empty string in isSafe")
    void shouldHandleEmptyString_InIsSafe() {
        assertTrue(validator.isSafe(""));
        assertTrue(validator.isSafe("   "));
    }
}
