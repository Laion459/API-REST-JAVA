# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.1.0] - 2025-02-XX

### Added
- **Value Objects (DDD)** - TaskTitle, TaskPriority, TaskDescription encapsulating business rules
- **Granular Permissions System** - Permission enum with 15+ specific permissions (task:create, task:read, etc.)
- **PermissionService** - Service for managing fine-grained permissions beyond roles
- **User-based Rate Limiting** - UserRateLimitService for per-user rate limiting using Redis
- **SSRF Protection** - SsrfValidator to prevent Server-Side Request Forgery attacks
- **Hibernate Second-Level Cache** - HibernateCacheConfig with distributed entity caching
- **Production Configuration** - application-prod.yml with optimized production settings
- **BaseValidationService** - Centralized validation service to reduce code duplication
- Enhanced TaskRequest validation (priority range 0-100)
- Cache annotations on Task and User entities (@Cacheable, @Cache)

### Changed
- RateLimitFilter now checks user-based limits before IP-based limits
- TaskRequest priority validation updated to 0-100 range
- TaskValidationService refactored to use BaseValidationService
- Enhanced security with SSRF protection
- Improved performance with Hibernate second-level cache

### Security
- SSRF protection prevents requests to internal/private networks
- User-based rate limiting adds protection against distributed attacks
- Granular permissions enable fine-grained access control
- Enhanced URL validation for all external requests

### Performance
- Hibernate second-level cache reduces database queries
- Distributed caching across multiple application instances
- Query cache enabled for frequently accessed data

### Architecture
- Domain-Driven Design with Value Objects
- Clean Architecture principles with domain layer
- Reduced code duplication with BaseValidationService
- Enhanced separation of concerns

### Tests
- TaskTitleTest - Value Object tests
- TaskPriorityTest - Value Object tests
- PermissionServiceTest - Permission system tests
- UserRateLimitServiceTest - User-based rate limiting tests
- SsrfValidatorTest - SSRF protection tests
- BaseValidationServiceTest - Validation service tests

## [Unreleased]

### Added
- Expanded test coverage with 500+ automated tests
- Additional unit tests for Value Objects, permissions, and validation services
- Tests for edge cases including null handling, branch coverage scenarios, and error conditions

### Planned Improvements

#### Infrastructure and DevOps
- [ ] **CI/CD Pipeline** - Implement complete pipeline with GitHub Actions
  - Test jobs with coverage (90% lines, 85% branches)
  - Automated JAR build
  - Docker image build and push
  - Automated deployment (staging/production)
- [ ] **Docker Compose for production** - Optimized configuration for production environment
- [ ] **Kubernetes manifests** - Configuration for K8s deployment
- [ ] **Deployment scripts** - Deployment automation for different environments

#### Documentation
- [ ] **Architecture Documentation** - Create ARQUITETURA_HIBRIDA.md explaining MVC vs WebFlux strategy
- [ ] **Architecture Decision Records (ADRs)** - Document important architectural decisions
- [ ] **Contribution Guide** - CONTRIBUTING.md with code standards and process
- [ ] **Deployment Guide** - DEPLOY.md with detailed deployment instructions
- [ ] **Architecture diagrams** - Visual diagrams of the system architecture

#### Testing and Quality
- [ ] **Performance test automation** - Integrate load tests into CI/CD
- [ ] **Contract testing** - Implement contract testing (Pact, Spring Cloud Contract)
- [ ] **Mutation testing** - Add mutation testing to validate test quality
- [ ] **Static code analysis** - Integrate SonarQube or similar into CI/CD

#### Performance and Observability
- [ ] **Production adjustments** - Configure sampling probability for production (< 1.0)
- [ ] **Monitoring dashboards** - Configure Grafana with Prometheus metrics
- [ ] **Configured alerts** - Configure alerts for critical metrics
- [ ] **Complete Distributed Tracing** - Integration with Jaeger/Zipkin for complete tracing
- [ ] **Log aggregation** - Configure ELK Stack or similar for centralized logs

#### Security
- [ ] **JWT secret rotation** - Implement automatic secret rotation
- [x] **User-based rate limiting** - ✅ Implemented in v3.1.0
- [ ] **Dependency analysis** - Integrate OWASP Dependency Check into CI/CD
- [ ] **Security scanning** - Add vulnerability scanning to the pipeline

#### Features
- [ ] **Webhooks** - Webhook system for event notifications
- [ ] **Data export** - Endpoints to export data in different formats (CSV, JSON, PDF)
- [ ] **Advanced filters** - More complex filters in queries (date ranges, multiple statuses, etc.)
- [ ] **Full-text search** - Implement full-text search in tasks
- [ ] **Notifications** - Notification system (email, push, etc.)

#### Technical Improvements
- [ ] **Circuit Breaker** - Implement circuit breaker for resilience
- [ ] **Retry policies** - More sophisticated retry policies
- [ ] **Distributed cache** - Optimize cache for distributed environments
- [ ] **Database sharding** - Prepare for sharding if necessary
- [ ] **API Gateway** - Consider API Gateway implementation for multiple APIs

## [3.0.0] - 2025-02-XX

### Added
- **Hybrid MVC + WebFlux Architecture** - Strategic implementation of reactive programming for high performance
- **ReactiveTaskController** - Reactive endpoints (`/api/v2/reactive/tasks`) optimized for high-concurrency reads
- **ReactiveTaskService** - Reactive service using Mono/Flux for non-blocking operations
- **ReactiveTaskRepository** - R2DBC repository for non-blocking access to PostgreSQL
- **R2DBC Config** - Complete R2DBC configuration for reactive programming
- **Reactive Redis** - Reactive cache support using ReactiveRedisTemplate
- **Refresh Token Persistence** - Refresh tokens are now persisted in the database with revocation support
- **Persistent Audit System** - All audit logs are persisted in the database with complete traceability
- **Soft Delete** - Implemented soft delete for Tasks and Users, allowing data recovery
- **IP-based Rate Limiting** - Additional IP-based rate limiting using Redis
- **AuditController** - Administrative endpoint to query audit logs
- **RefreshTokenService** - Complete service for refresh token management with revocation
- **Restoration methods** - Method to restore deleted tasks (soft delete)
- **Optimized indexes** - New indexes to improve query performance with soft delete
- **Database migrations** - V3 (refresh_tokens), V4 (audit_logs), V5 (soft delete)

### Changed
- **AuditService** - Now persists logs in the database asynchronously
- **UserService** - Integrated with RefreshTokenService for token management
- **TaskService** - Implemented soft delete instead of physical delete
- **TaskRepository** - Updated queries to filter deleted records (soft delete)
- **UserRepository** - Updated queries to filter deleted users
- **SecurityConfig** - Added audit endpoint with ADMIN protection
- **README** - Removed incorrect references to WebFlux and ReactiveTaskController

### Security
- Refresh tokens can be revoked individually or in bulk
- Complete auditing of all sensitive operations with persistence
- IP-based rate limiting adds an extra layer of protection
- Soft delete allows data recovery in case of accidental deletion

### Performance
- **Hybrid Architecture**: MVC for writes (JPA/transactions) + WebFlux for reads (high concurrency)
- **Reactive Endpoints**: `/api/v2/reactive/*` optimized for 10,000+ req/s with low latency
- **R2DBC**: Non-blocking access to PostgreSQL for read operations
- **Reactive Cache**: Reactive Redis integrated with WebFlux
- **Batch Operations**: Batch operations to efficiently process multiple tasks
- **Strategy Pattern**: Strategic cache eviction based on operation type
- **Cache Metrics**: Hit rate, miss rate, and cache performance metrics
- Optimized indexes for queries with soft delete
- Asynchronous auditing does not impact main operation performance
- IP-based rate limiting using Redis for high performance
- **Expected throughput**: 
  - MVC (writes): 1,000+ req/s
  - WebFlux (reads): 10,000+ req/s
  - Batch operations: 5,000+ req/s (depending on batch size)

### Observability
- **Logs Estruturados JSON**: Logback configurado para logs JSON em produção
- **Distributed Tracing**: Micrometer Tracing integrado para rastreamento de requisições
- **Cache Metrics**: Métricas detalhadas de cache (hits, misses, hit rate)
- **Enhanced Error Handling**: Tratamento melhorado de exceções com códigos de erro específicos

### Database
- **Task History**: Tabela de histórico de mudanças para auditoria completa de dados
- **TaskHistoryService**: Service para registrar mudanças de campos automaticamente
- **TaskHistoryController**: Endpoints para consultar histórico de mudanças
- **Índices Otimizados**: Novos índices para queries de histórico
- **Migração V6**: Criação de tabela de histórico com índices

### Tests
- **SecurityTest**: Testes abrangentes de segurança (OWASP Top 10)
  - Proteção contra SQL Injection
  - Proteção contra XSS
  - Rate limiting
  - Autenticação e autorização
  - Prevenção de brute force
- **BatchTaskServiceTest**: Testes para operações em lote
- **CacheEvictionServiceTest**: Testes para Strategy Pattern de cache
- **RefreshTokenServiceTest**: Testes completos de refresh tokens
- **UserServiceRefreshTokenTest**: Testes de integração de refresh tokens
- **AuditControllerTest**: Testes de endpoints de auditoria
- **BatchTaskControllerTest**: Testes de endpoints batch
- **TaskHistoryServiceTest**: Testes de histórico de mudanças
- Cobertura de testes expandida para novas funcionalidades
- Total de testes: 65+ (aumento de 49 para 65+)

### Database
- Nova tabela `refresh_tokens` com índices otimizados
- Nova tabela `audit_logs` com índices para consultas eficientes
- Colunas de soft delete adicionadas a `tasks` e `users`
- Migrações Flyway para todas as mudanças

### Fixed
- Inconsistências entre README e código (WebFlux removido)
- Refresh tokens agora podem ser revogados antes do vencimento
- Auditoria agora é persistente e consultável
- Soft delete permite recuperação de dados

## [2.1.0] - 2025-02-XX

### Added
- **CORS configurável via properties** - CORS agora é configurável via `application.yml` com `CorsProperties`
- **Refresh Tokens** - Implementado sistema completo de refresh tokens para renovação de autenticação
- **Validação de JWT Secret** - `JwtProperties` valida que o secret está configurado corretamente (obrigatório em produção)
- **Sistema de Auditoria** - `AuditService` para log de operações sensíveis (criação, atualização, deleção de tasks, autenticação)
- **Connection Pooling explícito** - Configuração HikariCP com parâmetros otimizados
- **SortParameterValidator** - Utility class para validação e normalização de parâmetros de ordenação
- **SqlInjectionValidator** - Validação adicional de SQL injection (defense in depth)
- **Testes de Performance** - `TaskControllerPerformanceTest` para validação de tempos de resposta

### Changed
- **CORS mais seguro** - Não usa mais `*` por padrão, usa origens configuráveis
- **JWT Service** - Refatorado para usar `JwtProperties` ao invés de `@Value`
- **TaskService e UserService** - Integrados com `AuditService` para auditoria de operações
- **TaskController** - Usa `SortParameterValidator` para eliminar duplicação de código
- **AuthResponse** - Adicionado campo `refreshToken`

### Security
- CORS configurável e mais restritivo por padrão
- JWT secret validado no startup (obrigatório em produção)
- Refresh tokens para melhor segurança de autenticação
- Auditoria de todas as operações sensíveis
- **Prevenção de SQL Injection em múltiplas camadas** - Validação adicional além do JPA
- **Validação de sort fields** - Prevenção de SQL injection em parâmetros de ordenação

### Performance
- Connection pooling configurado explicitamente (HikariCP)
- Melhorias na validação de parâmetros (menos overhead)
- Testes de performance para garantir tempos de resposta aceitáveis


## [2.0.0] - 2025-01-XX

### Added
- Custom health indicators (DatabaseHealthIndicator, RedisHealthIndicator, CacheHealthIndicator)
- Custom business metrics (TaskMetrics) with Prometheus integration
- TaskMetrics tracks: created, updated, deleted, retrieved operations
- TaskMetrics tracks: operations by status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
- TaskMetrics tracks: operation duration timers (creation, update, retrieval)
- MetricsConfig for automatic metrics initialization
- Enhanced health endpoint with component details

### Changed
- Increased minimum code coverage from 80% to 90% (JaCoCo)
- Added branch coverage requirement (85% minimum)
- CI/CD now enforces 90% code coverage (fails build if not met)
- TaskService integrated with TaskMetrics for all operations
- Health endpoint shows detailed component status
- Management endpoints configured for better observability

### Fixed
- Better production readiness with comprehensive monitoring
- Improved observability with custom metrics
- Health checks for all critical components (DB, Redis, Cache)

### Tests
- Code coverage validation now requires 90%+ (was 80%)
- Branch coverage validation requires 85%+

## [1.9.0] - 2025-01-XX

### Added
- Comprehensive Swagger/OpenAPI documentation
- OpenApiConfig with API metadata and security scheme
- Complete HTTP response documentation for all endpoints
- @ApiResponses annotations for all status codes (200, 201, 400, 401, 403, 404, 409, 422, 429, 500)
- @Schema annotations for all DTOs with examples and descriptions
- Security requirements documented in Swagger
- ErrorResponse schema in all error responses
- Detailed descriptions for all endpoints

### Changed
- All controllers now have complete Swagger documentation
- TaskController: 7 endpoints fully documented
- AuthController: 2 endpoints fully documented
- CacheController: 5 endpoints fully documented
- All DTOs have @Schema annotations with examples

### Fixed
- Improved API documentation quality
- Better developer experience with complete Swagger UI
- All HTTP status codes properly documented

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
- Swagger/OpenAPI 2.3.0
- Prometheus
- Docker & Docker Compose
- Maven

### Known Issues
- Uses generic RuntimeException instead of custom exceptions
- Cache evict strategy is too aggressive (allEntries = true)
- No authentication/authorization
- No rate limiting
- CI/CD pipeline not fully implemented
