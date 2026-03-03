# ADR-001: Application Service Layer

**Status:** Accepted  
**Date:** 2025-02-XX  
**Deciders:** Architecture Team

## Context

The current architecture mixes domain logic with application orchestration in service classes. This makes it difficult to distinguish between:
- Application use cases (orchestration)
- Domain business logic (rules)

## Decision

We will introduce an explicit **Application Service Layer** that:
- Orchestrates domain services
- Coordinates use cases
- Handles cross-cutting concerns (transactions, security)
- Sits between Controllers and Domain Services

## Consequences

### Positive
- Clear separation of concerns
- Better testability
- Easier to understand application flow
- Aligns with Clean Architecture

### Negative
- Additional layer (slight complexity increase)
- More classes to maintain

## Implementation

- `TaskApplicationService` orchestrates `TaskService`
- Controllers use Application Services
- Domain Services remain focused on business logic
