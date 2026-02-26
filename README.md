# High Performance REST API

High-performance REST API developed with Spring Boot for backend skills demonstration, focused on scalability, performance, and software engineering best practices.

## Status: v3.0.0

Reference project implementing all software engineering, security, and performance best practices. 
Includes hybrid MVC + WebFlux architecture for maximum performance. Production-ready.

## Objective

Demonstrate practical experience in backend development with Java/Spring Boot, including:
- High-performance REST APIs
- Concurrent programming and optimization
- Caching and query optimization
- Monitoring and metrics
- Automated testing
- Docker and CI/CD

## Technologies

- **Java 21** (LTS) - Programming language
- **Spring Boot 3.2.0** - Backend framework
- **Spring Data JPA** - Data persistence (MVC)
- **Spring WebFlux + R2DBC** - Reactive programming for high performance
- **PostgreSQL** - Relational database
- **Redis** - Caching and optimization (blocking and reactive)
- **Swagger/OpenAPI** - Automatic documentation
- **Prometheus** - Metrics and monitoring
- **Docker** - Containerization
- **Maven** - Dependency management

## Features

- Complete task CRUD
- **JWT Authentication with Refresh Tokens** (v1.4.0, v2.1.0)
- **Role-based Authorization** (USER, ADMIN)
- **Rate Limiting** (v1.5.0) - Abuse protection
- **Security Headers** (v1.7.0) - HTTP security headers (OWASP)
- **Optimistic Locking** (v1.8.0) - Concurrency control
- **Configurable CORS** (v2.1.0) - Enhanced security
- **Audit System** (v2.1.0) - Logging of sensitive operations
- **SQL Injection Prevention** (v2.1.0) - Multi-layer validation
- Pagination and filters
- Intelligent caching (Redis)
- Multi-layer data validation
- Standardized error handling
- **Complete Swagger Documentation** (v1.9.0) - All HTTP codes documented
- **Custom Health Checks** (v2.0.0) - DB, Redis, and Cache monitoring
- **Custom Metrics** (v2.0.0) - Business metrics with Prometheus
- **90%+ Test Coverage** (v2.0.0) - Mandatory validation in CI/CD
- **Performance Tests** (v2.1.0) - Response time validation
- Prometheus metrics
- Health checks
- Automated tests (unit, integration, performance)
- Docker and Docker Compose
- **CI/CD Pipeline** (v1.6.0) - GitHub Actions

## Arquitetura

```
src/
├── main/
│   ├── java/
│   │   └── com/leonardoborges/api/
│   │       ├── audit/           # Audit system
│   │       ├── cache/            # Cache management
│   │       ├── config/           # Configurations (Cache, Web, Security, etc)
│   │       ├── constants/        # System constants
│   │       ├── controller/      # REST controllers
│   │       ├── dto/              # Data Transfer Objects
│   │       ├── exception/        # Error handling
│   │       ├── health/           # Custom health checks
│   │       ├── mapper/           # DTO ↔ Entity mapping
│   │       ├── metrics/          # Custom metrics
│   │       ├── model/            # JPA entities
│   │       ├── repository/       # JPA and R2DBC repositories
│   │       ├── security/         # Security components
│   │       ├── service/          # Business logic
│   │       └── util/             # Utilities
│   └── resources/
│       ├── application.yml       # Configurations
│       ├── db/migration/         # Flyway migrations
│       └── logback-spring.xml    # Logging configuration
└── test/                         # Automated tests
```

## How to Run

### Prerequisites

- **Java 21** (LTS) - Install: `sudo apt install openjdk-21-jdk`
- Maven 3.6+ - Install: `sudo apt install maven`
- Docker and Docker Compose (optional)

### Option 0: Makefile (Easiest - Recommended)

The project includes a **complete Makefile** with useful commands to simplify development:

```bash
# View all available commands
make help

# Install dependencies and start Docker services
make install

# Complete quick start (Docker + Build + Run)
make quickstart

# Development (Docker + Run application)
make dev

# Run tests
make test

# Tests with coverage
make test-coverage

# Docker - simplified commands
make up      # Start containers
make down    # Stop containers
make ps      # List containers
make logs    # View logs

# Build and execution
make build   # Compile project
make run     # Run application
make clean   # Clean artifacts
```

**Most used commands:**

| Command | Description |
|---------|-------------|
| `make install` | Installs dependencies and starts Docker services |
| `make quickstart` | Complete quick start (everything automatic) |
| `make dev` | Development (Docker + application) |
| `make test` | Runs all tests |
| `make test-coverage` | Tests with coverage report |
| `make up` | Starts Docker containers |
| `make down` | Stops Docker containers |
| `make ps` | Lists running containers |
| `make logs` | Shows container logs |

To see all available commands: `make help`

### Option 1: Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL, Redis, API)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

The API will be available at: `http://localhost:8081`

### Option 2: Local Execution

1. **Install PostgreSQL and Redis** (or use Docker only for these services)

2. **Configure database:**
```sql
CREATE DATABASE tasksdb;
```

3. **Run the application:**
```bash
mvn spring-boot:run
```

### Option 3: Build and Run JAR

```bash
# Build
mvn clean package

# Run
java -jar target/high-performance-api-1.0.0.jar
```

## API Documentation

### Swagger UI
Access: `http://localhost:8081/swagger-ui.html`

The Swagger documentation is complete with:
- All endpoints documented
- All HTTP status codes (200, 201, 400, 401, 403, 404, 409, 422, 429, 500)
- Request and response examples
- Detailed descriptions for each endpoint
- JWT authentication schemas
- Documented validations and constraints
- DTOs with examples and descriptions

### Main Endpoints

**Authentication (v1.4.0+):**
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login and get JWT token
- `POST /api/v1/auth/refresh` - Refresh access token using refresh token

**Tasks (MVC - Writes and complex operations):** (Requires authentication)
- `POST /api/v1/tasks` - Create new task
- `GET /api/v1/tasks` - List all tasks (paginated)
- `GET /api/v1/tasks/{id}` - Get task by ID
- `GET /api/v1/tasks/status/{status}` - Filter by status
- `GET /api/v1/tasks/stats/count` - Statistics
- `PUT /api/v1/tasks/{id}` - Update task
- `DELETE /api/v1/tasks/{id}` - Delete task (soft delete)
- `GET /api/v1/tasks/{taskId}/history` - Task change history (paginated)
- `GET /api/v1/tasks/{taskId}/history/all` - Complete task history (no pagination)
- `GET /api/v1/tasks/{taskId}/history/field/{fieldName}` - History of a specific field
- `GET /api/v1/tasks/{taskId}/history/date-range` - History by date range

**Reactive Tasks (WebFlux - High Performance):** (Requires authentication)
- `GET /api/v2/reactive/tasks` - List tasks (reactive, high concurrency)
- `GET /api/v2/reactive/tasks/{id}` - Get task (reactive, low latency)
- `GET /api/v2/reactive/tasks/status/{status}` - Filter by status (reactive)
- `GET /api/v2/reactive/tasks/stats/count` - Statistics (reactive)

**Batch Operations:** (Requires authentication)
- `POST /api/v1/tasks/batch/create` - Create multiple tasks (up to 100)
- `PUT /api/v1/tasks/batch/update` - Update multiple tasks (up to 100)
- `DELETE /api/v1/tasks/batch/delete` - Delete multiple tasks (up to 100)

**Audit (Admin only):** (Requires ADMIN role)
- `GET /api/v1/audit` - List audit logs
- `GET /api/v1/audit/action/{action}` - Filter by action
- `GET /api/v1/audit/entity/{entityType}/{entityId}` - Filter by entity
- `GET /api/v1/audit/user/{username}` - Filter by user
- `GET /api/v1/audit/date-range` - Filter by date range
- `GET /api/v1/audit/stats/failed` - Failed actions statistics

**Note:** 
- Reactive endpoints (`/api/v2/reactive/*`) are optimized for high-concurrency reads (10,000+ req/s)
- Use MVC endpoints (`/api/v1/*`) for write operations that require complex transactions
- Batch operations are ideal for efficiently processing multiple operations

**Cache Management (v1.2.0+):** (Requires ADMIN role)
- `GET /api/v1/cache/stats` - Cache statistics
- `GET /api/v1/cache/tasks/{id}/cached` - Check if task is cached
- `DELETE /api/v1/cache/tasks/{id}` - Remove task from cache
- `DELETE /api/v1/cache/stats` - Clear statistics cache
- `DELETE /api/v1/cache/all` - Clear all caches (administrative)

### Rate Limiting (v1.5.0+)

The API implements rate limiting to protect against abuse:
- **Default endpoints**: 60 requests per minute
- **Authentication endpoints**: 5 requests per minute (brute force protection)
- **Administrative endpoints**: 200 requests per minute (for admins)

When the limit is exceeded, the API returns `429 Too Many Requests` with information about when to try again.

### Request Example

```bash
# 1. Register new user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# 2. Login and get token
RESPONSE=$(curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }')

TOKEN=$(echo $RESPONSE | jq -r '.token')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.refreshToken')

# 2.1. Refresh token (optional)
# TOKEN=$(curl -X POST http://localhost:8081/api/v1/auth/refresh \
#   -H "Content-Type: application/json" \
#   -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}" | jq -r '.token')

# 3. Create task (with authentication)
curl -X POST http://localhost:8081/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Implement feature X",
    "description": "Task description",
    "status": "PENDING",
    "priority": 1
  }'

# 4. List tasks (with authentication)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/tasks?page=0&size=20

# 5. Get by ID (with authentication)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/tasks/1
```

## Monitoring and Metrics

### Health Check
`GET http://localhost:8081/actuator/health`

### Prometheus Metrics
`GET http://localhost:8081/actuator/prometheus`

### Available Metrics
- `http_server_requests_seconds` - Request latency
- `jvm_memory_used_bytes` - Memory usage
- `jvm_gc_pause_seconds` - Garbage Collection
- `process_cpu_usage` - CPU usage

## Tests

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=TaskServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Coverage

- **475+ automated tests** covering all layers of the application
- **Target coverage: 90%** (lines) and **85%** (branches) - CI/CD validation
- **CI/CD minimum: 90%** (lines) and **85%** (branches) - Enforced in pom.xml
- Unit tests (Service, Repository, Controller, Cache, Utils)
- Integration tests (end-to-end)
- Authentication tests (JWT, login, registration, refresh tokens)
- Security tests (OWASP Top 10, SQL Injection, XSS, Rate Limiting)
- Batch operations tests
- Strategy Pattern tests (cache eviction)
- Tests with H2 (in-memory database)
- Testcontainers for integration tests
- JaCoCo configured for coverage reports

### Run Tests with Coverage

```bash
# Run tests and generate coverage report
mvn clean test jacoco:report

# View report (HTML)
open target/site/jacoco/index.html
```

## CI/CD Pipeline (v1.6.0+)

> **Note:** The CI/CD pipeline is planned for implementation. The structure below describes the pipeline that will be configured using GitHub Actions.

### Pipeline Jobs

1. **Test** - Runs all tests
   - Configures PostgreSQL and Redis as services
   - Runs tests with coverage
   - Validates minimum coverage (90% lines, 85% branches)
   - Uploads results and reports

2. **Build** - Compiles the application
   - JAR build
   - Artifact upload

3. **Docker Build** - Builds Docker image
   - Docker image build
   - Optimized cache

### Triggers

- Push to `main` or `develop`
- Pull requests to `main`
- Version tags (`v*`)

### Status Badge

Add to your README:
```markdown
![CI/CD](https://github.com/Laion459/API-REST-JAVA/workflows/CI%2FCD%20Pipeline/badge.svg)
```

## Performance

### Implemented Optimizations

- **Redis Cache** - Reduces database queries
  - Selective evict (v1.2.0) - Invalidates only affected caches
  - Optimized TTLs by cache type
  - Cache warming in production
- **Database indexes** - Query optimization
- **Pagination** - Reduces data transfer
- **HTTP compression** - Reduces response size
- **Connection Pool** - Connection reuse
- **Lazy Loading** - Optimized loading

### Cache Strategy (v1.2.0+)

- **Individual Tasks**: 15-minute TTL
- **Task Statistics**: 5-minute TTL
- **Task Lists**: 10-minute TTL (default)
- **Selective Evict**: Only affected caches are invalidated
- **Cache Warming**: Pre-loads frequent data on startup (prod profile)

### Load Tests

```bash
# Using Apache Bench
ab -n 10000 -c 100 http://localhost:8081/api/v1/tasks

# Using wrk
wrk -t12 -c400 -d30s http://localhost:8081/api/v1/tasks
```

## Performance Metrics

### Local Environment Results (Tested)
- **Throughput**: ~991 requests/second (10,000 requests, 100 concurrent)
- **P95 Latency**: ~369ms
- **P99 Latency**: ~641ms
- **Error rate**: 0% (10,000 requests, 0 failures)

### Goals for Optimized Production Environment
- **Throughput**: 10,000+ requests/second
- **P95 Latency**: < 50ms
- **P99 Latency**: < 100ms
- **Error rate**: < 0.1%

*Note: Production values require additional optimizations such as load balancing, distributed cache, and dedicated infrastructure.*

## Configuration

### Environment Variables

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tasksdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SERVER_PORT=8081
```

## Implemented Best Practices

### Architecture and Design
- **Clean Architecture** - Clear separation of responsibilities
- **SOLID Principles** - Extensible and maintainable code
- **Strategy Pattern** - Strategic cache eviction
- **DTO Pattern** - Optimized data transfer
- **Hybrid Architecture** - MVC + WebFlux for maximum performance

### Security
- **JWT Authentication & Authorization** - Stateless authentication
- **Persisted Refresh Tokens** - Revocation and complete control
- **Role-Based Access Control (RBAC)** - Granular access control
- **Password Encryption (BCrypt)** - Secure passwords
- **Rate Limiting (Bucket4j)** - Abuse protection
- **IP-based Rate Limiting** - Additional protection layer
- **Security Headers (OWASP)** - HTTP security headers
- **Input Sanitization** - Injection attack prevention
- **SQL Injection Prevention** - Multi-layer validation

### Performance
- **WebFlux (Reactive Programming)** - High concurrency (10,000+ req/s)
- **R2DBC** - Non-blocking access to PostgreSQL
- **Intelligent Cache** - Redis with optimized strategies
- **Cache Metrics** - Hit rate and performance monitoring
- **Batch Operations** - Efficient batch processing
- **Connection Pooling** - Optimized HikariCP
- **Database Indexing** - Optimized queries
- **Soft Delete** - Data recovery

### Observability
- **Distributed Tracing** - Integrated Micrometer Tracing
- **Structured JSON Logs** - Logback configured for production
- **Prometheus Metrics** - Complete monitoring
- **Custom Health Checks** - DB, Redis, Cache
- **Persistent Auditing** - Logs of all sensitive operations

### Quality
- **Exception Handling** - Robust error handling
- **Validation** - Multi-layer validation
- **Optimistic Locking** - Concurrency control
- **Structured Logging** - Organized logs
- **Constants for Magic Numbers** - Clean code
- **API Versioning** - Controlled compatibility
- **Documentation (Swagger)** - Complete documentation
- **Code Coverage** - Comprehensive tests (475+ tests, target: 90% lines, 85% branches)
- **CI/CD Pipeline** - Continuous integration

## CI/CD (Planned)

> **Status:** The CI/CD pipeline is planned for future implementation. The configuration below serves as a reference.

### GitHub Actions (Configuration Example)

```yaml
name: CI/CD Pipeline
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: tasksdb
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
      redis:
        image: redis:7-alpine
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests with coverage
        run: mvn clean test jacoco:report
      - name: Verify coverage
        run: mvn jacoco:check
      - name: Upload coverage reports
        uses: codecov/codecov-action@v3
        with:
          files: target/site/jacoco/jacoco.xml
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build JAR
        run: mvn clean package -DskipTests
      - name: Build Docker image
        run: docker build -t high-performance-api:latest .
```

## Deploy

### Docker
```bash
# Build image
docker build -t high-performance-api:latest .

# Run container
docker run -p 8081:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/tasksdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e SPRING_REDIS_HOST=host.docker.internal \
  -e SPRING_REDIS_PORT=6379 \
  high-performance-api:latest
```

### Cloud (AWS/GCP/Azure)
- Container Registry
- Kubernetes
- Cloud Run / ECS / AKS

## Learnings and Differentiators

This project demonstrates:
- Practical experience with Java/Spring Boot
- Knowledge in high-performance REST APIs
- Concurrent programming and optimization
- Caching and performance strategies
- Monitoring and observability
- Automated testing
- Docker and containerization
- Software engineering best practices

## License

This project is for technical skills demonstration purposes.

## Author

**Leonardo Dario Borges**
- LinkedIn: [borgesleonardod](https://www.linkedin.com/in/borgesleonardod/)
- Portfolio: [leonardodborges.com.br](https://leonardodborges.com.br)

---

*Developed with focus on high performance, scalability, and software engineering best practices.*
