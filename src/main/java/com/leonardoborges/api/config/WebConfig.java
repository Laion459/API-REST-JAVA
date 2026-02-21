package com.leonardoborges.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS and other web-related settings.
 * Note: CORS is primarily configured in SecurityConfig for security filter chain.
 * This configuration is kept for backward compatibility but SecurityConfig takes precedence.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final CorsProperties corsProperties;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Use configurable CORS settings
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.getAllowedOriginsList().toArray(new String[0]))
                .allowedMethods(corsProperties.getAllowedMethodsList().toArray(new String[0]))
                .allowedHeaders(corsProperties.getAllowedHeadersList().toArray(new String[0]))
                .exposedHeaders(corsProperties.getExposedHeadersList().toArray(new String[0]))
                .allowCredentials(corsProperties.getAllowCredentials())
                .maxAge(corsProperties.getMaxAge());
    }
}
