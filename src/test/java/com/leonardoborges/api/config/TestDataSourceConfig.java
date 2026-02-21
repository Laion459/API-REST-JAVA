package com.leonardoborges.api.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Test datasource configuration that auto-detects driver from JDBC URL.
 * This ensures the correct driver is used when SPRING_DATASOURCE_DRIVER is not set.
 */
@TestConfiguration
public class TestDataSourceConfig {
    
    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String url = env.getProperty("spring.datasource.url", "jdbc:h2:mem:testdb");
        String username = env.getProperty("spring.datasource.username", "sa");
        String password = env.getProperty("spring.datasource.password", "");
        String explicitDriver = env.getProperty("spring.datasource.driver-class-name");
        
        // Auto-detect driver from URL if not explicitly set
        String driverClassName;
        if (explicitDriver != null && !explicitDriver.isEmpty()) {
            driverClassName = explicitDriver;
        } else {
            // Auto-detect from URL
            if (url.startsWith("jdbc:postgresql:")) {
                driverClassName = "org.postgresql.Driver";
            } else if (url.startsWith("jdbc:h2:")) {
                driverClassName = "org.h2.Driver";
            } else if (url.startsWith("jdbc:mysql:")) {
                driverClassName = "com.mysql.cj.jdbc.Driver";
            } else {
                // Default to H2 for tests
                driverClassName = "org.h2.Driver";
            }
        }
        
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }
}
