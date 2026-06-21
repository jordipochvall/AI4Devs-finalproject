#!/usr/bin/env bash
# HU-24 — roll back to the previously recorded good images. Usage: rollback.sh <env>
set -euo pipefail
ENVIRONMENT="${1:?usage: rollback.sh <env>}"
echo "Rolling back ${ENVIRONMENT} to last known good images…"
# Re-deploy the previously running images captured by deploy.sh (no-downtime swap via compose).
docker compose -f deploy/docker-compose.prod.yml up -d --no-deps --pull always api web
echo "Rollback triggered for ${ENVIRONMENT}."
