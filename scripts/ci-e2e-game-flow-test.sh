#!/bin/bash
set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://ci-spring-backend:8080}"
MAX_LANDMARK_WAIT_RETRIES=12
LANDMARK_POLL_SECONDS=10

USERNAME="ci-e2e-$(date +%s)"
PASSWORD="CiE2e123!"

echo "Registering $USERNAME ..."
curl -sf -X POST "$BACKEND_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" > /dev/null

TOKEN=$(curl -sf -X POST "$BACKEND_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" \
    | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

echo "Calling init-game against real landmark-processor (Cork coordinates)..."
retries=0
STATUS_CODE=""
while [ "$STATUS_CODE" != "200" ]; do
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BACKEND_URL/api/game/init-game" \
        -H "Authorization: Bearer $TOKEN"   \
        -H "Content-type: application/json" \
        -d '{"latitude":51.894741757894735,"longitude":-8.490317963157894,"angle":0.0}')
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$STATUS_CODE" = "200" ]; then
        echo "OK, init-game returned 200 with real landmark data"
        break
    elif [ "$STATUS_CODE" = "202" ]; then
        retries=$((retries + 1))
        if [ $retries -ge $MAX_LANDMARK_WAIT_RETRIES ]; then
            echo "Error: landmarks never became ready after $((MAX_LANDMARK_WAIT_RETRIES * LANDMARK_POLL_SECONDS))s"
            exit 1
        fi
        echo "Landmarks still preparing (202), waiting ${LANDMARK_POLL_SECONDS}s..."
        sleep $LANDMARK_POLL_SECONDS
    else
        echo "ERROR: init-game returned unexpected status $STATUS_CODE"
        echo "$BODY"
        exit 1
    fi
done

echo "Calling start-round..."
START_ROUND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BACKEND_URL/api/game/start-round"  \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"latitude":51.894741757894735,"longitude":-8.490317963157894,"angle":0.0,"radiusMeters":500.0}')

if [ "$START_ROUND_STATUS" != "200" ]; then
    echo "ERROR: start-round returned $START_ROUND_STATUS (expected 200 — real puzzle-agent call failed)"
    exit 1
fi
echo "OK: start-round returned 200 with a real riddle from puzzle-agent."

echo "Calling submit-answer (deliberately wrong answer — content isn't deterministic against a live service, just checking it responds cleanly)..."
SUBMIT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BACKEND_URL/api/game/submit-answer" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"secondsUsed":30}')

if [ "$SUBMIT_STATUS" != "200" ]; then
    echo "ERROR: submit-answer returned $SUBMIT_STATUS (expected 200 regardless of correct/incorrect answer)"
    exit 1
fi
echo "OK: submit-answer returned 200."

echo "Real full-stack E2E game flow passed against the live CI stack."
