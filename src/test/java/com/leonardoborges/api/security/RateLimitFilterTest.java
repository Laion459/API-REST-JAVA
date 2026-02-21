package com.leonardoborges.api.security;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Bucket defaultBucket;

    @Test
    void testRateLimit_WithinLimit() throws Exception {
        // Reset bucket for test
        defaultBucket.reset();
        
        // Make requests within limit
        for (int i = 0; i < 10; i++) {
            var result = mockMvc.perform(get("/actuator/health"))
                    .andReturn();
            // Health endpoint may return 200 or 503 depending on Redis, but should not be 429
            assertTrue(result.getResponse().getStatus() != 429, 
                    "Request should not be rate limited");
        }
    }

    @Test
    void testRateLimit_ExceedsLimit() throws Exception {
        // Reset bucket for test
        defaultBucket.reset();
        
        // Exhaust bucket
        for (int i = 0; i < 60; i++) {
            defaultBucket.tryConsume(1);
        }
        
        // Next request should be rate limited
        var result = mockMvc.perform(get("/actuator/health"))
                .andReturn();
        assertEquals(429, result.getResponse().getStatus(), 
                "Request should be rate limited (429 Too Many Requests)");
    }
}
