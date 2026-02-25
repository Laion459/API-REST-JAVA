package com.leonardoborges.api.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração de teste para desabilitar completamente R2DBC.
 * 
 * Boas práticas aplicadas:
 * - Desabilita auto-configuração R2DBC para evitar conflitos com JPA
 * - Usa @Profile("test") para garantir que só carrega em testes
 * - Simples e direto, sem ComponentScan que pode interferir
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
    // R2DBC is disabled via:
    // 1. @Profile("!test") in production R2dbcConfig
    // 2. spring.r2dbc.enabled=false in application-test.yml
    // 3. Exclusion of auto-configurations above
}
