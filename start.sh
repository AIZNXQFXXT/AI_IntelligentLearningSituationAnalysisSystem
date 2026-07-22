#!/usr/bin/env bash
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_PID=""
PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"

cleanup() {
    echo ""
    echo "Shutting down backend (PID: $BACKEND_PID)..."
    [ -n "$BACKEND_PID" ] && kill "$BACKEND_PID" 2>/dev/null || true
    wait "$BACKEND_PID" 2>/dev/null || true
    echo "Done."
}
trap cleanup EXIT INT TERM

cd "$SCRIPT_DIR"

echo "[1/4] Checking environment..."

command -v mvn >/dev/null 2>&1 || { echo "ERROR: mvn not found in PATH"; exit 1; }
command -v java >/dev/null 2>&1 || { echo "ERROR: java not found in PATH"; exit 1; }

if [ -z "${JAVA_HOME:-}" ]; then
    echo "[WARN] JAVA_HOME not set. Maven may fail."
fi

echo "[OK] Environment OK  (profile: $PROFILE)"

echo ""
echo "[2/4] Installing common-module..."
echo "     Running: mvn install -DskipTests -pl common-module -am"
echo "     (This may take a while on first run - downloading dependencies)"
echo ""
mvn install -DskipTests -pl common-module -am || {
    echo ""
    echo "ERROR: common-module compilation failed"
    exit 1
}
echo "[OK] common-module installed"

echo ""
echo "[3/4] Starting backend in background..."
SPRING_PROFILES_ACTIVE="$PROFILE" \
mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true > backend.log 2>&1 &
BACKEND_PID=$!
echo "[OK] Backend is starting (PID: $BACKEND_PID, logs: backend.log)"

echo ""
echo "[4/4] Starting JavaFX client..."
echo "     Client will auto-retry connection while backend starts up."
echo ""
mvn javafx:run -pl client-module
