package com.leonardoborges.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputSanitizerTest {

    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    @Test
    void shouldSanitizeStringWithWhitespace() {
        String input = "  Test String  ";
        String result = inputSanitizer.sanitizeString(input);
        
        assertEquals("Test String", result);
    }

    @Test
    void shouldRemoveControlCharacters() {
        String input = "Test\u0000String\u0001";
        String result = inputSanitizer.sanitizeString(input);
        
        assertEquals("TestString", result);
    }

    @Test
    void shouldReplaceMultipleSpaces() {
        String input = "Test    String";
        String result = inputSanitizer.sanitizeString(input);
        
        assertEquals("Test String", result);
    }

    @Test
    void shouldReturnNullForNullInput() {
        String result = inputSanitizer.sanitizeString(null);
        
        assertNull(result);
    }

    @Test
    void shouldTruncateLongString() {
        String input = "A".repeat(2000);
        String result = inputSanitizer.sanitizeAndTruncate(input, 100);
        
        assertNotNull(result);
        assertEquals(100, result.length());
    }

    @Test
    void shouldNotTruncateShortString() {
        String input = "Short string";
        String result = inputSanitizer.sanitizeAndTruncate(input, 100);
        
        assertEquals(input, result);
    }

    @Test
    void shouldValidateStringAfterSanitization() {
        assertTrue(inputSanitizer.isValidAfterSanitization("Valid String"));
        assertFalse(inputSanitizer.isValidAfterSanitization("   "));
        assertFalse(inputSanitizer.isValidAfterSanitization(null));
        assertFalse(inputSanitizer.isValidAfterSanitization(""));
    }

    @Test
    void shouldPreserveNewlinesAndTabs() {
        String input = "Line1\nLine2\tTabbed";
        String result = inputSanitizer.sanitizeString(input);
        
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("\t"));
    }
}
