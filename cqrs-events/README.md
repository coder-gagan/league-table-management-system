# CQRS Events

Shared **Kafka contract** for the league-table bounded context. Both [`score-event-processor`](../score-event-processor/) and [`table-retriever-service`](../table-retriever-service/) depend on this module at compile time; types are packaged into each service JAR (this module is **not** deployed on its own).

[← Root README](../README.md)

---

## Purpose

Events are the integration boundary between producers (e.g. a scorekeeper) and consumers (command/query services). Centralising types here avoids drift between services and documents the payload shape in one place.

**Topic:** `scoreUpdate` (created by `docker-compose` / `kafka-init`).

---

## Types

### `UpdatePointsEvent`

Represents one finished fixture applied to a league table.

| Field | Type | Role |
|-------|------|------|
| `tableId` | `String` | League / competition identifier |
| `gameweek` | `Integer` | Matchday index (used for “table up to matchday N” queries) |
| `timeStamp` | `Instant` | Event time (used for point-in-time replay) |
| `homeTeamDetails` | `Record` | Home club stats for this fixture |
| `awayTeamDetails` | `Record` | Away club stats for this fixture |

### `Record`

Per-team contribution from a single match.

| Field | Type | Role |
|-------|------|------|
| `teamName` | `String` | Club name (natural key within a table) |
| `matchNumber` | `Integer` | Fixture identifier |
| `isHomeGame` | `boolean` | Home vs away flag |
| `points` | `Integer` | 3 / 1 / 0 for win / draw / loss |
| `goalsScored` | `Integer` | Goals for |
| `goalsConceded` | `Integer` | Goals against |

---

## Serialization

Consumers use Spring Kafka `JsonDeserializer` with `TRUSTED_PACKAGES=com.cqrs.events`. Custom `UpdatePointsEventSerializer` / `UpdatePointsEventDeserializer` classes exist in each service for low-level `KafkaConsumer` beans used in on-demand replay.

**Example payload (conceptual):**

```json
{
  "tableId": "premier-league-2025",
  "gameweek": 12,
  "timeStamp": "2025-05-17T15:00:00Z",
  "homeTeamDetails": {
    "teamName": "Arsenal",
    "matchNumber": 120,
    "isHomeGame": true,
    "points": 3,
    "goalsScored": 2,
    "goalsConceded": 1
  },
  "awayTeamDetails": {
    "teamName": "Chelsea",
    "matchNumber": 120,
    "isHomeGame": false,
    "points": 0,
    "goalsScored": 1,
    "goalsConceded": 2
  }
}
```

---

## Build

```bash
./gradlew :cqrs-events:build
```

Gradle applies the `java-library` plugin; Lombok generates builders/getters used in tests and services.

---

## Evolving the contract

1. **Coordinate** with every publisher and consumer before breaking changes.
2. Prefer **additive** fields (optional semantics) over renames.
3. Version topics or use a schema registry if multiple event generations must coexist.
4. Rebuild and redeploy both services after contract changes—there is no shared runtime artifact.

---

## Package layout

```
src/main/java/com/cqrs/events/
├── UpdatePointsEvent.java
└── Record.java
```
