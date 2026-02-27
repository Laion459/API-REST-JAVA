package com.leonardoborges.api.validation;

import com.leonardoborges.api.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SSRF Validator Tests")
class SsrfValidatorTest {

    private SsrfValidator ssrfValidator;

    @BeforeEach
    void setUp() {
        ssrfValidator = new SsrfValidator();
    }

    @Test
    @DisplayName("Should accept valid public HTTPS URL")
    void shouldAcceptValidPublicHttpsUrl() {
        assertDoesNotThrow(() -> ssrfValidator.validateUrl("https://example.com/api"));
    }

    @Test
    @DisplayName("Should accept valid public HTTP URL")
    void shouldAcceptValidPublicHttpUrl() {
        assertDoesNotThrow(() -> ssrfValidator.validateUrl("http://example.com/api"));
    }

    @Test
    @DisplayName("Should reject localhost URL")
    void shouldRejectLocalhostUrl() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("http://localhost:8080/api"));
    }

    @Test
    @DisplayName("Should reject 127.0.0.1 URL")
    void shouldReject127001Url() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("http://127.0.0.1:8080/api"));
    }

    @Test
    @DisplayName("Should reject private IP range 192.168.x.x")
    void shouldRejectPrivateIp192168() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("http://192.168.1.1/api"));
    }

    @Test
    @DisplayName("Should reject private IP range 10.x.x.x")
    void shouldRejectPrivateIp10() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("http://10.0.0.1/api"));
    }

    @Test
    @DisplayName("Should reject private IP range 172.16-31.x.x")
    void shouldRejectPrivateIp172() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("http://172.16.0.1/api"));
    }

    @Test
    @DisplayName("Should reject file protocol")
    void shouldRejectFileProtocol() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("file:///etc/passwd"));
    }

    @Test
    @DisplayName("Should reject gopher protocol")
    void shouldRejectGopherProtocol() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("gopher://example.com"));
    }

    @Test
    @DisplayName("Should reject null URL")
    void shouldRejectNullUrl() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl(null));
    }

    @Test
    @DisplayName("Should reject empty URL")
    void shouldRejectEmptyUrl() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl(""));
    }

    @Test
    @DisplayName("Should reject invalid URL format")
    void shouldRejectInvalidUrlFormat() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("not-a-valid-url"));
    }

    @Test
    @DisplayName("Should reject URL without protocol")
    void shouldRejectUrlWithoutProtocol() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("example.com/api"));
    }

    @Test
    @DisplayName("Should reject .local domain")
    void shouldRejectLocalDomain() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("http://example.local/api"));
    }

    @Test
    @DisplayName("Should reject .internal domain")
    void shouldRejectInternalDomain() {
        assertThrows(ValidationException.class, 
                () -> ssrfValidator.validateUrl("http://example.internal/api"));
    }
}
