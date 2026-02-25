package com.leonardoborges.api.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Configuração R2DBC para programação reativa com PostgreSQL.
 * Usado apenas para endpoints reativos de alta performance.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.leonardoborges.api.repository.reactive")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "spring.r2dbc.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
@org.springframework.context.annotation.Profile({"!test", "!integration-test"})
@org.springframework.boot.autoconfigure.condition.ConditionalOnClass(name = "io.r2dbc.spi.ConnectionFactory")
public class R2dbcConfig extends AbstractR2dbcConfiguration {
    
    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/tasksdb}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username:postgres}")
    private String username;
    
    @Value("${spring.datasource.password:postgres}")
    private String password;
    
    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        // Parse JDBC URL to R2DBC format
        String r2dbcUrl = datasourceUrl.replace("jdbc:postgresql://", "");
        String[] parts = r2dbcUrl.split("/");
        String hostPort = parts[0];
        String database = parts.length > 1 ? parts[1] : "tasksdb";
        
        String[] hostPortParts = hostPort.split(":");
        String host = hostPortParts[0];
        int port = hostPortParts.length > 1 ? Integer.parseInt(hostPortParts[1]) : 5432;
        
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host)
                        .port(port)
                        .database(database)
                        .username(username)
                        .password(password)
                        .build()
        );
    }
}
