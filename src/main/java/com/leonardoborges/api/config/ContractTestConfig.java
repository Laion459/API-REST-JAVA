package com.leonardoborges.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Contract Testing (Pact).
 * 
 * Contract testing ensures API compatibility between consumer and provider.
 * 
 * Note: Full Pact implementation requires:
 * - pact-jvm-consumer dependency
 * - pact-jvm-provider dependency
 * - Consumer contract definitions
 * - Provider verification
 * 
 * This configuration provides the foundation for contract testing.
 */
@Configuration
@ConditionalOnProperty(name = "app.contract-testing.enabled", havingValue = "true", matchIfMissing = false)
public class ContractTestConfig {
    
    /**
     * Contract testing metadata.
     */
    public static class ContractMetadata {
        private final String consumerName;
        private final String providerName;
        private final String version;
        
        public ContractMetadata(String consumerName, String providerName, String version) {
            this.consumerName = consumerName;
            this.providerName = providerName;
            this.version = version;
        }
        
        public String getConsumerName() {
            return consumerName;
        }
        
        public String getProviderName() {
            return providerName;
        }
        
        public String getVersion() {
            return version;
        }
    }
    
    /**
     * Default contract metadata.
     */
    public static ContractMetadata defaultMetadata() {
        return new ContractMetadata(
            "api-client",
            "high-performance-api",
            "3.2.0"
        );
    }
}
