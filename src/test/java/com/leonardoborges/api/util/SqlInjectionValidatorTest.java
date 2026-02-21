package com.leonardoborges.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlInjectionValidatorTest {
    
    private SqlInjectionValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new SqlInjectionValidator();
    }
    
    @Test
    void shouldAcceptSafeInput() {
        assertTrue(validator.isSafe("Normal task title"));
        assertTrue(validator.isSafe("Task 123"));
        assertTrue(validator.isSafe("Hello World"));
        assertTrue(validator.isSafe(null));
        assertTrue(validator.isSafe(""));
    }
    
    @Test
    void shouldDetectSqlInjectionPatterns() {
        assertFalse(validator.isSafe("'; DROP TABLE tasks; --"));
        assertFalse(validator.isSafe("' OR '1'='1"));
        assertFalse(validator.isSafe("' OR 1=1 --"));
        assertFalse(validator.isSafe("'; INSERT INTO users VALUES('admin', 'pass'); --"));
        assertFalse(validator.isSafe("' UNION SELECT * FROM users --"));
    }
    
    @Test
    void shouldDetectDangerousKeywords() {
        assertFalse(validator.isSafe("SELECT * FROM tasks"));
        assertFalse(validator.isSafe("DROP TABLE tasks"));
        assertFalse(validator.isSafe("INSERT INTO tasks VALUES"));
        assertFalse(validator.isSafe("UPDATE tasks SET"));
        assertFalse(validator.isSafe("DELETE FROM tasks"));
    }
    
    @Test
    void shouldSanitizeDangerousInput() {
        String safe = validator.sanitize("'; DROP TABLE tasks; --");
        assertNotNull(safe);
        assertFalse(safe.contains("DROP"));
        assertFalse(safe.contains("TABLE"));
    }
    
    @Test
    void shouldValidateSortFields() {
        assertTrue(validator.isValidSortField("id"));
        assertTrue(validator.isValidSortField("title"));
        assertTrue(validator.isValidSortField("createdAt"));
        assertTrue(validator.isValidSortField("status_priority"));
        
        assertFalse(validator.isValidSortField("'; DROP TABLE --"));
        assertFalse(validator.isValidSortField("id; DELETE"));
        assertFalse(validator.isValidSortField("id' OR '1'='1"));
        assertFalse(validator.isValidSortField(null));
        assertFalse(validator.isValidSortField(""));
    }
    
    @Test
    void shouldValidateIdentifiers() {
        assertTrue(validator.isValidIdentifier("tasks"));
        assertTrue(validator.isValidIdentifier("user_id"));
        assertTrue(validator.isValidIdentifier("Task123"));
        
        assertFalse(validator.isValidIdentifier("123tasks"));
        assertFalse(validator.isValidIdentifier("tasks-table"));
        assertFalse(validator.isValidIdentifier("tasks; DROP"));
        assertFalse(validator.isValidIdentifier(null));
        assertFalse(validator.isValidIdentifier(""));
    }
}
