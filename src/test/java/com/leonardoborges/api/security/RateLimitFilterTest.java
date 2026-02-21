package com.leonardoborges.api.security;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
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
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().is(429)); // Too Many Requests
    }
}
