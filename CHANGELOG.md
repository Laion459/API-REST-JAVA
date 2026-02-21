# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned Improvements
- JWT authentication and authorization
- Optimized cache strategy
- Real reactive programming with R2DBC
- Comprehensive CI/CD pipeline
- Rate limiting
- Performance testing automation
- Optimistic locking for concurrency
- Custom metrics
- Architecture decision records

## [1.1.0] - 2025-01-XX

### Added
- Custom exception hierarchy (BaseException)
- TaskNotFoundException for task-specific errors
- ValidationException for validation errors
- BusinessException for business rule violations
- ResourceNotFoundException for generic resource errors
- Error code field in ErrorResponse DTO

### Changed
- Replaced generic RuntimeException with custom exceptions in TaskService
- Improved GlobalExceptionHandler with specific exception handlers
- Added errorCode to all error responses
- Updated HTTP status codes (422 for business errors)
- Enhanced error messages with resource IDs

### Fixed
- Better error traceability with error codes
- Consistent error response structure
- Proper HTTP status codes for different error types

### Tests
- Updated tests to use TaskNotFoundException
- Added tests for update and delete error scenarios
- Improved test coverage for exception handling

## [1.0.0] - 2025-01-XX

### Added
- Initial project structure with Spring Boot 3.2.0
- Java 21 (LTS) support
- REST API for task management (CRUD operations)
- PostgreSQL database integration
- Redis caching implementation
- Spring WebFlux reactive endpoints
- Swagger/OpenAPI documentation
- Prometheus metrics and monitoring
- Docker and Docker Compose setup
- Unit and integration tests (18 tests)
- Global exception handler
- DTO pattern implementation
- JPA auditing (createdAt, updatedAt)
- Database indexing for performance
- HTTP compression
- Health checks via Spring Actuator

### Technical Stack
- Java 21
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL 16
- Redis 7
- Spring WebFlux
- Swagger/OpenAPI 2.3.0
- Prometheus
- Docker & Docker Compose
- Maven

### Known Issues
- Uses generic RuntimeException instead of custom exceptions
- Cache evict strategy is too aggressive (allEntries = true)
- Reactive controller is not truly reactive (wraps blocking code)
- No authentication/authorization
- No rate limiting
- CI/CD pipeline not fully implemented
