# High Performance REST API

API REST de alta performance desenvolvida com Spring Boot para demonstração de habilidades backend, focada em escalabilidade, performance e boas práticas de engenharia de software.

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
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados relacional
- **Redis** - Cache e otimização
- **Spring WebFlux** - Programação reativa (alta performance)
- **Swagger/OpenAPI** - Documentação automática
- **Prometheus** - Métricas e monitoramento
- **Docker** - Containerização
- **Maven** - Gerenciamento de dependências

## Funcionalidades

- CRUD completo de tarefas
- **Autenticação JWT** (v1.4.0)
- **Autorização baseada em roles** (USER, ADMIN)
- **Rate Limiting** (v1.5.0) - Proteção contra abuso
- **Security Headers** (v1.7.0) - Headers HTTP de segurança (OWASP)
- Paginação e filtros
- Cache inteligente (Redis)
- Validação de dados
- Tratamento de erros padronizado
- Documentação Swagger automática
- Métricas Prometheus
- Health checks
- Testes automatizados
- Docker e Docker Compose
- **CI/CD Pipeline** (v1.6.0) - GitHub Actions

## Arquitetura

```
src/
├── main/
│   ├── java/
│   │   └── com/leonardoborges/api/
│   │       ├── config/          # Configurações (Cache, Web, etc)
│   │       ├── controller/      # Controllers REST
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── exception/       # Tratamento de erros
│   │       ├── model/           # Entidades JPA
│   │       ├── repository/      # Repositórios JPA
│   │       └── service/         # Lógica de negócio
│   └── resources/
│       └── application.yml      # Configurações
└── test/                        # Testes automatizados
```

## Como Executar

### Pré-requisitos

- **Java 21** (LTS) - Instalar: `sudo apt install openjdk-21-jdk`
- Maven 3.6+ - Instalar: `sudo apt install maven`
- Docker e Docker Compose (opcional)

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

### Endpoints Principais

**Authentication (v1.4.0+):**
- `POST /api/v1/auth/register` - Registrar novo usuário
- `POST /api/v1/auth/login` - Login e obter token JWT

**Tasks:** (Requer autenticação)
- `POST /api/v1/tasks` - Criar nova tarefa
- `GET /api/v1/tasks` - Listar todas as tarefas (paginado)
- `GET /api/v1/tasks/{id}` - Buscar tarefa por ID
- `GET /api/v1/tasks/status/{status}` - Filtrar por status
- `GET /api/v1/tasks/stats/count` - Estatísticas
- `PUT /api/v1/tasks/{id}` - Atualizar tarefa
- `DELETE /api/v1/tasks/{id}` - Deletar tarefa

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
TOKEN=$(curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }' | jq -r '.token')

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

- **46 testes** automatizados (100% passando)
- Testes unitários (Service, Repository, Controller, Cache, Utils)
- Testes de integração (end-to-end)
- Testes de autenticação (JWT, login, registro)
- Testes com H2 (banco em memória)
- JaCoCo configurado para relatórios de cobertura

### Executar Testes com Cobertura

```bash
# Executar testes e gerar relatório de cobertura
mvn clean test jacoco:report

# Ver relatório (HTML)
open target/site/jacoco/index.html
```

## CI/CD Pipeline (v1.6.0+)

O projeto inclui um pipeline completo de CI/CD usando GitHub Actions:

### Jobs do Pipeline

1. **Test** - Executa todos os testes
   - Configura PostgreSQL e Redis como serviços
   - Roda testes com cobertura
   - Valida cobertura mínima (80%)
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

- Clean Architecture
- SOLID Principles
- DTO Pattern
- Exception Handling
- Validation
- Input Sanitization (v1.3.0)
- JWT Authentication & Authorization (v1.4.0)
- Role-Based Access Control (v1.4.0)
- Password Encryption (BCrypt) (v1.4.0)
- Rate Limiting (Bucket4j) (v1.5.0)
- Security Headers (OWASP) (v1.7.0)
- Structured Logging
- Constants for Magic Numbers (v1.3.0)
- Caching Strategy
- Database Indexing
- API Versioning
- Documentation (Swagger)
- Code Coverage Reporting (JaCoCo)
- CI/CD Pipeline (GitHub Actions) (v1.6.0)

## CI/CD

### GitHub Actions (Exemplo)

```yaml
name: CI/CD Pipeline
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: mvn test
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: mvn package
      - run: docker build -t api:latest .
```

## Deploy

### Docker
```bash
docker build -t high-performance-api:latest .
docker run -p 8081:8081 high-performance-api:latest
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
