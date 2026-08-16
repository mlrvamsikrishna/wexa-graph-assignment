#!/usr/bin/env zsh
set -euo pipefail

# Expects COGNODB_URI, COGNODB_USER, COGNODB_PASSWORD in environment
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."

MVN_CMD="${MVN_CMD:-}"
if [[ -z "${MVN_CMD}" ]]; then
  if [[ -x "${PROJECT_ROOT}/mvnw" ]]; then
    MVN_CMD="${PROJECT_ROOT}/mvnw"
  elif command -v mvn >/dev/null 2>&1; then
    MVN_CMD="mvn"
  else
    echo "Maven not found. Install Maven or run with MVN_CMD=/absolute/path/to/mvnw" >&2
    exit 1
  fi
fi

"${MVN_CMD}" -q -f "${PROJECT_ROOT}/pom.xml" exec:java
