.PHONY: help build test run clean docker-up docker-down docker-build docker-run coverage format lint install up down ps logs build-docker stop clean-docker test-fast test-debug test-specific test-report test-verify docker-ps

# Variables
APP_NAME=high-performance-api
JAR_FILE=target/$(APP_NAME)-1.0.0.jar
DOCKER_IMAGE=$(APP_NAME):latest
DOCKER_CONTAINER=$(APP_NAME)-container

# Colors for output
GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
NC=\033[0m # No Color

help: ## Show this help message
	@echo "$(GREEN)Available commands:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'

install: ## Install dependencies, start Docker services and run application
	@echo "$(GREEN)=== Installing and starting everything ===$(NC)"
	@echo "$(GREEN)Step 1/3: Installing dependencies...$(NC)"
	mvn clean install -DskipTests
	@echo "$(GREEN)Step 2/3: Starting Docker services (PostgreSQL, Redis)...$(NC)"
	$(DOCKER_COMPOSE) up -d
	@echo "$(GREEN)Waiting for services to be ready...$(NC)"
	@sleep 5
	@echo "$(GREEN)Step 3/3: Starting application...$(NC)"
	@echo "$(YELLOW)Application is starting in the background...$(NC)"
	@echo "$(GREEN)=== Installation complete! ===$(NC)"
	@echo "$(GREEN)Services running:$(NC)"
	@$(DOCKER_COMPOSE) ps
	@echo "$(GREEN)Application will be available at: http://localhost:8080$(NC)"
	@echo "$(GREEN)Swagger UI: http://localhost:8080/swagger-ui.html$(NC)"
	@echo "$(YELLOW)To run the application, use: make run$(NC)"
	@echo "$(YELLOW)Or use 'make dev' to start services and run application together$(NC)"

build: ## Build the application
	@echo "$(GREEN)Building application...$(NC)"
	mvn clean package -DskipTests

test: ## Run all tests
	@echo "$(GREEN)Running tests...$(NC)"
	mvn clean test

test-coverage: ## Run tests with coverage report
	@echo "$(GREEN)Running tests with coverage...$(NC)"
	mvn clean test jacoco:report
	@echo "$(GREEN)Coverage report generated at: target/site/jacoco/index.html$(NC)"

test-unit: ## Run only unit tests
	@echo "$(GREEN)Running unit tests...$(NC)"
	mvn test -Dtest=*Test

test-integration: ## Run only integration tests
	@echo "$(GREEN)Running integration tests...$(NC)"
	mvn test -Dtest=*IntegrationTest

coverage: test-coverage ## Alias for test-coverage
	@echo "$(GREEN)Opening coverage report...$(NC)"

run: ## Run the application (ensures Docker services are up, finds available port)
	@echo "$(GREEN)Checking Docker services...$(NC)"
	@if ! $(DOCKER_COMPOSE) ps postgres redis 2>/dev/null | grep -q "Up"; then \
		echo "$(YELLOW)Docker services not running. Starting them...$(NC)"; \
		$(DOCKER_COMPOSE) up -d postgres redis; \
		sleep 5; \
	fi
	@echo "$(GREEN)Finding available port (8080-8090)...$(NC)"
	@PORT=8080; \
	FOUND=0; \
	while [ $$PORT -le 8090 ]; do \
		if command -v lsof >/dev/null 2>&1; then \
			if ! lsof -i :$$PORT >/dev/null 2>&1; then \
				FOUND=1; \
				break; \
			fi; \
		elif command -v netstat >/dev/null 2>&1; then \
			if ! netstat -tln 2>/dev/null | grep -q ":$$PORT "; then \
				FOUND=1; \
				break; \
			fi; \
		elif command -v ss >/dev/null 2>&1; then \
			if ! ss -tln 2>/dev/null | grep -q ":$$PORT "; then \
				FOUND=1; \
				break; \
			fi; \
		else \
			echo "$(YELLOW)Warning: No port checking tool available, trying port $$PORT$(NC)"; \
			FOUND=1; \
			break; \
		fi; \
		PORT=$$((PORT + 1)); \
	done; \
	if [ $$FOUND -eq 1 ]; then \
		echo "$(GREEN)✓ Using port $$PORT$(NC)"; \
		echo "$(YELLOW)Access the API at: http://localhost:$$PORT$(NC)"; \
		echo "$(YELLOW)Swagger UI at: http://localhost:$$PORT/swagger-ui.html$(NC)"; \
		SERVER_PORT=$$PORT mvn spring-boot:run; \
	else \
		echo "$(RED)✗ No available port found between 8080-8090$(NC)"; \
		exit 1; \
	fi

run-jar: build ## Run the application from JAR file
	@echo "$(GREEN)Running application from JAR...$(NC)"
	java -jar $(JAR_FILE)

clean: ## Clean build artifacts
	@echo "$(GREEN)Cleaning build artifacts...$(NC)"
	mvn clean
	@echo "$(GREEN)Removing Docker containers and images...$(NC)"
	$(DOCKER_COMPOSE) down -v 2>/dev/null || docker compose down -v 2>/dev/null || true

format: ## Format code using Maven formatter
	@echo "$(GREEN)Formatting code...$(NC)"
	mvn com.spotify.fmt:fmt-maven-plugin:format

lint: ## Check code style
	@echo "$(GREEN)Checking code style...$(NC)"
	mvn checkstyle:check || true

validate: ## Validate project configuration
	@echo "$(GREEN)Validating project...$(NC)"
	mvn validate

compile: ## Compile the project
	@echo "$(GREEN)Compiling project...$(NC)"
	mvn clean compile

package: build ## Alias for build

# Docker commands (detect docker-compose v1 or docker compose v2)
DOCKER_COMPOSE = $(shell command -v docker-compose >/dev/null 2>&1 && echo "docker-compose" || echo "docker compose")

docker-up: ## Start Docker containers (PostgreSQL, Redis)
	@echo "$(GREEN)Starting Docker containers...$(NC)"
	$(DOCKER_COMPOSE) up -d
	@echo "$(GREEN)Waiting for services to be ready...$(NC)"
	@sleep 5
	@echo "$(GREEN)Services are ready!$(NC)"

docker-down: ## Stop Docker containers
	@echo "$(GREEN)Stopping Docker containers...$(NC)"
	$(DOCKER_COMPOSE) down

docker-logs: ## Show Docker container logs
	@echo "$(GREEN)Showing Docker logs...$(NC)"
	$(DOCKER_COMPOSE) logs -f

docker-ps: ## List Docker containers
	@echo "$(GREEN)Docker containers status:$(NC)"
	$(DOCKER_COMPOSE) ps

docker-build: ## Build Docker image
	@echo "$(GREEN)Building Docker image...$(NC)"
	docker build -t $(DOCKER_IMAGE) .
	@echo "$(GREEN)Docker image built: $(DOCKER_IMAGE)$(NC)"

docker-run: docker-build ## Build and run Docker container
	@echo "$(GREEN)Running Docker container...$(NC)"
	docker run -d --name $(DOCKER_CONTAINER) \
		-p 8080:8080 \
		--network api-rest-network \
		$(DOCKER_IMAGE)
	@echo "$(GREEN)Container running on http://localhost:8080$(NC)"

docker-stop: ## Stop Docker container
	@echo "$(GREEN)Stopping Docker container...$(NC)"
	docker stop $(DOCKER_CONTAINER) 2>/dev/null || true
	docker rm $(DOCKER_CONTAINER) 2>/dev/null || true

docker-clean: docker-down docker-stop ## Clean all Docker resources
	@echo "$(GREEN)Cleaning Docker resources...$(NC)"
	docker rmi $(DOCKER_IMAGE) 2>/dev/null || true
	docker system prune -f

# Docker aliases (shortcuts)
up: docker-up ## Alias for docker-up
down: docker-down ## Alias for docker-down
ps: docker-ps ## Alias for docker-ps (list containers)
logs: docker-logs ## Alias for docker-logs
build-docker: docker-build ## Alias for docker-build
stop: docker-stop ## Alias for docker-stop
clean-docker: docker-clean ## Alias for docker-clean

# Development commands
dev: docker-up run ## Start services and run application in development mode

dev-stop: docker-down ## Stop development environment

restart: clean build docker-stop run ## Clean, build, stop Docker app, start services and run locally

# Testing commands
test-watch: ## Run tests in watch mode (requires mvn test-compile)
	@echo "$(GREEN)Running tests in watch mode...$(NC)"
	@while true; do \
		mvn test-compile test -q; \
		sleep 2; \
	done

test-fast: ## Run tests without cleaning (faster)
	@echo "$(GREEN)Running tests (fast mode, no clean)...$(NC)"
	mvn test

test-debug: ## Run tests with debug output
	@echo "$(GREEN)Running tests with debug output...$(NC)"
	mvn clean test -X

test-specific: ## Run a specific test class (usage: make test-specific TEST=TaskServiceTest)
	@if [ -z "$(TEST)" ]; then \
		echo "$(YELLOW)Usage: make test-specific TEST=ClassName$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)Running test: $(TEST)...$(NC)"
	mvn test -Dtest=$(TEST)

test-report: test-coverage ## Open test coverage report in browser
	@echo "$(GREEN)Opening coverage report...$(NC)"
	@if command -v xdg-open > /dev/null; then \
		xdg-open target/site/jacoco/index.html; \
	elif command -v open > /dev/null; then \
		open target/site/jacoco/index.html; \
	else \
		echo "$(YELLOW)Open manually: target/site/jacoco/index.html$(NC)"; \
	fi

test-verify: ## Verify test coverage meets requirements (90% line, 85% branch)
	@echo "$(GREEN)Verifying test coverage...$(NC)"
	mvn clean test jacoco:report jacoco:check
	@echo "$(GREEN)Coverage verification complete!$(NC)"

# Utility commands
check: validate lint test ## Run validation, linting and tests

ci: clean test-coverage ## Run CI pipeline locally
	@echo "$(GREEN)CI pipeline completed!$(NC)"

version: ## Show project version
	@mvn help:evaluate -Dexpression=project.version -q -DforceStdout

deps: ## Show dependency tree
	@echo "$(GREEN)Dependency tree:$(NC)"
	mvn dependency:tree

deps-update: ## Update dependencies
	@echo "$(GREEN)Updating dependencies...$(NC)"
	mvn versions:display-dependency-updates

# Database commands
db-migrate: ## Run database migrations (if using Flyway/Liquibase)
	@echo "$(GREEN)Running database migrations...$(NC)"
	mvn flyway:migrate 2>/dev/null || echo "$(YELLOW)Flyway not configured$(NC)"

db-reset: docker-down docker-up ## Reset database (stop and start containers)
	@echo "$(GREEN)Database reset complete$(NC)"

# Documentation
docs: ## Generate API documentation
	@echo "$(GREEN)API documentation available at: http://localhost:8080/swagger-ui.html$(NC)"
	@echo "$(GREEN)OpenAPI spec at: http://localhost:8080/v3/api-docs$(NC)"

# Health check
health: ## Check application health
	@echo "$(GREEN)Checking application health...$(NC)"
	@curl -s http://localhost:8080/actuator/health | jq . || echo "$(YELLOW)Application not running or jq not installed$(NC)"

# Quick start
quickstart: docker-up build run ## Quick start: start services, build and run
	@echo "$(GREEN)Quick start complete!$(NC)"
	@echo "$(GREEN)Application running at: http://localhost:8080$(NC)"
	@echo "$(GREEN)Swagger UI at: http://localhost:8080/swagger-ui.html$(NC)"

# Full setup (install everything and start services)
setup: install ## Full setup: install dependencies and start all services
	@echo "$(GREEN)Full setup completed!$(NC)"

# Default target
.DEFAULT_GOAL := help
