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
        assertEquals(1, priority.getValue());
    }
    
    @Test
    void shouldCreateFromNullable() {
        TaskPriority priority1 = TaskPriority.fromNullable(null);
        assertEquals(1, priority1.getValue());
        
        TaskPriority priority2 = TaskPriority.fromNullable(5);
        assertEquals(5, priority2.getValue());
    }
    
    @Test
    void shouldThrowExceptionForNegativePriority() {
        assertThrows(ValidationException.class, () -> TaskPriority.of(-1));
    }
    
    @Test
    void shouldThrowExceptionForPriorityAboveMax() {
        assertThrows(ValidationException.class, () -> TaskPriority.of(11));
    }
    
    @Test
    void shouldAllowMinPriority() {
        TaskPriority priority = TaskPriority.of(0);
        assertEquals(0, priority.getValue());
    }
    
    @Test
    void shouldAllowMaxPriority() {
        TaskPriority priority = TaskPriority.of(10);
        assertEquals(10, priority.getValue());
    }
    
    @Test
    void shouldComparePriorities() {
        TaskPriority low = TaskPriority.of(2);
        TaskPriority high = TaskPriority.of(8);
        
        assertTrue(high.isHigherThan(low));
        assertTrue(low.isLowerThan(high));
    }
    
    @Test
    void shouldIdentifyUrgentPriority() {
        TaskPriority urgent = TaskPriority.of(8);
        TaskPriority normal = TaskPriority.of(5);
        
        assertTrue(urgent.isUrgent());
        assertFalse(normal.isUrgent());
    }
    
    @Test
    void shouldIdentifyLowPriority() {
        TaskPriority low = TaskPriority.of(2);
        TaskPriority normal = TaskPriority.of(5);
        
        assertTrue(low.isLow());
        assertFalse(normal.isLow());
    }
    
    @Test
    void shouldBeEqualForSameValue() {
        TaskPriority p1 = TaskPriority.of(5);
        TaskPriority p2 = TaskPriority.of(5);
        
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
