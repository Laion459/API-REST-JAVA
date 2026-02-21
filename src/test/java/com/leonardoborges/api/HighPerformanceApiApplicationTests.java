package com.leonardoborges.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles("test")
class HighPerformanceApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
