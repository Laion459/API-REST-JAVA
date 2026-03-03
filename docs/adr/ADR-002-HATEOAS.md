# ADR-002: HATEOAS Implementation

**Status:** Accepted  
**Date:** 2025-02-XX  
**Deciders:** Architecture Team

## Context

REST APIs should be hypermedia-driven to enable client navigation without hardcoded URLs.

## Decision

We will implement **HATEOAS (Hypermedia as the Engine of Application State)**:
- Add `Link` objects to responses
- Provide navigation links (self, update, delete, etc.)
- Enable pagination links
- Make API more discoverable

## Consequences

### Positive
- Better API discoverability
- Clients don't need hardcoded URLs
- Follows REST principles completely
- Enables API evolution

### Negative
- Slightly larger response payloads
- Additional complexity in response building

## Implementation

- `Link` DTO for hypermedia links
- `HateoasHelper` utility for link generation
- Optional: Can be enabled/disabled via configuration
