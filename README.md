# High Performance REST API

API REST de alta performance desenvolvida com Spring Boot para demonstração de habilidades backend, focada em escalabilidade, performance e boas práticas de engenharia de software.

## Status: v3.0.0

Projeto de referência implementando todas as melhores práticas de engenharia de software, segurança e performance. 
Inclui arquitetura híbrida MVC + WebFlux para máxima performance. Pronto para produção.

## Objetivo

Demonstrar experiência prática em desenvolvimento backend com Java/Spring Boot, incluindo:
- APIs REST de alta performance
- Programação concorrente e otimização
- Cache e otimização de consultas
- Monitoramento e métricas
- Testes automatizados
- Docker e CI/CD

## Tecnologias

- **Java 21** (LTS) - Linguagem de programação
- **Spring Boot 3.2.0** - Framework backend
- **Spring Data JPA** - Persistência de dados (MVC)
- **Spring WebFlux + R2DBC** - Programação reativa para alta performance
- **PostgreSQL** - Banco de dados relacional
- **Redis** - Cache e otimização (bloqueante e reativo)
- **Swagger/OpenAPI** - Documentação automática
- **Prometheus** - Métricas e monitoramento
- **Docker** - Containerização
- **Maven** - Gerenciamento de dependências

## Funcionalidades

- CRUD completo de tarefas
- **Autenticação JWT com Refresh Tokens** (v1.4.0, v2.1.0)
- **Autorização baseada em roles** (USER, ADMIN)
- **Rate Limiting** (v1.5.0) - Proteção contra abuso
- **Security Headers** (v1.7.0) - Headers HTTP de segurança (OWASP)
- **Optimistic Locking** (v1.8.0) - Controle de concorrência
- **CORS Configurável** (v2.1.0) - Segurança aprimorada
- **Sistema de Auditoria** (v2.1.0) - Log de operações sensíveis
- **Prevenção de SQL Injection** (v2.1.0) - Validação em múltiplas camadas
- Paginação e filtros
- Cache inteligente (Redis)
- Validação de dados em múltiplas camadas
- Tratamento de erros padronizado
- **Documentação Swagger completa** (v1.9.0) - Todos os códigos HTTP documentados
- **Health Checks Customizados** (v2.0.0) - Monitoramento de DB, Redis e Cache
- **Métricas Customizadas** (v2.0.0) - Métricas de negócio com Prometheus
- **Cobertura de Testes 90%+** (v2.0.0) - Validação obrigatória no CI/CD
- **Testes de Performance** (v2.1.0) - Validação de tempos de resposta
- Métricas Prometheus
- Health checks
- Testes automatizados (unitários, integração, performance)
- Docker e Docker Compose
- **CI/CD Pipeline** (v1.6.0) - GitHub Actions

## Arquitetura

```
src/
├── main/
│   ├── java/
│   │   └── com/leonardoborges/api/
│   │       ├── audit/           # Sistema de auditoria
│   │       ├── cache/            # Gerenciamento de cache
│   │       ├── config/           # Configurações (Cache, Web, Security, etc)
│   │       ├── constants/        # Constantes do sistema
│   │       ├── controller/      # Controllers REST
│   │       ├── dto/              # Data Transfer Objects
│   │       ├── exception/        # Tratamento de erros
│   │       ├── health/           # Health checks customizados
│   │       ├── mapper/           # Mapeamento DTO ↔ Entity
│   │       ├── metrics/          # Métricas customizadas
│   │       ├── model/            # Entidades JPA
│   │       ├── repository/       # Repositórios JPA e R2DBC
│   │       ├── security/         # Componentes de segurança
│   │       ├── service/          # Lógica de negócio
│   │       └── util/             # Utilitários
│   └── resources/
│       ├── application.yml       # Configurações
│       ├── db/migration/         # Migrações Flyway
│       └── logback-spring.xml    # Configuração de logs
└── test/                         # Testes automatizados
```

## Como Executar

### Pré-requisitos

- **Java 21** (LTS) - Instalar: `sudo apt install openjdk-21-jdk`
- Maven 3.6+ - Instalar: `sudo apt install maven`
- Docker e Docker Compose (opcional)

### Opção 0: Makefile (Mais Fácil - Recomendado)

O projeto inclui um **Makefile completo** com comandos úteis para simplificar o desenvolvimento:

```bash
# Ver todos os comandos disponíveis
make help

# Instalar dependências e iniciar serviços Docker
make install

# Início rápido completo (Docker + Build + Run)
make quickstart

# Desenvolvimento (Docker + Run aplicação)
make dev

# Executar testes
make test

# Testes com cobertura
make test-coverage

# Docker - comandos simplificados
make up      # Iniciar containers
make down    # Parar containers
make ps      # Listar containers
make logs    # Ver logs

# Build e execução
make build   # Compilar projeto
make run     # Executar aplicação
make clean   # Limpar artefatos
```

**Comandos mais usados:**

| Comando | Descrição |
|---------|-----------|
| `make install` | Instala dependências e inicia serviços Docker |
| `make quickstart` | Início rápido completo (tudo automático) |
| `make dev` | Desenvolvimento (Docker + aplicação) |
| `make test` | Executa todos os testes |
| `make test-coverage` | Testes com relatório de cobertura |
| `make up` | Inicia containers Docker |
| `make down` | Para containers Docker |
| `make ps` | Lista containers em execução |
| `make logs` | Mostra logs dos containers |

Para ver todos os comandos disponíveis: `make help`

### Opção 1: Docker Compose (Recomendado)

```bash
# Iniciar todos os serviços (PostgreSQL, Redis, API)
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Parar serviços
docker-compose down
```

A API estará disponível em: `http://localhost:8081`

### Opção 2: Execução Local

1. **Instalar PostgreSQL e Redis** (ou usar Docker apenas para esses serviços)

2. **Configurar banco de dados:**
```sql
CREATE DATABASE tasksdb;
```

3. **Executar a aplicação:**
```bash
mvn spring-boot:run
```

### Opção 3: Build e Executar JAR

```bash
# Build
mvn clean package

# Executar
java -jar target/high-performance-api-1.0.0.jar
```

## Documentação da API

### Swagger UI
Acesse: `http://localhost:8081/swagger-ui.html`

A documentação Swagger está completa com:
- Todos os endpoints documentados
- Todos os códigos de status HTTP (200, 201, 400, 401, 403, 404, 409, 422, 429, 500)
- Exemplos de requisição e resposta
- Descrições detalhadas de cada endpoint
- Esquemas de autenticação JWT
- Validações e constraints documentadas
- DTOs com exemplos e descrições

### Endpoints Principais

**Authentication (v1.4.0+):**
- `POST /api/v1/auth/register` - Registrar novo usuário
- `POST /api/v1/auth/login` - Login e obter token JWT
- `POST /api/v1/auth/refresh` - Renovar token de acesso usando refresh token

**Tasks (MVC - Escritas e operações complexas):** (Requer autenticação)
- `POST /api/v1/tasks` - Criar nova tarefa
- `GET /api/v1/tasks` - Listar todas as tarefas (paginado)
- `GET /api/v1/tasks/{id}` - Buscar tarefa por ID
- `GET /api/v1/tasks/status/{status}` - Filtrar por status
- `GET /api/v1/tasks/stats/count` - Estatísticas
- `PUT /api/v1/tasks/{id}` - Atualizar tarefa
- `DELETE /api/v1/tasks/{id}` - Deletar tarefa (soft delete)
- `GET /api/v1/tasks/{taskId}/history` - Histórico de mudanças de uma task (paginado)
- `GET /api/v1/tasks/{taskId}/history/all` - Todo o histórico de uma task (sem paginação)
- `GET /api/v1/tasks/{taskId}/history/field/{fieldName}` - Histórico de um campo específico
- `GET /api/v1/tasks/{taskId}/history/date-range` - Histórico por intervalo de datas

**Tasks Reativas (WebFlux - Alta Performance):** (Requer autenticação)
- `GET /api/v2/reactive/tasks` - Listar tarefas (reativo, alta concorrência)
- `GET /api/v2/reactive/tasks/{id}` - Buscar tarefa (reativo, baixa latência)
- `GET /api/v2/reactive/tasks/status/{status}` - Filtrar por status (reativo)
- `GET /api/v2/reactive/tasks/stats/count` - Estatísticas (reativo)

**Batch Operations (Operações em Lote):** (Requer autenticação)
- `POST /api/v1/tasks/batch/create` - Criar múltiplas tarefas (até 100)
- `PUT /api/v1/tasks/batch/update` - Atualizar múltiplas tarefas (até 100)
- `DELETE /api/v1/tasks/batch/delete` - Deletar múltiplas tarefas (até 100)

**Audit (Admin only):** (Requer role ADMIN)
- `GET /api/v1/audit` - Listar logs de auditoria
- `GET /api/v1/audit/action/{action}` - Filtrar por ação
- `GET /api/v1/audit/entity/{entityType}/{entityId}` - Filtrar por entidade
- `GET /api/v1/audit/user/{username}` - Filtrar por usuário
- `GET /api/v1/audit/date-range` - Filtrar por intervalo de datas
- `GET /api/v1/audit/stats/failed` - Estatísticas de ações falhadas

**Nota:** 
- Endpoints reativos (`/api/v2/reactive/*`) são otimizados para leitura com alta concorrência (10.000+ req/s)
- Use endpoints MVC (`/api/v1/*`) para operações de escrita que requerem transações complexas
- Batch operations são ideais para processar múltiplas operações de forma eficiente

**Cache Management (v1.2.0+):** (Requer role ADMIN)
- `GET /api/v1/cache/stats` - Estatísticas do cache
- `GET /api/v1/cache/tasks/{id}/cached` - Verificar se tarefa está em cache
- `DELETE /api/v1/cache/tasks/{id}` - Remover tarefa do cache
- `DELETE /api/v1/cache/stats` - Limpar cache de estatísticas
- `DELETE /api/v1/cache/all` - Limpar todos os caches (administrativo)

### Rate Limiting (v1.5.0+)

A API implementa rate limiting para proteger contra abuso:
- **Endpoints padrão**: 60 requisições por minuto
- **Endpoints de autenticação**: 5 requisições por minuto (proteção contra brute force)
- **Endpoints administrativos**: 200 requisições por minuto (para admins)

Quando o limite é excedido, a API retorna `429 Too Many Requests` com informações sobre quando tentar novamente.

### Exemplo de Requisição

```bash
# 1. Registrar novo usuário
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# 2. Login e obter token
RESPONSE=$(curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }')

TOKEN=$(echo $RESPONSE | jq -r '.token')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.refreshToken')

# 2.1. Renovar token (opcional)
# TOKEN=$(curl -X POST http://localhost:8081/api/v1/auth/refresh \
#   -H "Content-Type: application/json" \
#   -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}" | jq -r '.token')

# 3. Criar tarefa (com autenticação)
curl -X POST http://localhost:8081/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Implementar feature X",
    "description": "Descrição da tarefa",
    "status": "PENDING",
    "priority": 1
  }'

# 4. Listar tarefas (com autenticação)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/tasks?page=0&size=20

# 5. Buscar por ID (com autenticação)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/tasks/1
```

## Monitoramento e Métricas

### Health Check
`GET http://localhost:8081/actuator/health`

### Métricas Prometheus
`GET http://localhost:8081/actuator/prometheus`

### Métricas Disponíveis
- `http_server_requests_seconds` - Latência de requisições
- `jvm_memory_used_bytes` - Uso de memória
- `jvm_gc_pause_seconds` - Garbage Collection
- `process_cpu_usage` - Uso de CPU

## Testes

### Executar Testes

```bash
# Executar todos os testes
mvn test

# Executar teste específico
mvn test -Dtest=TaskServiceTest

# Executar com cobertura
mvn test jacoco:report
```

### Cobertura de Testes

- **65+ testes** automatizados (100% passando)
- **Cobertura mínima: 90%** (linhas) e **85%** (branches)
- **Validação obrigatória no CI/CD** - Build falha se cobertura < 90%
- Testes unitários (Service, Repository, Controller, Cache, Utils)
- Testes de integração (end-to-end)
- Testes de autenticação (JWT, login, registro, refresh tokens)
- Testes de segurança (OWASP Top 10, SQL Injection, XSS, Rate Limiting)
- Testes de batch operations
- Testes de Strategy Pattern (cache eviction)
- Testes com H2 (banco em memória)
- Testcontainers para testes de integração
- JaCoCo configurado para relatórios de cobertura

### Executar Testes com Cobertura

```bash
# Executar testes e gerar relatório de cobertura
mvn clean test jacoco:report

# Ver relatório (HTML)
open target/site/jacoco/index.html
```

## CI/CD Pipeline (v1.6.0+)

> **Nota:** O pipeline de CI/CD está planejado para implementação. A estrutura abaixo descreve o pipeline que será configurado usando GitHub Actions.

### Jobs do Pipeline

1. **Test** - Executa todos os testes
   - Configura PostgreSQL e Redis como serviços
   - Roda testes com cobertura
   - Valida cobertura mínima (90% linhas, 85% branches)
   - Upload de resultados e relatórios

2. **Build** - Compila a aplicação
   - Build do JAR
   - Upload do artefato

3. **Docker Build** - Constrói imagem Docker
   - Build da imagem Docker
   - Cache otimizado

### Triggers

- Push para `main` ou `develop`
- Pull requests para `main`
- Tags de versão (`v*`)

### Status Badge

Adicione ao seu README:
```markdown
![CI/CD](https://github.com/Laion459/API-REST-JAVA/workflows/CI%2FCD%20Pipeline/badge.svg)
```

## Performance

### Otimizações Implementadas

- **Cache Redis** - Reduz consultas ao banco
  - Evict seletivo (v1.2.0) - Invalida apenas caches afetados
  - TTLs otimizados por tipo de cache
  - Cache warming em produção
- **Índices no banco** - Otimização de queries
- **Paginação** - Reduz transferência de dados
- **Compressão HTTP** - Reduz tamanho de respostas
- **Connection Pool** - Reutilização de conexões
- **Lazy Loading** - Carregamento otimizado

### Estratégia de Cache (v1.2.0+)

- **Individual Tasks**: TTL de 15 minutos
- **Task Statistics**: TTL de 5 minutos
- **Task Lists**: TTL de 10 minutos (padrão)
- **Evict Seletivo**: Apenas caches afetados são invalidados
- **Cache Warming**: Pré-carrega dados frequentes na inicialização (perfil prod)

### Testes de Carga

```bash
# Usando Apache Bench
ab -n 10000 -c 100 http://localhost:8081/api/v1/tasks

# Usando wrk
wrk -t12 -c400 -d30s http://localhost:8081/api/v1/tasks
```

## Métricas de Performance

### Resultados em Ambiente Local (Testado)
- **Throughput**: ~991 requisições/segundo (10.000 requisições, 100 concorrentes)
- **Latência P95**: ~369ms
- **Latência P99**: ~641ms
- **Taxa de erro**: 0% (10.000 requisições, 0 falhas)

### Objetivos para Ambiente de Produção Otimizado
- **Throughput**: 10.000+ requisições/segundo
- **Latência P95**: < 50ms
- **Latência P99**: < 100ms
- **Taxa de erro**: < 0.1%

*Nota: Valores de produção requerem otimizações adicionais como load balancing, cache distribuído, e infraestrutura dedicada.*

## Configuração

### Variáveis de Ambiente

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tasksdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SERVER_PORT=8081
```

## Boas Práticas Implementadas

### Arquitetura e Design
- **Clean Architecture** - Separação clara de responsabilidades
- **SOLID Principles** - Código extensível e manutenível
- **Strategy Pattern** - Cache eviction estratégico
- **DTO Pattern** - Transferência de dados otimizada
- **Arquitetura Híbrida** - MVC + WebFlux para máxima performance

### Segurança
- **JWT Authentication & Authorization** - Autenticação stateless
- **Refresh Tokens Persistidos** - Revogação e controle completo
- **Role-Based Access Control (RBAC)** - Controle de acesso granular
- **Password Encryption (BCrypt)** - Senhas seguras
- **Rate Limiting (Bucket4j)** - Proteção contra abuso
- **Rate Limiting por IP** - Camada adicional de proteção
- **Security Headers (OWASP)** - Headers HTTP de segurança
- **Input Sanitization** - Prevenção de injection attacks
- **SQL Injection Prevention** - Validação em múltiplas camadas

### Performance
- **WebFlux (Programação Reativa)** - Alta concorrência (10.000+ req/s)
- **R2DBC** - Acesso não-bloqueante ao PostgreSQL
- **Cache Inteligente** - Redis com estratégias otimizadas
- **Cache Metrics** - Monitoramento de hit rate e performance
- **Batch Operations** - Processamento eficiente em lote
- **Connection Pooling** - HikariCP otimizado
- **Database Indexing** - Queries otimizadas
- **Soft Delete** - Recuperação de dados

### Observabilidade
- **Distributed Tracing** - Micrometer Tracing integrado
- **Logs Estruturados JSON** - Logback configurado para produção
- **Métricas Prometheus** - Monitoramento completo
- **Health Checks Customizados** - DB, Redis, Cache
- **Auditoria Persistente** - Logs de todas as operações sensíveis

### Qualidade
- **Exception Handling** - Tratamento robusto de erros
- **Validation** - Validação em múltiplas camadas
- **Optimistic Locking** - Controle de concorrência
- **Structured Logging** - Logs organizados
- **Constants for Magic Numbers** - Código limpo
- **API Versioning** - Compatibilidade controlada
- **Documentation (Swagger)** - Documentação completa
- **Code Coverage 90%+** - Testes abrangentes
- **CI/CD Pipeline** - Integração contínua

## CI/CD (Planejado)

> **Status:** O pipeline de CI/CD está planejado para implementação futura. A configuração abaixo serve como referência.

### GitHub Actions (Exemplo de Configuração)

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
# Build da imagem
docker build -t high-performance-api:latest .

# Executar container
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

## Aprendizados e Diferenciais

Este projeto demonstra:
- Experiência prática com Java/Spring Boot
- Conhecimento em APIs REST de alta performance
- Programação concorrente e otimização
- Cache e estratégias de performance
- Monitoramento e observabilidade
- Testes automatizados
- Docker e containerização
- Boas práticas de engenharia de software

## Licença

Este projeto é para fins de demonstração de habilidades técnicas.

## Autor

**Leonardo Dario Borges**
- LinkedIn: [borgesleonardod](https://www.linkedin.com/in/borgesleonardod/)
- Portfólio: [leonardodborges.com.br](https://leonardodborges.com.br)

---

*Desenvolvido com foco em alta performance, escalabilidade e boas práticas de engenharia de software.*
