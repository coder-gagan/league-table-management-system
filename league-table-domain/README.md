# League Table Domain

**Domain library** for league-table standings: MongoDB entity shapes, ranking rules, and event-to-table projection.

[← Root README](../README.md) · Contract: [`league-table-events`](../league-table-events/)

Used by [`scorekeeper`](../scorekeeper/), [`score-event-processor`](../score-event-processor/), and [`table-retriever-service`](../table-retriever-service/) so ranking and reconstruction logic lives in one place (DRY).

---

## Contents

| Type | Purpose |
|------|---------|
| `com.cqrs.table.model.Standing` | Per-team row in a table |
| `com.cqrs.table.model.PointsTable` | MongoDB document (`points_table` collection) |
| `com.cqrs.table.ranking.TableRanking` | Sort comparator + assign ranks 1…n |
| `com.cqrs.table.LeagueTableReconstructor` | Apply `UpdatePointsEvent` list to a `PointsTable` |

### Ranking order (`TableRanking`)

1. Points (desc)
2. Goal difference (desc)
3. Goals scored (desc)
4. Goals conceded (asc)

Each service exposes a thin `@Component` subclass `ReconstructTableUtil extends LeagueTableReconstructor` for Spring injection without duplicating logic.

---

## Build & test

```bash
./gradlew :league-table-domain:test
```

Domain scenarios (home win, draw, tie-breakers, multi-event replay) are tested in `LeagueTableReconstructorTest`.

---

## Dependencies

- **`league-table-events`** — `UpdatePointsEvent` / `Record` on the wire
- **Spring Data MongoDB** (compile-only) — `@Document` / `@Id` on `PointsTable`
