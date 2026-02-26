package com.leonardoborges.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtProperties Tests")
class JwtPropertiesTest {

    private Environment environment;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        jwtProperties = new JwtProperties(environment);
    }

    @Test
    @DisplayName("Should set default test secret when in test profile and secret is null")
    void shouldSetDefaultTestSecret_WhenInTestProfileAndSecretIsNull() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        jwtProperties.setSecret(null);

        jwtProperties.validate();

        assertNotNull(jwtProperties.getSecret());
        assertTrue(jwtProperties.getSecret().length() >= 32);
    }

    @Test
    @DisplayName("Should keep existing secret when in test profile and secret is provided")
    void shouldKeepExistingSecret_WhenInTestProfileAndSecretIsProvided() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        String existingSecret = "existing-test-secret-key-minimum-32-chars";
        jwtProperties.setSecret(existingSecret);

        jwtProperties.validate();

        assertEquals(existingSecret, jwtProperties.getSecret());
    }

    @Test
    @DisplayName("Should throw exception when secret is too short in production")
    void shouldThrowException_WhenSecretIsTooShortInProduction() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        jwtProperties.setSecret("short");

        assertThrows(IllegalStateException.class, () -> {
            jwtProperties.validate();
        });
    }

    @Test
    @DisplayName("Should throw exception when secret is null in production")
    void shouldThrowException_WhenSecretIsNullInProduction() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        jwtProperties.setSecret(null);

        assertThrows(IllegalStateException.class, () -> {
            jwtProperties.validate();
        });
    }

    @Test
    @DisplayName("Should warn but not throw when secret is too short in development")
    void shouldWarnButNotThrow_WhenSecretIsTooShortInDevelopment() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        jwtProperties.setSecret("short");

        assertDoesNotThrow(() -> {
            jwtProperties.validate();
        });
    }

    @Test
    @DisplayName("Should accept valid secret in production")
    void shouldAcceptValidSecret_InProduction() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        jwtProperties.setSecret("a-very-long-secret-key-for-production-minimum-32-chars");

        assertDoesNotThrow(() -> {
            jwtProperties.validate();
        });
    }

    @Test
    @DisplayName("Should handle production profile name")
    void shouldHandleProductionProfileName() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"production"});
        jwtProperties.setSecret("short");

        assertThrows(IllegalStateException.class, () -> {
            jwtProperties.validate();
        });
    }

    @Test
    @DisplayName("Should warn when expiration is very short")
    void shouldWarn_WhenExpirationIsVeryShort() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        jwtProperties.setSecret("test-secret-key-for-testing-purposes-only-minimum-32-chars");
        jwtProperties.setExpiration(30000L);

        assertDoesNotThrow(() -> {
            jwtProperties.validate();
        });
    }

    @Test
    @DisplayName("Should warn when refresh expiration is not longer than access expiration")
    void shouldWarn_WhenRefreshExpirationIsNotLongerThanAccessExpiration() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        jwtProperties.setSecret("test-secret-key-for-testing-purposes-only-minimum-32-chars");
        jwtProperties.setExpiration(86400000L);
        jwtProperties.setRefreshExpiration(86400000L);

        assertDoesNotThrow(() -> {
            jwtProperties.validate();
        });
    }
}
