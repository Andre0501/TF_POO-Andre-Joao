#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
RESGATE_PORTA="${1:-${PORT:-8080}}"
RESGATE_ARQUIVO="${2:-${RESGATE_DATA:-extra-web/dados/central-web.dat}}"
bash compilar-web.sh
exec java -Duser.timezone=America/Sao_Paulo --add-modules jdk.httpserver -jar ResgateRoboticoVisual.jar "$RESGATE_PORTA" "$RESGATE_ARQUIVO"
