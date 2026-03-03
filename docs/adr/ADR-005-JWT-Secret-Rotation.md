# ADR-005: JWT Secret Rotation

**Status:** Accepted  
**Date:** 2025-02-XX  
**Deciders:** Architecture Team

## Context

JWT secrets should be rotated periodically for security. Manual rotation is error-prone.

## Decision

We will implement **JWT Secret Rotation Infrastructure**:
- `JwtSecretRotationService` for rotation management
- Support for current and previous secrets during rotation
- Scheduled checks for rotation needs
- Graceful rotation without invalidating all tokens

## Consequences

### Positive
- Better security posture
- Automated rotation checks
- Graceful rotation process
- Foundation for full automation

### Negative
- Additional service complexity
- Requires coordination with token invalidation

## Implementation

- `JwtSecretRotationService` provides rotation infrastructure
- Scheduled checks (daily)
- Support for dual-secret validation during rotation
