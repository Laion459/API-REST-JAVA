package com.leonardoborges.api.audit;

import com.leonardoborges.api.model.AuditLog;
import com.leonardoborges.api.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        // Setup request context
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("testuser");

        // Setup request headers
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("X-Real-IP")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        lenient().when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create audit log successfully")
    void shouldCreateAuditLogSuccessfully() {
        auditService.audit("TASK_CREATED", "Task", 1L, "Task created successfully");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("TASK_CREATED", savedLog.getAction());
        assertEquals("Task", savedLog.getEntityType());
        assertEquals(1L, savedLog.getEntityId());
        assertEquals("testuser", savedLog.getUsername());
        assertEquals("192.168.1.1", savedLog.getIpAddress());
        assertTrue(savedLog.getSuccess());
    }

    @Test
    @DisplayName("Should create audit log with changes")
    void shouldCreateAuditLogWithChanges() {
        auditService.auditWithChanges("TASK_UPDATED", "Task", 1L, 
                "Task updated", "Old Title", "New Title");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("TASK_UPDATED", savedLog.getAction());
        assertEquals("Old Title", savedLog.getOldValue());
        assertEquals("New Title", savedLog.getNewValue());
    }

    @Test
    @DisplayName("Should create authentication audit log successfully")
    void shouldCreateAuthenticationAuditLogSuccessfully() {
        auditService.auditAuthentication("LOGIN_SUCCESS", "testuser", "User logged in");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("LOGIN_SUCCESS", savedLog.getAction());
        assertEquals("Authentication", savedLog.getEntityType());
        assertEquals("testuser", savedLog.getUsername());
        assertTrue(savedLog.getSuccess());
        assertNull(savedLog.getError());
    }

    @Test
    @DisplayName("Should create authentication audit log with failure")
    void shouldCreateAuthenticationAuditLogWithFailure() {
        auditService.auditAuthentication("LOGIN_FAILED", "testuser", "Invalid credentials");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("LOGIN_FAILED", savedLog.getAction());
        assertFalse(savedLog.getSuccess());
        assertEquals("Invalid credentials", savedLog.getError());
    }

    @Test
    @DisplayName("Should create security audit log")
    void shouldCreateSecurityAuditLog() {
        auditService.auditSecurity("RATE_LIMIT_EXCEEDED", "Rate limit exceeded for IP");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("RATE_LIMIT_EXCEEDED", savedLog.getAction());
        assertEquals("Security", savedLog.getEntityType());
        assertFalse(savedLog.getSuccess());
        assertEquals("Rate limit exceeded for IP", savedLog.getError());
    }

    @Test
    @DisplayName("Should use ANONYMOUS when there is no authentication")
    void shouldUseAnonymousWhenThereIsNoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        auditService.audit("TASK_CREATED", "Task", 1L, "Details");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("ANONYMOUS", captor.getValue().getUsername());
    }

    @Test
    @DisplayName("Should extract IP from X-Forwarded-For")
    void shouldExtractIpFromXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");

        auditService.audit("TASK_CREATED", "Task", 1L, "Details");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("10.0.0.1", captor.getValue().getIpAddress());
    }

    @Test
    @DisplayName("Should extract IP from X-Real-IP")
    void shouldExtractIpFromXRealIp() {
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.1");

        auditService.audit("TASK_CREATED", "Task", 1L, "Details");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("10.0.0.1", captor.getValue().getIpAddress());
    }

    @Test
    @DisplayName("Should truncate very long User-Agent")
    void shouldTruncateVeryLongUserAgent() {
        String longUserAgent = "A".repeat(600);
        when(request.getHeader("User-Agent")).thenReturn(longUserAgent);

        auditService.audit("TASK_CREATED", "Task", 1L, "Details");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals(500, captor.getValue().getUserAgent().length());
    }

    @Test
    @DisplayName("Should handle exception when saving audit log")
    void shouldHandleExceptionWhenSavingAuditLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Should not throw exception, only log
        assertDoesNotThrow(() -> {
            auditService.audit("TASK_CREATED", "Task", 1L, "Details");
        });

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
