package com.leonardoborges.api.domain.valueobject;

import com.leonardoborges.api.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskTitle Value Object Tests")
class TaskTitleTest {

    @Test
    @DisplayName("Should create valid TaskTitle")
    void shouldCreateValidTaskTitle() {
        TaskTitle title = TaskTitle.of("Valid Task Title");
        assertNotNull(title);
        assertEquals("Valid Task Title", title.getValue());
    }

    @Test
    @DisplayName("Should trim whitespace")
    void shouldTrimWhitespace() {
        TaskTitle title = TaskTitle.of("  Valid Task Title  ");
        assertEquals("Valid Task Title", title.getValue());
    }

    @Test
    @DisplayName("Should throw exception for null title")
    void shouldThrowExceptionForNullTitle() {
        assertThrows(ValidationException.class, () -> TaskTitle.of(null));
    }

    @Test
    @DisplayName("Should throw exception for empty title")
    void shouldThrowExceptionForEmptyTitle() {
        assertThrows(ValidationException.class, () -> TaskTitle.of(""));
        assertThrows(ValidationException.class, () -> TaskTitle.of("   "));
    }

    @Test
    @DisplayName("Should throw exception for title too short")
    void shouldThrowExceptionForTitleTooShort() {
        assertThrows(ValidationException.class, () -> TaskTitle.of("AB"));
    }

    @Test
    @DisplayName("Should throw exception for title too long")
    void shouldThrowExceptionForTitleTooLong() {
        String longTitle = "A".repeat(256);
        assertThrows(ValidationException.class, () -> TaskTitle.of(longTitle));
    }

    @Test
    @DisplayName("Should be equal for same value")
    void shouldBeEqualForSameValue() {
        TaskTitle title1 = TaskTitle.of("Test Title");
        TaskTitle title2 = TaskTitle.of("Test Title");
        assertEquals(title1, title2);
        assertEquals(title1.hashCode(), title2.hashCode());
    }
}
