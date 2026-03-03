# ADR-006: Database Sharding Preparation

**Status:** Accepted  
**Date:** 2025-02-XX  
**Deciders:** Architecture Team

## Context

As the application scales, a single database may become a bottleneck. We need to prepare for horizontal scaling.

## Decision

We will implement **Sharding Infrastructure**:
- `ShardingConfig` with shard key extraction
- User-based sharding strategy (shard key = user_id)
- Foundation for multi-database deployment
- Conditional activation (disabled by default)

## Consequences

### Positive
- Prepared for horizontal scaling
- Clear sharding strategy
- Easy to enable when needed
- No impact when disabled

### Negative
- Additional configuration
- Requires sharding proxy for full implementation

## Implementation

- `ShardingConfig` with `ShardKeyExtractor`
- User-based sharding (modulo operation)
- Conditional on `app.sharding.enabled=true`
