package com.leonardoborges.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CorsProperties Tests")
class CorsPropertiesTest {

    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        corsProperties = new CorsProperties();
    }

    @Test
    @DisplayName("Should return empty list when allowedOrigins is null")
    void shouldReturnEmptyList_WhenAllowedOriginsIsNull() {
        corsProperties.setAllowedOrigins(null);

        List<String> result = corsProperties.getAllowedOriginsList();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when allowedOrigins is empty")
    void shouldReturnEmptyList_WhenAllowedOriginsIsEmpty() {
        corsProperties.setAllowedOrigins("   ");

        List<String> result = corsProperties.getAllowedOriginsList();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should parse allowedOrigins correctly")
    void shouldParseAllowedOrigins_Correctly() {
        corsProperties.setAllowedOrigins("http://localhost:3000,http://localhost:8080");

        List<String> result = corsProperties.getAllowedOriginsList();

        assertEquals(2, result.size());
        assertTrue(result.contains("http://localhost:3000"));
        assertTrue(result.contains("http://localhost:8080"));
    }

    @Test
    @DisplayName("Should return default methods when allowedMethods is null")
    void shouldReturnDefaultMethods_WhenAllowedMethodsIsNull() {
        corsProperties.setAllowedMethods(null);

        List<String> result = corsProperties.getAllowedMethodsList();

        assertEquals(5, result.size());
        assertTrue(result.contains("GET"));
        assertTrue(result.contains("POST"));
    }

    @Test
    @DisplayName("Should return default methods when allowedMethods is empty")
    void shouldReturnDefaultMethods_WhenAllowedMethodsIsEmpty() {
        corsProperties.setAllowedMethods("   ");

        List<String> result = corsProperties.getAllowedMethodsList();

        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Should return default headers when allowedHeaders is null")
    void shouldReturnWildcard_WhenAllowedHeadersIsNull() {
        corsProperties.setAllowedHeaders(null);

        List<String> result = corsProperties.getAllowedHeadersList();

        assertEquals(3, result.size());
        assertTrue(result.contains("Authorization"));
        assertTrue(result.contains("Content-Type"));
        assertTrue(result.contains("X-Requested-With"));
    }

    @Test
    @DisplayName("Should return default headers when allowedHeaders is empty")
    void shouldReturnWildcard_WhenAllowedHeadersIsEmpty() {
        corsProperties.setAllowedHeaders("   ");

        List<String> result = corsProperties.getAllowedHeadersList();

        assertEquals(3, result.size());
        assertTrue(result.contains("Authorization"));
        assertTrue(result.contains("Content-Type"));
        assertTrue(result.contains("X-Requested-With"));
    }

    @Test
    @DisplayName("Should return wildcard when allowedHeaders is asterisk")
    void shouldReturnWildcard_WhenAllowedHeadersIsAsterisk() {
        corsProperties.setAllowedHeaders("*");

        List<String> result = corsProperties.getAllowedHeadersList();

        assertEquals(1, result.size());
        assertEquals("*", result.get(0));
    }

    @Test
    @DisplayName("Should parse allowedHeaders correctly")
    void shouldParseAllowedHeaders_Correctly() {
        corsProperties.setAllowedHeaders("Authorization,Content-Type");

        List<String> result = corsProperties.getAllowedHeadersList();

        assertEquals(2, result.size());
        assertTrue(result.contains("Authorization"));
        assertTrue(result.contains("Content-Type"));
    }

    @Test
    @DisplayName("Should return default exposed headers when exposedHeaders is null")
    void shouldReturnDefaultExposedHeaders_WhenExposedHeadersIsNull() {
        corsProperties.setExposedHeaders(null);

        List<String> result = corsProperties.getExposedHeadersList();

        assertEquals(2, result.size());
        assertTrue(result.contains("Authorization"));
        assertTrue(result.contains("Content-Type"));
    }

    @Test
    @DisplayName("Should return default exposed headers when exposedHeaders is empty")
    void shouldReturnDefaultExposedHeaders_WhenExposedHeadersIsEmpty() {
        corsProperties.setExposedHeaders("   ");

        List<String> result = corsProperties.getExposedHeadersList();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should parse exposedHeaders correctly")
    void shouldParseExposedHeaders_Correctly() {
        corsProperties.setExposedHeaders("Authorization,Content-Type,X-Refresh-Token");

        List<String> result = corsProperties.getExposedHeadersList();

        assertEquals(3, result.size());
        assertTrue(result.contains("Authorization"));
        assertTrue(result.contains("Content-Type"));
        assertTrue(result.contains("X-Refresh-Token"));
    }

    @Test
    @DisplayName("Should filter empty strings from origins")
    void shouldFilterEmptyStrings_FromOrigins() {
        corsProperties.setAllowedOrigins("http://localhost:3000,,http://localhost:8080,  ");

        List<String> result = corsProperties.getAllowedOriginsList();

        assertEquals(2, result.size());
        assertTrue(result.contains("http://localhost:3000"));
        assertTrue(result.contains("http://localhost:8080"));
    }

    @Test
    @DisplayName("Should filter empty strings from methods")
    void shouldFilterEmptyStrings_FromMethods() {
        corsProperties.setAllowedMethods("GET,POST,,PUT,  ");

        List<String> result = corsProperties.getAllowedMethodsList();

        assertEquals(3, result.size());
        assertTrue(result.contains("GET"));
        assertTrue(result.contains("POST"));
        assertTrue(result.contains("PUT"));
    }

    @Test
    @DisplayName("Should filter empty strings from headers")
    void shouldFilterEmptyStrings_FromHeaders() {
        corsProperties.setAllowedHeaders("Authorization,,Content-Type,  ");

        List<String> result = corsProperties.getAllowedHeadersList();

        assertEquals(2, result.size());
        assertTrue(result.contains("Authorization"));
        assertTrue(result.contains("Content-Type"));
    }

    @Test
    @DisplayName("Should filter empty strings from exposed headers")
    void shouldFilterEmptyStrings_FromExposedHeaders() {
        corsProperties.setExposedHeaders("Authorization,,Content-Type,  ");

        List<String> result = corsProperties.getExposedHeadersList();

        assertEquals(2, result.size());
        assertTrue(result.contains("Authorization"));
        assertTrue(result.contains("Content-Type"));
    }
}
