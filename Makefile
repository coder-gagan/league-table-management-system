.PHONY: up down down-clean ps logs test smoke-test

COMPOSE := docker compose

## Start infra + all services (waits for health checks)
up:
	$(COMPOSE) up --build -d --wait
	@echo ""
	@echo "Scorekeeper (command):  http://localhost:8081/ping"
	@echo "Score event processor:  http://localhost:8080/actuator/health"
	@echo "Table retriever (query): http://localhost:8083/ping"

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
	$(COMPOSE) logs -f scorekeeper score-event-processor table-retriever-service

test:
	./gradlew test

## E2E: compose stack must be up (make up). Command -> Kafka -> projector -> query.
smoke-test:
	chmod +x scripts/e2e-smoke.sh
	./scripts/e2e-smoke.sh
