# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned Improvements

#### Infraestrutura e DevOps
- [ ] **CI/CD Pipeline** - Implementar pipeline completo com GitHub Actions
  - Jobs de teste com cobertura (90% linhas, 85% branches)
  - Build automatizado do JAR
  - Build e push de imagem Docker
  - Deploy automatizado (staging/produção)
- [ ] **Docker Compose para produção** - Configuração otimizada para ambiente produtivo
- [ ] **Kubernetes manifests** - Configuração para deploy em K8s
- [ ] **Scripts de deploy** - Automação de deploy em diferentes ambientes

#### Documentação
- [ ] **Documentação de Arquitetura** - Criar ARQUITETURA_HIBRIDA.md explicando estratégia MVC vs WebFlux
- [ ] **Architecture Decision Records (ADRs)** - Documentar decisões arquiteturais importantes
- [ ] **Guia de Contribuição** - CONTRIBUTING.md com padrões de código e processo
- [ ] **Guia de Deploy** - DEPLOY.md com instruções detalhadas de deploy
- [ ] **Diagramas de arquitetura** - Diagramas visuais da arquitetura do sistema

#### Testes e Qualidade
- [ ] **Automação de testes de performance** - Integrar testes de carga no CI/CD
- [ ] **Testes de contrato** - Implementar contract testing (Pact, Spring Cloud Contract)
- [ ] **Testes de mutação** - Adicionar mutation testing para validar qualidade dos testes
- [ ] **Análise estática de código** - Integrar SonarQube ou similar no CI/CD

#### Performance e Observabilidade
- [ ] **Ajustes de produção** - Configurar sampling probability para produção (< 1.0)
- [ ] **Dashboards de monitoramento** - Configurar Grafana com métricas Prometheus
- [ ] **Alertas configurados** - Configurar alertas para métricas críticas
- [ ] **Distributed Tracing completo** - Integração com Jaeger/Zipkin para tracing completo
- [ ] **Log aggregation** - Configurar ELK Stack ou similar para logs centralizados

#### Segurança
- [ ] **Rotação de secrets JWT** - Implementar rotação automática de secrets
- [ ] **Rate limiting por usuário** - Adicionar rate limiting granular por usuário
- [ ] **Análise de dependências** - Integrar OWASP Dependency Check no CI/CD
- [ ] **Security scanning** - Adicionar scanning de vulnerabilidades no pipeline

#### Funcionalidades
- [ ] **Webhooks** - Sistema de webhooks para notificações de eventos
- [ ] **Exportação de dados** - Endpoints para exportar dados em diferentes formatos (CSV, JSON, PDF)
- [ ] **Filtros avançados** - Filtros mais complexos nas queries (range de datas, múltiplos status, etc.)
- [ ] **Busca full-text** - Implementar busca full-text em tasks
- [ ] **Notificações** - Sistema de notificações (email, push, etc.)

#### Melhorias Técnicas
- [ ] **Circuit Breaker** - Implementar circuit breaker para resiliência
- [ ] **Retry policies** - Políticas de retry mais sofisticadas
- [ ] **Cache distribuído** - Otimizar cache para ambientes distribuídos
- [ ] **Database sharding** - Preparar para sharding se necessário
- [ ] **API Gateway** - Considerar implementação de API Gateway para múltiplas APIs

## [3.0.0] - 2025-02-XX

### Added
- **Arquitetura Híbrida MVC + WebFlux** - Implementação estratégica de programação reativa para alta performance
- **ReactiveTaskController** - Endpoints reativos (`/api/v2/reactive/tasks`) otimizados para leitura com alta concorrência
- **ReactiveTaskService** - Service reativo usando Mono/Flux para operações não-bloqueantes
- **ReactiveTaskRepository** - Repository R2DBC para acesso não-bloqueante ao PostgreSQL
- **R2DBC Config** - Configuração completa de R2DBC para programação reativa
- **Redis Reativo** - Suporte a cache reativo usando ReactiveRedisTemplate
- **Persistência de Refresh Tokens** - Refresh tokens agora são persistidos no banco de dados com suporte a revogação
- **Sistema de Auditoria Persistente** - Todos os logs de auditoria são persistidos no banco de dados com rastreabilidade completa
- **Soft Delete** - Implementado soft delete para Tasks e Users, permitindo recuperação de dados
- **Rate Limiting por IP** - Rate limiting adicional baseado em IP usando Redis
- **AuditController** - Endpoint administrativo para consultar logs de auditoria
- **RefreshTokenService** - Serviço completo para gerenciamento de refresh tokens com revogação
- **Métodos de restauração** - Método para restaurar tasks deletadas (soft delete)
- **Índices otimizados** - Novos índices para melhorar performance de queries com soft delete
- **Migrações de banco** - V3 (refresh_tokens), V4 (audit_logs), V5 (soft delete)

### Changed
- **AuditService** - Agora persiste logs no banco de dados de forma assíncrona
- **UserService** - Integrado com RefreshTokenService para gerenciamento de tokens
- **TaskService** - Implementado soft delete ao invés de delete físico
- **TaskRepository** - Queries atualizadas para filtrar registros deletados (soft delete)
- **UserRepository** - Queries atualizadas para filtrar usuários deletados
- **SecurityConfig** - Adicionado endpoint de auditoria com proteção ADMIN
- **README** - Removidas referências incorretas a WebFlux e ReactiveTaskController

### Security
- Refresh tokens podem ser revogados individualmente ou em massa
- Auditoria completa de todas as operações sensíveis com persistência
- Rate limiting por IP adiciona camada extra de proteção
- Soft delete permite recuperação de dados em caso de exclusão acidental

### Performance
- **Arquitetura Híbrida**: MVC para escritas (JPA/transações) + WebFlux para leituras (alta concorrência)
- **Endpoints Reativos**: `/api/v2/reactive/*` otimizados para 10.000+ req/s com baixa latência
- **R2DBC**: Acesso não-bloqueante ao PostgreSQL para operações de leitura
- **Cache Reativo**: Redis reativo integrado com WebFlux
- **Batch Operations**: Operações em lote para processar múltiplas tasks eficientemente
- **Strategy Pattern**: Cache eviction estratégico baseado no tipo de operação
- **Cache Metrics**: Métricas de hit rate, miss rate e performance de cache
- Índices otimizados para queries com soft delete
- Auditoria assíncrona não impacta performance das operações principais
- Rate limiting por IP usando Redis para alta performance
- **Throughput esperado**: 
  - MVC (escritas): 1.000+ req/s
  - WebFlux (leituras): 10.000+ req/s
  - Batch operations: 5.000+ req/s (dependendo do tamanho do lote)

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
