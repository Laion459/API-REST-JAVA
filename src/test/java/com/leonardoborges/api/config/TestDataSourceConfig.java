package com.leonardoborges.api.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * Test datasource configuration that auto-detects driver from JDBC URL.
 * This ensures the correct driver is used when SPRING_DATASOURCE_DRIVER is not set.
 */
@TestConfiguration
public class TestDataSourceConfig {
    
    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties(Environment env) {
        DataSourceProperties properties = new DataSourceProperties();
        String url = env.getProperty("spring.datasource.url", "jdbc:h2:mem:testdb");
        String explicitDriver = env.getProperty("spring.datasource.driver-class-name");
        
        properties.setUrl(url);
        properties.setUsername(env.getProperty("spring.datasource.username", "sa"));
        properties.setPassword(env.getProperty("spring.datasource.password", ""));
        
        // Auto-detect driver from URL if not explicitly set
        if (explicitDriver != null && !explicitDriver.isEmpty()) {
            properties.setDriverClassName(explicitDriver);
        } else {
            // Auto-detect from URL
            if (url.startsWith("jdbc:postgresql:")) {
                properties.setDriverClassName("org.postgresql.Driver");
            } else if (url.startsWith("jdbc:h2:")) {
                properties.setDriverClassName("org.h2.Driver");
            } else if (url.startsWith("jdbc:mysql:")) {
                properties.setDriverClassName("com.mysql.cj.jdbc.Driver");
            } else {
                // Default to H2 for tests
                properties.setDriverClassName("org.h2.Driver");
            }
        }
        
        return properties;
    }
}
