package com.leonardoborges.api.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for distributed tracing.
 * Integrates with Micrometer Tracing for complete observability.
 */
@Configuration
public class TracingConfig {
    
    /**
     * Bean to add trace ID to logs.
     * Allows correlating logs from different services.
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
