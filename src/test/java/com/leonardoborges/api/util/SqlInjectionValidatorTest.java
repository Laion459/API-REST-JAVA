package com.leonardoborges.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SqlInjectionValidator.
 * 
 * Valida proteção contra SQL Injection:
 * - Detecção de padrões de SQL injection
 * - Detecção de palavras-chave perigosas
 * - Sanitização de inputs perigosos
 * - Validação de campos de ordenação
 * - Validação de identificadores
 * 
 * Boas práticas aplicadas:
 * - Testes isolados e independentes
 * - Nomes descritivos de testes
 * - Cobertura de casos de ataque conhecidos
 * - Validação de casos válidos e inválidos
 */
class SqlInjectionValidatorTest {
    
    private SqlInjectionValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new SqlInjectionValidator();
    }
    
    @Test
    @DisplayName("Deve aceitar inputs seguros")
    void shouldAcceptSafeInput_WhenNoInjectionPatterns() {
        // Assert
        assertTrue(validator.isSafe("Normal task title"));
        assertTrue(validator.isSafe("Task 123"));
        assertTrue(validator.isSafe("Hello World"));
        assertTrue(validator.isSafe(null));
        assertTrue(validator.isSafe(""));
    }
    
    @Test
    @DisplayName("Deve detectar padrões de SQL injection")
    void shouldDetectSqlInjectionPatterns_WhenPresent() {
        // Assert
        assertFalse(validator.isSafe("'; DROP TABLE tasks; --"));
        assertFalse(validator.isSafe("' OR '1'='1"));
        assertFalse(validator.isSafe("' OR 1=1 --"));
        assertFalse(validator.isSafe("'; INSERT INTO users VALUES('admin', 'pass'); --"));
        assertFalse(validator.isSafe("' UNION SELECT * FROM users --"));
    }
    
    @Test
    @DisplayName("Deve detectar palavras-chave perigosas")
    void shouldDetectDangerousKeywords_WhenPresent() {
        // Assert
        assertFalse(validator.isSafe("SELECT * FROM tasks"));
        assertFalse(validator.isSafe("DROP TABLE tasks"));
        assertFalse(validator.isSafe("INSERT INTO tasks VALUES"));
        assertFalse(validator.isSafe("UPDATE tasks SET title='hack' WHERE id=1"));
        assertFalse(validator.isSafe("DELETE FROM tasks"));
    }
    
    @Test
    @DisplayName("Deve sanitizar inputs perigosos")
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
    @DisplayName("Deve validar campos de ordenação seguros")
    void shouldValidateSortFields_WhenSafe() {
        // Assert
        assertTrue(validator.isValidSortField("id"));
        assertTrue(validator.isValidSortField("title"));
        assertTrue(validator.isValidSortField("createdAt"));
        assertTrue(validator.isValidSortField("status_priority"));
    }
    
    @Test
    @DisplayName("Deve rejeitar campos de ordenação perigosos")
    void shouldRejectSortFields_WhenDangerous() {
        // Assert
        assertFalse(validator.isValidSortField("'; DROP TABLE --"));
        assertFalse(validator.isValidSortField("id; DELETE"));
        assertFalse(validator.isValidSortField("id' OR '1'='1"));
        assertFalse(validator.isValidSortField(null));
        assertFalse(validator.isValidSortField(""));
    }
    
    @Test
    @DisplayName("Deve validar identificadores seguros")
    void shouldValidateIdentifiers_WhenSafe() {
        // Assert
        assertTrue(validator.isValidIdentifier("tasks"));
        assertTrue(validator.isValidIdentifier("user_id"));
        assertTrue(validator.isValidIdentifier("Task123"));
    }
    
    @Test
    @DisplayName("Deve rejeitar identificadores perigosos")
    void shouldRejectIdentifiers_WhenDangerous() {
        // Assert
        assertFalse(validator.isValidIdentifier("123tasks"));
        assertFalse(validator.isValidIdentifier("tasks-table"));
        assertFalse(validator.isValidIdentifier("tasks; DROP"));
        assertFalse(validator.isValidIdentifier(null));
        assertFalse(validator.isValidIdentifier(""));
    }
}
