#!/usr/bin/env bash
# Rebuilds only the Maven modules whose source changed since their jar was last
# built, so `docker compose build`/`up --build` picks up fresh code.
#
# "Changed" = any file under <module>/src or <module>/pom.xml is newer than the
# module's built jar in <module>/target. If a module has no jar yet, it's
# treated as changed too.
#
# Usage:
#   ./rebuild-jars.sh           # only rebuild what changed
#   ./rebuild-jars.sh --all     # force-rebuild every module
#
# Tests are skipped here on purpose -- this script is for fast local/Docker
# iteration, not verification. Run `mvn test` yourself before committing.

set -e
cd "$(dirname "$0")"

MODULES=(eureka-server gateway carrito catalogo clientes envios inventario notificaciones pagos pedidos resenas sucursales)

FORCE_ALL=false
if [ "$1" == "--all" ]; then
  FORCE_ALL=true
fi

TO_BUILD=()

for m in "${MODULES[@]}"; do
  jar=$(find "$m/target" -maxdepth 1 -name "*.jar" 2>/dev/null | head -1)

  if [ "$FORCE_ALL" == "true" ]; then
    echo "[$m] --all -> build"
    TO_BUILD+=("$m")
    continue
  fi

  if [ -z "$jar" ]; then
    echo "[$m] no jar yet -> build"
    TO_BUILD+=("$m")
    continue
  fi

  newest_src=$(find "$m/src" "$m/pom.xml" -type f -newer "$jar" 2>/dev/null | head -1)
  if [ -n "$newest_src" ]; then
    echo "[$m] changed ($newest_src) -> build"
    TO_BUILD+=("$m")
  else
    echo "[$m] up to date -> skip"
  fi
done

if [ ${#TO_BUILD[@]} -eq 0 ]; then
  echo ""
  echo "Nothing changed, all jars up to date."
  exit 0
fi

MODULE_LIST=$(IFS=,; echo "${TO_BUILD[*]}")
echo ""
echo "Building: ${TO_BUILD[*]}"
mvn -q -DskipTests package -pl "$MODULE_LIST"
echo "Done."
