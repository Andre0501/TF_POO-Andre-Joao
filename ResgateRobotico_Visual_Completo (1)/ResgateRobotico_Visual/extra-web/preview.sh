#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
RESGATE_PREVIEW_PORT="${PORT:-8080}"
while [ "$#" -gt 0 ]; do
  case "$1" in
    --port) RESGATE_PREVIEW_PORT="$2"; shift 2 ;;
    --host) shift 2 ;;
    --strictPort) shift ;;
    *) echo "Parâmetro de desenvolvimento desconhecido: $1"; exit 1 ;;
  esac
done
exec bash iniciar-web.sh "$RESGATE_PREVIEW_PORT"
