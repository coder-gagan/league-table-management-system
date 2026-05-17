# League Table Events

**Integration contract** for the league-table bounded context: Kafka message types shared by publishers and consumers.

[← Root README](../README.md) · Domain logic: [`league-table-domain`](../league-table-domain/)

Consumed and produced by:

- [`scorekeeper`](../scorekeeper/) — **publishes** `UpdatePointsEvent`
- [`score-event-processor`](../score-event-processor/) — **consumes** and projects to MongoDB
- [`table-retriever-service`](../table-retriever-service/) — **consumes** for projection and replay

This module is a **Java library** (not deployed alone). Java package: `com.cqrs.events`. Ranking and table projection live in [`league-table-domain`](../league-table-domain/).

**Topic:** `scoreUpdate` (created by root `docker-compose` / `kafka-init`).

---

## Types

### `UpdatePointsEvent`

One finished fixture applied to a league table.

| Field | Type | Role |
|-------|------|------|
| `tableId` | `String` | League / competition identifier |
| `gameweek` | `Integer` | Matchday index |
| `timeStamp` | `Instant` | Event time (point-in-time replay) |
| `homeTeamDetails` | `Record` | Home club stats |
| `awayTeamDetails` | `Record` | Away club stats |

### `Record`

Per-team contribution from a single match (points 3/1/0, goals, match metadata).

---

## Serialization

- Scorekeeper uses Java serialization helpers for its `KafkaTemplate` producer configuration.
- Consumers often use Spring Kafka `JsonDeserializer` with `TRUSTED_PACKAGES=com.cqrs.events`.

Coordinate format changes across **all** publishers and consumers.

---

## Build

```bash
./gradlew :league-table-events:build
```

---

## Evolving the contract

1. Coordinate with scorekeeper and all consumers before breaking changes.
2. Prefer additive fields.
3. Rebuild and redeploy every service that depends on this module.
