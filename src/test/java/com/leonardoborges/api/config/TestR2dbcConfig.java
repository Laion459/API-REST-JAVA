package com.leonardoborges.api.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration to completely disable R2DBC.
 * 
 * Excludes R2DBC auto-configurations to avoid conflicts with JPA in tests.
 * SqlInitializationAutoConfiguration is also excluded to prevent SQL script initialization via R2DBC.
 */
@TestConfiguration
@Profile("test")
@EnableAutoConfiguration(exclude = {
    R2dbcDataAutoConfiguration.class,
    R2dbcRepositoriesAutoConfiguration.class,
    R2dbcAutoConfiguration.class,
    SqlInitializationAutoConfiguration.class
})
public class TestR2dbcConfig {
}
