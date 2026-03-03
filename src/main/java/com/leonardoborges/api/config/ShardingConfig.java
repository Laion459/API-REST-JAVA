package com.leonardoborges.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for database sharding preparation.
 * 
 * This configuration provides the foundation for horizontal sharding
 * when the application needs to scale beyond a single database instance.
 * 
 * Sharding strategy: User-based sharding (shard key = user_id)
 * 
 * Note: Full sharding implementation requires:
 * - Sharding proxy (e.g., ShardingSphere)
 * - Multiple database instances
 * - Shard key routing logic
 * - Data migration strategy
 */
@Configuration
@ConditionalOnProperty(name = "app.sharding.enabled", havingValue = "true", matchIfMissing = false)
public class ShardingConfig {
    
    /**
     * Shard key extractor for user-based sharding.
     * Routes requests to appropriate shard based on user_id.
     */
    public static class ShardKeyExtractor {
        
        /**
         * Extracts shard key from user ID.
         * Uses modulo operation for shard routing.
         * 
         * @param userId The user ID
         * @param totalShards Total number of shards
         * @return Shard number (0 to totalShards-1)
         */
        public static int extractShardKey(Long userId, int totalShards) {
            if (userId == null) {
                return 0; // Default shard for null user
            }
            return (int) (userId % totalShards);
        }
        
        /**
         * Gets shard name for a user.
         * 
         * @param userId The user ID
         * @param totalShards Total number of shards
         * @return Shard name (e.g., "shard_0", "shard_1")
         */
        public static String getShardName(Long userId, int totalShards) {
            int shardKey = extractShardKey(userId, totalShards);
            return "shard_" + shardKey;
        }
    }
    
    /**
     * Sharding metadata for tracking shard distribution.
     */
    public static class ShardingMetadata {
        private final int totalShards;
        private final String shardingStrategy;
        
        public ShardingMetadata(int totalShards, String shardingStrategy) {
            this.totalShards = totalShards;
            this.shardingStrategy = shardingStrategy;
        }
        
        public int getTotalShards() {
            return totalShards;
        }
        
        public String getShardingStrategy() {
            return shardingStrategy;
        }
    }
}
