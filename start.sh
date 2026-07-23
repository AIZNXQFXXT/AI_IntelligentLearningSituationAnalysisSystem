#!/usr/bin/env bash
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_PID=""

# 选择运行环境:优先用环境变量 SPRING_PROFILES_ACTIVE,否则交互式选择
if [ -n "${SPRING_PROFILES_ACTIVE:-}" ]; then
    PROFILE="$SPRING_PROFILES_ACTIVE"
else
    echo "请选择运行环境:"
    echo "  1) dev  (开发环境,默认)"
    echo "  2) prod (生产环境)"
    read -p "请输入 [1/2] (默认 1): " choice || choice=""
    case "${choice:-}" in
        2) PROFILE="prod" ;;
        *) PROFILE="dev" ;;
    esac
fi

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
    if command -v java >/dev/null 2>&1; then
        export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
        echo "[OK] Auto-detected JAVA_HOME: $JAVA_HOME"
    else
        echo "[ERROR] JAVA_HOME not set and java not found in PATH"
        exit 1
    fi
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
mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true -Dspring-boot.run.profiles="$PROFILE" > backend.log 2>&1 &
BACKEND_PID=$!
echo "[OK] Backend is starting (PID: $BACKEND_PID, profile: $PROFILE, logs: backend.log)"

echo ""
echo "[4/4] Starting JavaFX client..."
echo "     Client will auto-retry connection while backend starts up."
echo ""
mvn javafx:run -pl client-module
