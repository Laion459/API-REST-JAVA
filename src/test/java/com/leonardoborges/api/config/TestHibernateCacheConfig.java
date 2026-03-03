package com.leonardoborges.api.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

/**
 * Test configuration to disable Hibernate second-level cache and provide test Redis beans.
 * Prevents JCache provider initialization errors in tests.
 */
@TestConfiguration
@Profile("test")
public class TestHibernateCacheConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (Map<String, Object> hibernateProperties) -> {
            hibernateProperties.put("hibernate.cache.use_second_level_cache", false);
            hibernateProperties.put("hibernate.cache.use_query_cache", false);
        };
    }

    @Bean
    @Primary
    public org.springframework.data.redis.connection.RedisConnectionFactory testRedisConnectionFactory() {
        return new org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory();
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> testRedisTemplate(
            org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
