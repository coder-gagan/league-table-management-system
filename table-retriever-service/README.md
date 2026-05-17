# Table Retriever Service

**Query side** of the league-table CQRS split: serves league tables over HTTP, maintains a MongoDB projection from Kafka, and answers **temporal** questions by **replaying** the `scoreUpdate` log (with Redis caching for hot paths).

Commands are handled by [`scorekeeper`](../scorekeeper/); this service **reads only**.

[← Root README](../README.md) · Command: [`scorekeeper`](../scorekeeper/) · Contract: [`league-table-events`](../league-table-events/)

| | |
|--|--|
| **Port** | 8083 |
| **Health** | `GET /ping` |
| **Stack** | Spring Web, Spring Kafka, MongoDB, Redis |

---

## Read paths

| Use case | Mechanism | Endpoint |
|----------|-----------|----------|
| **Latest table** | MongoDB snapshot | `GET /table/{tableId}` |
| **Table after matchday N** | Kafka replay (`gameweek ≤ N`) | `GET /table/{tableId}/matchday/{matchday}` |
| **Table at instant** | Kafka replay (before timestamp) | `GET /table/{tableId}?instant={epochSeconds}` |

Projections are fed by the same `scoreUpdate` stream that [`score-event-processor`](../score-event-processor/) consumes, plus optional direct updates from scorekeeper in non-CQRS mode.

---

## HTTP API

### `GET /ping`

Liveness check.

### `GET /table/{tableId}`

Latest snapshot; `204` if unknown.

Optional `?instant={epochSeconds}` replays events before that UTC time.

### `GET /table/{tableId}/matchday/{matchday}`

Standings after all events with `gameweek ≤ matchday`.

```bash
curl -s http://localhost:8083/table/premier-league-2025/matchday/10 | jq
```

---

## Kafka integration

- **Live projection** — `ScoreEventBatchListener` keeps MongoDB warm for fast reads.
- **On-demand replay** — `ScoreEventRetriever` reads from earliest offset, filters by `tableId` / time / matchday.

---

## Caching

Redis-backed `@Cacheable` on replay endpoints when `cache.enabled=true`.

---

## Run locally

```bash
./gradlew :table-retriever-service:bootRun
```

Requires Kafka, MongoDB, and Redis (`make infra` from repo root).

---

## Tests

```bash
./gradlew :table-retriever-service:test
```

Includes controller, replay orchestration, `ScoreEventRetriever` filtering, and cross-layer integration tests.
