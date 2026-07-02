#!/bin/bash

set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://spring-backend:8080}"
LANDMARK_URL="${LANDMARK_URL:-http://landmark-processor:5000}"
PUZZLE_URL="${PUZZLE_URL:-http://puzzle-agent:5000}"


MAX_RETRIES=30
SLEEP_SECONDS=5

wait_for_health() {
    local url=$1
    local name=$2
    local retries = 0

    echo "Waiting for $name at $url ..."
    until curl -sf "$url" > /dev/null 2>&1; do
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
wait_for_health "$PUZZLE_URL/health" "Puzzle Agent"

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
