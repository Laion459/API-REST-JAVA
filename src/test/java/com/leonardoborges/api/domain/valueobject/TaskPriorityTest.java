package com.leonardoborges.api.domain.valueobject;

import com.leonardoborges.api.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskPriority Value Object Tests")
class TaskPriorityTest {
    
    @Test
    void shouldCreateValidPriority() {
        TaskPriority priority = TaskPriority.of(5);
        assertEquals(5, priority.getValue());
    }
    
    @Test
    void shouldCreateDefaultPriority() {
        TaskPriority priority = TaskPriority.defaultPriority();
        assertEquals(0, priority.getValue()); // Default priority is 0
    }
    
    @Test
    void shouldCreateFromNullable() {
        TaskPriority priority1 = TaskPriority.ofNullable(null);
        assertEquals(0, priority1.getValue()); // Default priority is 0
        
        TaskPriority priority2 = TaskPriority.ofNullable(5);
        assertEquals(5, priority2.getValue());
    }
    
    @Test
    void shouldThrowExceptionForNegativePriority() {
        assertThrows(ValidationException.class, () -> TaskPriority.of(-1));
    }
    
    @Test
    void shouldThrowExceptionForPriorityAboveMax() {
        assertThrows(ValidationException.class, () -> TaskPriority.of(101));
    }
    
    @Test
    void shouldAllowMinPriority() {
        TaskPriority priority = TaskPriority.of(0);
        assertEquals(0, priority.getValue());
    }
    
    @Test
    void shouldAllowMaxPriority() {
        TaskPriority priority = TaskPriority.of(100);
        assertEquals(100, priority.getValue());
    }
    
    @Test
    void shouldComparePriorities() {
        TaskPriority low = TaskPriority.of(2);
        TaskPriority high = TaskPriority.of(8);
        
        assertTrue(high.isHigherThan(low));
        assertTrue(low.isLowerThan(high));
    }
    
    @Test
    void shouldComparePrioritiesCorrectly() {
        TaskPriority urgent = TaskPriority.of(80);
        TaskPriority normal = TaskPriority.of(50);
        
        assertTrue(urgent.isHigherThan(normal));
        assertTrue(normal.isLowerThan(urgent));
    }
    
    @Test
    void shouldBeEqualForSameValue() {
        TaskPriority p1 = TaskPriority.of(5);
        TaskPriority p2 = TaskPriority.of(5);
        
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
