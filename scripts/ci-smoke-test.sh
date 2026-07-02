#!/bin/bash

set -euo pipefail

# Use container_name, not the bare Compose service name — the dev stack
# (docker-compose.yml) shares this network and uses the same service names
# (spring-backend, landmark-processor, puzzle-agent, mongo), so those bare
# names are ambiguous whenever CI runs alongside dev. container_name is
# globally unique, so it's the only reliable way to address the CI-only container.
BACKEND_URL="${BACKEND_URL:-http://ci-spring-backend:8080}"
LANDMARK_URL="${LANDMARK_URL:-http://ci-landmark-processor:5000}"
PUZZLE_URL="${PUZZLE_URL:-http://ci-puzzle-agent:5000}"


MAX_RETRIES=30
SLEEP_SECONDS=5

wait_for_health() {
    local url=$1
    local name=$2
    local method=${3:-GET}
    local retries=0

    echo "Waiting for $name at $url ..."
    until curl -sf -X "$method" "$url" > /dev/null 2>&1; do
        retries=$((retries + 1))
        if [ $retries -ge $MAX_RETRIES ]; then
            echo "Error: $name did not become healthy after $((MAX_RETRIES * SLEEP_SECONDS))s"
            exit 1
        fi
        sleep $SLEEP_SECONDS
    done
    echo "OK: $name is healthy"
}

wait_for_health "$BACKEND_URL/actuator/health" "Spring Backend"
wait_for_health "$LANDMARK_URL/health" "Landmark Processor"
# puzzle-agent's /health route is registered as POST-only (see services/puzzle-agent/app.py) — GET returns 405.
wait_for_health "$PUZZLE_URL/health" "Puzzle Agent" POST

# Basic API smoke test -register + login
echo "Testing auth endpoints..."
REGISTER_RESPONSE=$(curl -sf -X POST "$BACKEND_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{"username":"ci-smoke-user","password":"CiSmoke123!"}')
echo "Register response: $REGISTER_RESPONSE"

LOGIN_RESPONSE=$(curl -sf -X POST "$BACKEND_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"ci-smoke-user","password":"CiSmoke123!"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
if [ -z "$TOKEN" ]; then
    echo "ERROR: Login did not return a token"
    exit 1
fi
echo "OK: Auth smoke test passed (token received)"

echo "All smoke tests passed."
