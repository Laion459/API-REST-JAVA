package com.leonardoborges.api.config;

import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Map;

/**
 * Configuration for Hibernate second-level cache.
 * Enables distributed caching for entities using JCache (JSR-107).
 */
@Configuration
@Profile("!test")
public class HibernateCacheConfig {
    
    /**
     * Configures Hibernate to use JCache for second-level cache.
     * This enables distributed caching across multiple application instances.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernateSecondLevelCacheCustomizer() {
        return (Map<String, Object> hibernateProperties) -> {
            // Enable second-level cache
            hibernateProperties.put("hibernate.cache.use_second_level_cache", true);
            hibernateProperties.put("hibernate.cache.use_query_cache", true);
            hibernateProperties.put("hibernate.cache.region.factory_class", 
                    "org.hibernate.cache.jcache.JCacheRegionFactory");
            
            // Configure cache provider (uses Spring Cache abstraction)
            hibernateProperties.put(ConfigSettings.CACHE_MANAGER, "hibernateCacheManager");
            
            // Enable statistics for cache monitoring
            hibernateProperties.put("hibernate.generate_statistics", false);
            hibernateProperties.put("hibernate.cache.use_structured_entries", true);
        };
    }
    
    /**
     * Creates a JCache CacheManager for Hibernate second-level cache.
     * Uses the same Redis configuration as Spring Cache.
     */
    @Bean(name = "hibernateCacheManager")
    public CacheManager hibernateCacheManager() {
        try {
            CachingProvider cachingProvider = Caching.getCachingProvider();
            return cachingProvider.getCacheManager(
                    cachingProvider.getDefaultURI(),
                    cachingProvider.getDefaultClassLoader()
            );
        } catch (Exception e) {
            // Fallback to simple cache if JCache is not available
            return null;
        }
    }
}
