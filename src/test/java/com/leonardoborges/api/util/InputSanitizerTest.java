package com.leonardoborges.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InputSanitizer.
 * 
 * Validates user input sanitization:
 * - Removal of whitespace
 * - Removal of control characters
 * - Normalization of multiple spaces
 * - Truncation of long strings
 * - Validation after sanitization
 * 
 * Best practices applied:
 * - Isolated and independent tests
 * - Descriptive test names
 * - Coverage of edge cases (null, empty, too long)
 */
class InputSanitizerTest {

    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    @Test
    @DisplayName("Should remove leading and trailing whitespace")
    void shouldSanitizeString_WithLeadingAndTrailingWhitespace() {
        // Arrange
        String input = "  Test String  ";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("Test String", result);
    }

    @Test
    @DisplayName("Should remove control characters")
    void shouldRemoveControlCharacters_FromString() {
        // Arrange
        String input = "Test\u0000String\u0001";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("TestString", result);
    }

    @Test
    @DisplayName("Should normalize multiple spaces into a single space")
    void shouldReplaceMultipleSpaces_WithSingleSpace() {
        // Arrange
        String input = "Test    String";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("Test String", result);
    }

    @Test
    @DisplayName("Should return null when input is null")
    void shouldReturnNull_WhenInputIsNull() {
        // Act
        String result = inputSanitizer.sanitizeString(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should truncate very long string")
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
    @DisplayName("Should not truncate short string")
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
    @DisplayName("Should validate string after sanitization")
    void shouldValidateString_AfterSanitization() {
        // Assert
        assertTrue(inputSanitizer.isValidAfterSanitization("Valid String"));
        assertFalse(inputSanitizer.isValidAfterSanitization("   "));
        assertFalse(inputSanitizer.isValidAfterSanitization(null));
        assertFalse(inputSanitizer.isValidAfterSanitization(""));
    }

    @Test
    @DisplayName("Should preserve newlines and tabs")
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
    @DisplayName("Should sanitize empty string")
    void shouldSanitizeEmptyString() {
        // Arrange
        String input = "";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should sanitize string with only spaces")
    void shouldSanitizeString_WithOnlySpaces() {
        // Arrange
        String input = "     ";

        // Act
        String result = inputSanitizer.sanitizeString(input);

        // Assert
        assertNotNull(result);
    }
}
