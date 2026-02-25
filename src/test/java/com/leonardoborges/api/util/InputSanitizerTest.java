package com.leonardoborges.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para InputSanitizer.
 * 
 * Valida a sanitização de inputs do usuário:
 * - Remoção de espaços em branco
 * - Remoção de caracteres de controle
 * - Normalização de espaços múltiplos
 * - Truncamento de strings longas
 * - Validação após sanitização
 * 
 * Boas práticas aplicadas:
 * - Testes isolados e independentes
 * - Nomes descritivos de testes
 * - Cobertura de casos de borda (null, vazio, muito longo)
 */
class InputSanitizerTest {

    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    @Test
    @DisplayName("Deve remover espaços em branco no início e fim")
    void shouldSanitizeString_WithLeadingAndTrailingWhitespace() {
        // Arrange
        String input = "  Test String  ";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("Test String", result);
    }

    @Test
    @DisplayName("Deve remover caracteres de controle")
    void shouldRemoveControlCharacters_FromString() {
        // Arrange
        String input = "Test\u0000String\u0001";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("TestString", result);
    }

    @Test
    @DisplayName("Deve normalizar múltiplos espaços em um único espaço")
    void shouldReplaceMultipleSpaces_WithSingleSpace() {
        // Arrange
        String input = "Test    String";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("Test String", result);
    }

    @Test
    @DisplayName("Deve retornar null quando input é null")
    void shouldReturnNull_WhenInputIsNull() {
        // Act
        String result = inputSanitizer.sanitizeString(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve truncar string muito longa")
    void shouldTruncateLongString_WhenExceedsMaxLength() {
        // Arrange
        String input = "A".repeat(2000);
        int maxLength = 100;

        // Act
        String result = inputSanitizer.sanitizeAndTruncate(input, maxLength);

        // Assert
        assertNotNull(result);
        assertEquals(maxLength, result.length());
    }

    @Test
    @DisplayName("Não deve truncar string curta")
    void shouldNotTruncateShortString_WhenWithinMaxLength() {
        // Arrange
        String input = "Short string";
        int maxLength = 100;

        // Act
        String result = inputSanitizer.sanitizeAndTruncate(input, maxLength);

        // Assert
        assertEquals(input, result);
    }

    @Test
    @DisplayName("Deve validar string após sanitização")
    void shouldValidateString_AfterSanitization() {
        // Assert
        assertTrue(inputSanitizer.isValidAfterSanitization("Valid String"));
        assertFalse(inputSanitizer.isValidAfterSanitization("   "));
        assertFalse(inputSanitizer.isValidAfterSanitization(null));
        assertFalse(inputSanitizer.isValidAfterSanitization(""));
    }

    @Test
    @DisplayName("Deve preservar quebras de linha e tabs")
    void shouldPreserveNewlinesAndTabs_InString() {
        // Arrange
        String input = "Line1\nLine2\tTabbed";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("\t"));
    }

    @Test
    @DisplayName("Deve sanitizar string vazia")
    void shouldSanitizeEmptyString() {
        // Arrange
        String input = "";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Deve sanitizar string com apenas espaços")
    void shouldSanitizeString_WithOnlySpaces() {
        // Arrange
        String input = "     ";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertNotNull(result);
    }
}
