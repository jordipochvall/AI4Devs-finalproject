#!/usr/bin/env bash
# HU-24 — deploy a version to an environment with IaC + health gate. Usage: deploy.sh <env> <version>
set -euo pipefail
ENVIRONMENT="${1:?usage: deploy.sh <env> <version>}"
export VERSION="${2:?usage: deploy.sh <env> <version>}"

echo "Deploying version ${VERSION} to ${ENVIRONMENT}…"
# Record the current version for rollback before swapping.
docker compose -f deploy/docker-compose.prod.yml ps --format '{{.Image}}' > ".last-good-${ENVIRONMENT}.txt" || true

# Flyway migrations run automatically on API start; compose waits for DB health, then API health.
docker compose -f deploy/docker-compose.prod.yml up -d --pull always

echo "Waiting for API health…"
for i in $(seq 1 30); do
  if docker compose -f deploy/docker-compose.prod.yml exec -T api wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
    echo "API healthy."; exit 0
  fi
  sleep 5
done
echo "API did not become healthy"; exit 1
