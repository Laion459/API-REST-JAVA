package com.leonardoborges.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("SortParameterValidator Tests")
class SortParameterValidatorTest {

    @Mock
    private SqlInjectionValidator sqlInjectionValidator;

    @InjectMocks
    private SortParameterValidator validator;

    @BeforeEach
    void setUp() {
        lenient().when(sqlInjectionValidator.isValidSortField(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Should return valid Pageable when sort is valid")
    void shouldReturnValidPageableWhenSortIsValid() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("title"));
        
        Pageable result = validator.validateAndNormalizeTaskSort(pageable, "createdAt", Sort.Direction.DESC);
        
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(20, result.getPageSize());
        assertTrue(result.getSort().isSorted());
    }

    @Test
    @DisplayName("Should return Pageable with default sort when field is invalid")
    void shouldReturnPageableWithDefaultSortWhenFieldIsInvalid() {
        when(sqlInjectionValidator.isValidSortField("invalidField")).thenReturn(true);
        
        Pageable pageable = PageRequest.of(0, 20, Sort.by("invalidField"));
        
        Pageable result = validator.validateAndNormalizeTaskSort(pageable, "createdAt", Sort.Direction.DESC);
        
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(20, result.getPageSize());
        assertEquals("createdAt", result.getSort().iterator().next().getProperty());
        assertEquals(Sort.Direction.DESC, result.getSort().iterator().next().getDirection());
    }

    @Test
    @DisplayName("Should return original Pageable when there is no sort")
    void shouldReturnOriginalPageableWhenThereIsNoSort() {
        Pageable pageable = PageRequest.of(0, 20);
        
        Pageable result = validator.validateAndNormalizeTaskSort(pageable, "createdAt", Sort.Direction.DESC);
        
        assertNotNull(result);
        assertEquals(pageable, result);
    }

    @Test
    @DisplayName("Should return Pageable with default sort when SQL injection is detected")
    void shouldReturnPageableWithDefaultSortWhenSqlInjectionIsDetected() {
        when(sqlInjectionValidator.isValidSortField("'; DROP TABLE tasks; --")).thenReturn(false);
        
        Pageable pageable = PageRequest.of(0, 20, Sort.by("'; DROP TABLE tasks; --"));
        
        Pageable result = validator.validateAndNormalizeTaskSort(pageable, "createdAt", Sort.Direction.DESC);
        
        assertNotNull(result);
        assertEquals("createdAt", result.getSort().iterator().next().getProperty());
    }

    @Test
    @DisplayName("Should return Pageable with default sort when exception occurs")
    void shouldReturnPageableWithDefaultSortWhenExceptionOccurs() {
        Pageable pageable = mock(Pageable.class);
        Sort sort = mock(Sort.class);
        when(pageable.getSort()).thenReturn(sort);
        when(pageable.getPageNumber()).thenReturn(0);
        when(pageable.getPageSize()).thenReturn(20);
        when(sort.isSorted()).thenReturn(true);
        when(sort.stream()).thenThrow(new RuntimeException("Test exception"));
        
        Pageable result = validator.validateAndNormalizeTaskSort(pageable, "createdAt", Sort.Direction.DESC);
        
        assertNotNull(result);
        assertEquals("createdAt", result.getSort().iterator().next().getProperty());
    }

    @Test
    @DisplayName("Should validate valid sort field")
    void shouldValidateValidSortField() {
        when(sqlInjectionValidator.isValidSortField("title")).thenReturn(true);
        
        assertTrue(validator.isValidTaskSortField("title"));
    }

    @Test
    @DisplayName("Should return false for invalid sort field")
    void shouldReturnFalseForInvalidSortField() {
        when(sqlInjectionValidator.isValidSortField("invalidField")).thenReturn(true);
        
        assertFalse(validator.isValidTaskSortField("invalidField"));
    }

    @Test
    @DisplayName("Should return false for null field")
    void shouldReturnFalseForNullField() {
        assertFalse(validator.isValidTaskSortField(null));
    }

    @Test
    @DisplayName("Should return false when SQL injection is detected")
    void shouldReturnFalseWhenSqlInjectionIsDetected() {
        when(sqlInjectionValidator.isValidSortField("'; DROP TABLE tasks; --")).thenReturn(false);
        
        assertFalse(validator.isValidTaskSortField("'; DROP TABLE tasks; --"));
    }

    @Test
    @DisplayName("Should validate all allowed sort fields")
    void shouldValidateAllAllowedSortFields() {
        String[] validFields = {"id", "title", "description", "status", "priority", 
                                "createdAt", "updatedAt", "version"};
        
        for (String field : validFields) {
            when(sqlInjectionValidator.isValidSortField(field)).thenReturn(true);
            assertTrue(validator.isValidTaskSortField(field), 
                    "Field " + field + " should be valid");
        }
    }
}
