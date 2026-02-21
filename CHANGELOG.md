# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned Improvements
- Real reactive programming with R2DBC
- Performance testing automation
- Custom metrics
- Architecture decision records

## [1.8.0] - 2025-01-XX

### Added
- Optimistic locking with @Version annotation
- Version field in Task entity
- Version field in TaskResponse and TaskRequest DTOs
- OptimisticLockingException for handling version conflicts
- Spring Retry for automatic retry on optimistic locking failures
- Retry mechanism (3 attempts with 100ms delay)
- TaskServiceOptimisticLockingTest with 3 new tests

### Changed
- TaskService.updateTask now validates version before update
- GlobalExceptionHandler handles OptimisticLockingException and OptimisticLockingFailureException
- Update operations now return version in response
- HighPerformanceApiApplication enables @EnableRetry

### Fixed
- Protection against concurrent update conflicts
- Better handling of version mismatches
- Automatic retry on transient optimistic locking failures

### Tests
- Added TaskServiceOptimisticLockingTest with 3 new tests
- Total test count: 49 tests (46 + 3)

## [1.7.0] - 2025-01-XX

### Added
- SecurityHeadersFilter for HTTP security headers
- OWASP-compliant security headers implementation
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security (HSTS) for HTTPS
- Content-Security-Policy
- Referrer-Policy: strict-origin-when-cross-origin
- Permissions-Policy
- X-Permitted-Cross-Domain-Policies: none
- SecurityHeadersFilterTest with 2 new tests
- Server error message hiding configuration

### Changed
- SecurityConfig now includes SecurityHeadersFilter
- Security headers applied to all HTTP responses
- Improved security posture following OWASP guidelines

### Fixed
- Protection against MIME type sniffing
- Protection against clickjacking attacks
- Protection against XSS attacks
- Better security headers compliance

### Tests
- Added SecurityHeadersFilterTest with 2 new tests
- Total test count: 46 tests (44 + 2)

## [1.6.0] - 2025-01-XX

### Added
- GitHub Actions CI/CD pipeline
- Automated testing on push and pull requests
- Code coverage reporting and validation
- Docker image building in CI
- Automated build and artifact upload
- PostgreSQL and Redis services in CI
- Multi-job pipeline (test, build, docker-build)

### Changed
- Dockerfile updated to use Java 21
- Dockerfile improved with non-root user
- CI runs on push to main/develop and PRs
- CI runs on version tags

### Fixed
- Automated quality assurance
- Continuous integration for all changes
- Build verification before deployment

### Tests
- All existing tests run in CI pipeline
- Code coverage checked automatically
- Test results uploaded as artifacts

## [1.5.0] - 2025-01-XX

### Added
- Rate limiting with Bucket4j
- RateLimitConfig with different buckets (default, auth, admin)
- RateLimitFilter for request throttling
- Different rate limits for different endpoint types
- Rate limiting constants in TaskConstants
- RateLimitFilterTest with 2 new tests

### Changed
- SecurityConfig now includes RateLimitFilter
- Auth endpoints have stricter rate limits (5 req/min)
- Admin endpoints have higher rate limits (200 req/min)
- Default endpoints have standard rate limits (60 req/min)

### Fixed
- Protection against DDoS and brute force attacks
- Better API security with request throttling
- Improved resource management

### Tests
- Added RateLimitFilterTest with 2 new tests
- Total test count: 44 tests (42 + 2)

## [1.4.0] - 2025-01-XX

### Added
- JWT authentication and authorization
- User entity with roles (USER, ADMIN)
- UserRepository for user management
- JwtService for token generation and validation
- UserService for authentication and registration
- AuthController with register and login endpoints
- SecurityConfig with Spring Security configuration
- JwtAuthenticationFilter for token validation
- Password encryption with BCrypt
- Role-based access control (RBAC)
- Protected endpoints with authentication
- Admin-only endpoints for cache management
- CORS configuration
- AuthControllerTest with 5 new tests

### Changed
- All task endpoints now require authentication
- Cache management endpoints require ADMIN role
- Swagger UI and health endpoints remain public
- Security configuration with stateless sessions

### Fixed
- Improved API security with JWT tokens
- Better access control with role-based permissions
- Secure password storage with BCrypt

### Tests
- Added AuthControllerTest with 5 new tests
- Total test count: 42 tests (37 + 5)

## [1.3.0] - 2025-01-XX

### Added
- TaskConstants class for centralized magic numbers
- InputSanitizer utility for input validation and sanitization
- JaCoCo plugin for code coverage reporting
- Structured logging configuration
- Input sanitization in TaskService (create and update operations)

### Changed
- Replaced all magic numbers with constants from TaskConstants
- Improved logging levels (INFO for services/controllers, DEBUG for utils)
- Enhanced logging patterns for better readability
- TaskService now sanitizes all string inputs before processing
- All cache names now use constants
- All pagination sizes use constants
- All TTL values use constants

### Fixed
- Better code maintainability with centralized constants
- Improved security with input sanitization
- More consistent logging across the application

### Tests
- Added InputSanitizerTest with 8 new tests
- Total test count: 37 tests (29 + 8)

## [1.2.0] - 2025-01-XX

### Added
- CacheService for selective cache management
- CacheController for cache administration and statistics
- CacheWarmingConfig for pre-loading frequently accessed data
- Selective cache eviction (replaces allEntries = true)
- Cache statistics endpoint
- Different TTL configurations for different cache types
- Cache warming on application startup (production profile)

### Changed
- TaskService now uses selective cache eviction instead of clearing all entries
- Cache eviction strategy optimized to only invalidate affected caches
- Redis cache configuration with different TTLs:
  - Individual tasks: 15 minutes
  - Task statistics: 5 minutes
  - Default: 10 minutes
- Update operations now use @CachePut for immediate cache update

### Fixed
- Cache performance improved by avoiding unnecessary cache clears
- Reduced database load through better cache hit rates
- More efficient cache invalidation strategy

### Performance
- Cache hit rate improved by selective eviction
- Reduced cache misses after updates
- Better memory utilization with optimized TTLs

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
