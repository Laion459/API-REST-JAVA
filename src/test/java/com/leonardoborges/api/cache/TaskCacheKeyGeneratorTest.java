package com.leonardoborges.api.cache;

import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskCacheKeyGenerator Tests")
class TaskCacheKeyGeneratorTest {

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TaskCacheKeyGenerator taskCacheKeyGenerator;

    private Method testMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        testMethod = TaskCacheKeyGeneratorTest.class.getDeclaredMethod("testMethod", String.class, Long.class);
    }

    @Test
    @DisplayName("Should generate cache key with username and parameters")
    void shouldGenerateCacheKey_WithUsernameAndParameters() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenReturn("testuser");
        Object target = new Object();
        Object[] params = {"param1", 123L};

        // Act
        Object result = taskCacheKeyGenerator.generate(target, testMethod, params);

        // Assert
        assertNotNull(result);
        String key = result.toString();
        assertTrue(key.contains("testMethod"));
        assertTrue(key.contains("testuser"));
        assertTrue(key.contains("param1"));
        assertTrue(key.contains("123"));
    }

    @Test
    @DisplayName("Should generate cache key with username when no parameters")
    void shouldGenerateCacheKey_WithUsernameWhenNoParameters() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenReturn("testuser");
        Object target = new Object();
        Object[] params = {};

        // Act
        Object result = taskCacheKeyGenerator.generate(target, testMethod, params);

        // Assert
        assertNotNull(result);
        String key = result.toString();
        assertTrue(key.contains("testMethod"));
        assertTrue(key.contains("testuser"));
    }

    @Test
    @DisplayName("Should use fallback when exception occurs")
    void shouldUseFallback_WhenExceptionOccurs() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenThrow(new BusinessException("Error"));
        Object target = new Object();
        Object[] params = {"param1"};

        // Act
        Object result = taskCacheKeyGenerator.generate(target, testMethod, params);

        // Assert
        assertNotNull(result);
        String key = result.toString();
        assertTrue(key.contains("testMethod"));
        assertTrue(key.contains("anonymous"));
        assertTrue(key.contains("param1"));
    }

    @Test
    @DisplayName("Should handle null parameters")
    void shouldHandleNullParameters() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenReturn("testuser");
        Object target = new Object();
        Object[] params = {null, "param2"};

        // Act
        Object result = taskCacheKeyGenerator.generate(target, testMethod, params);

        // Assert
        assertNotNull(result);
        String key = result.toString();
        assertTrue(key.contains("testMethod"));
        assertTrue(key.contains("testuser"));
        assertTrue(key.contains("param2"));
    }

    @Test
    @DisplayName("Should handle null params array")
    void shouldHandleNullParamsArray() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenReturn("testuser");
        Object target = new Object();

        // Act
        Object result = taskCacheKeyGenerator.generate(target, testMethod, (Object[]) null);

        // Assert
        assertNotNull(result);
        String key = result.toString();
        assertTrue(key.contains("testMethod"));
        assertTrue(key.contains("testuser"));
    }

    @Test
    @DisplayName("Should generate unique keys for different users")
    void shouldGenerateUniqueKeys_ForDifferentUsers() {
        // Arrange
        Object target = new Object();
        Object[] params = {"param1"};

        when(securityUtils.getCurrentUsername()).thenReturn("user1");
        Object key1 = taskCacheKeyGenerator.generate(target, testMethod, params);

        when(securityUtils.getCurrentUsername()).thenReturn("user2");
        Object key2 = taskCacheKeyGenerator.generate(target, testMethod, params);

        // Assert
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Should generate unique keys for different parameters")
    void shouldGenerateUniqueKeys_ForDifferentParameters() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenReturn("testuser");
        Object target = new Object();

        Object key1 = taskCacheKeyGenerator.generate(target, testMethod, "param1");
        Object key2 = taskCacheKeyGenerator.generate(target, testMethod, "param2");

        // Assert
        assertNotEquals(key1, key2);
    }

    @SuppressWarnings("unused")
    private void testMethod(String param1, Long param2) {
        // Test method for reflection
    }
}
