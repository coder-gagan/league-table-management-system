#!/usr/bin/env bash
# End-to-end smoke test: POST /score (command) -> Kafka -> projector -> GET /table (query)
set -euo pipefail

SCOREKEEPER_URL="${SCOREKEEPER_URL:-http://localhost:8081}"
RETRIEVER_URL="${RETRIEVER_URL:-http://localhost:8083}"
TABLE_ID="${TABLE_ID:-e2e-smoke-$(date +%s)}"
POLL_SECONDS="${POLL_SECONDS:-90}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-2}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Required command not found: $1" >&2
    exit 1
  }
}

require_cmd curl
require_cmd jq

echo "==> Health checks"
curl -sf "${SCOREKEEPER_URL}/ping" >/dev/null
curl -sf "${RETRIEVER_URL}/ping" >/dev/null
echo "    scorekeeper and table-retriever are up"

echo "==> POST /score (tableId=${TABLE_ID})"
curl -sf -X POST "${SCOREKEEPER_URL}/score" \
  -H 'Content-Type: application/json' \
  -d "{
    \"header\": { \"correlationId\": \"e2e-smoke\" },
    \"body\": {
      \"scoreRecords\": [{
        \"tableId\": \"${TABLE_ID}\",
        \"homeTeam\": \"Smoke Home FC\",
        \"awayTeam\": \"Smoke Away FC\",
        \"homeTeamGoals\": 2,
        \"awayTeamGoals\": 0,
        \"homeTeamMatchNumber\": 1,
        \"awayTeamMatchNumber\": 1,
        \"gameweek\": 1,
        \"timeStamp\": \"2025-05-17T12:00:00+00:00\"
      }]
    }
  }" | jq -e '.bodyView.statusCode == 200' >/dev/null

echo "==> Waiting for projection (GET /table/${TABLE_ID})"
deadline=$((SECONDS + POLL_SECONDS))
until [ "$SECONDS" -ge "$deadline" ]; do
  if response="$(curl -sf "${RETRIEVER_URL}/table/${TABLE_ID}" 2>/dev/null)"; then
    if echo "$response" | jq -e '
      .standings
      | map(select(.teamName == "Smoke Home FC" and .points == 3))
      | length > 0
    ' >/dev/null; then
      echo "    OK — home team leads with 3 points"
      echo "$response" | jq '.standings[] | {rank, teamName, points}'
      exit 0
    fi
  fi
  sleep "$INTERVAL_SECONDS"
done

echo "FAIL — table not projected within ${POLL_SECONDS}s" >&2
exit 1
