package com.leonardoborges.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.leonardoborges.api.config.TestSecurityConfig.class)
class SecurityHeadersFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSecurityHeaders_Present() throws Exception {
        var result = mockMvc.perform(get("/actuator/health"))
                .andReturn();
        
        // Health endpoint may return 200 (UP) or 503 (DOWN) depending on Redis availability
        assertTrue(result.getResponse().getStatus() == 200 || result.getResponse().getStatus() == 503);
        
        // Verify security headers are present regardless of health status
        assertNotNull(result.getResponse().getHeader("X-Content-Type-Options"));
        assertNotNull(result.getResponse().getHeader("X-Frame-Options"));
        assertNotNull(result.getResponse().getHeader("X-XSS-Protection"));
        assertNotNull(result.getResponse().getHeader("Content-Security-Policy"));
        assertNotNull(result.getResponse().getHeader("Referrer-Policy"));
        assertNotNull(result.getResponse().getHeader("Permissions-Policy"));
        assertNotNull(result.getResponse().getHeader("X-Permitted-Cross-Domain-Policies"));
    }

    @Test
    void testSecurityHeaders_AllEndpoints() throws Exception {
        var result = mockMvc.perform(get("/api/v1/auth/register"))
                .andReturn();
        
        // Verify security headers are present regardless of status
        assertNotNull(result.getResponse().getHeader("X-Content-Type-Options"));
        assertNotNull(result.getResponse().getHeader("X-Frame-Options"));
    }
}
