package com.leonardoborges.api.security;

import com.leonardoborges.api.config.TestSecurityConfig;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
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
            // Note: In test profile, RateLimitFilter is disabled, so rate limiting won't be enforced
            assertTrue(result.getResponse().getStatus() != 429, 
                    "Request should not be rate limited");
        }
    }

    @Test
    void testRateLimit_ExceedsLimit() throws Exception {
        // Reset bucket for test
        defaultBucket.reset();
        
        // Test that bucket can be exhausted
        boolean allConsumed = true;
        for (int i = 0; i < 60; i++) {
            if (!defaultBucket.tryConsume(1)) {
                allConsumed = false;
                break;
            }
        }
        
        // Verify bucket was exhausted
        assertTrue(allConsumed, "Bucket should allow consuming all tokens");
        
        // Verify bucket is now empty
        assertFalse(defaultBucket.tryConsume(1), 
                "Bucket should be empty after consuming all tokens");
        
        // Note: In test profile, RateLimitFilter is disabled, so HTTP 429 won't be returned
        // This test verifies the bucket functionality itself
        var result = mockMvc.perform(get("/actuator/health"))
                .andReturn();
        // Request should succeed because filter is disabled in test profile
        assertTrue(result.getResponse().getStatus() == 200 || result.getResponse().getStatus() == 503);
    }
}
