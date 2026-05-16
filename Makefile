.PHONY: up down down-clean ps logs test

COMPOSE := docker compose

## Start infra + both services (waits for health checks)
up:
	$(COMPOSE) up --build -d --wait
	@echo ""
	@echo "Score event processor: http://localhost:8080/actuator/health"
	@echo "Table retriever:       http://localhost:8083/ping"

## Infra only (for host-based ./gradlew bootRun)
infra:
	$(COMPOSE) up -d --wait zookeeper kafka kafka-init mongo redis

down:
	$(COMPOSE) down

down-clean:
	$(COMPOSE) down -v

ps:
	$(COMPOSE) ps

logs:
	$(COMPOSE) logs -f score-event-processor table-retriever-service

test:
	./gradlew test
