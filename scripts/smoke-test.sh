#!/usr/bin/env bash
# HU-24 — post-deploy smoke tests against a base URL. Usage: smoke-test.sh <baseUrl>
set -euo pipefail
BASE="${1:?usage: smoke-test.sh <baseUrl>}"
# HU-38: the seed player's password is configurable per environment (SEED_PLAYER_PASSWORD); falls
# back to the dev default so this script still works unmodified against a dev/default deployment.
SEED_PLAYER_PASSWORD="${SEED_PLAYER_PASSWORD:-player123}"

echo "Smoke: health endpoint"
curl -fsS "${BASE}/actuator/health" | grep -q '"status":"UP"'

echo "Smoke: seed login returns a token"
curl -fsS -X POST "${BASE}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"player1@nova.test\",\"password\":\"${SEED_PLAYER_PASSWORD}\"}" | grep -q '"token"'

echo "Smoke: lobby catalogue is reachable (requires auth — expect 401 without token)"
code=$(curl -s -o /dev/null -w '%{http_code}' "${BASE}/api/v1/player/games")
test "$code" = "401"

echo "Smoke tests passed."
