package com.leonardoborges.api;

import com.leonardoborges.api.config.JpaAuditingConfig;
import com.leonardoborges.api.config.TestHibernateCacheConfig;
import com.leonardoborges.api.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, JpaAuditingConfig.class, TestHibernateCacheConfig.class})
class HighPerformanceApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
