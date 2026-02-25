package com.leonardoborges.api.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração para distributed tracing.
 * Integra com Micrometer Tracing para observabilidade completa.
 */
@Configuration
public class TracingConfig {
    
    /**
     * Bean para adicionar trace ID aos logs.
     * Permite correlacionar logs de diferentes serviços.
     */
    @Bean
    public TraceIdLoggingFilter traceIdLoggingFilter() {
        return new TraceIdLoggingFilter();
    }
    
    /**
     * Filter para adicionar trace ID aos MDC (Mapped Diagnostic Context).
     */
    public static class TraceIdLoggingFilter {
        // Spring Boot 3.x already integrates trace IDs automatically via Micrometer
        // This bean serves as a placeholder for future configurations
    }
}
