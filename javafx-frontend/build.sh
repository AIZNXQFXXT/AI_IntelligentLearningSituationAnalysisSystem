#!/bin/bash
set -e

VUE_DIR="/mnt/d/项目/frontend"
JAVAFX_DIR="/mnt/d/项目/javafx-frontend"
VUE_DIST="$VUE_DIR/dist"
VUE_RESOURCES="$JAVAFX_DIR/src/main/resources/vue-app"

echo "=== Step 1: Building Vue frontend ==="
cd "$VUE_DIR"
npm run build

echo "=== Step 2: Copying Vue build output to JavaFX resources ==="
rm -rf "$VUE_RESOURCES"
mkdir -p "$VUE_RESOURCES"
cp -r "$VUE_DIST"/* "$VUE_RESOURCES/"

echo "=== Step 3: Building JavaFX application ==="
cd "$JAVAFX_DIR"
mvn clean compile

echo "=== Build complete ==="
echo "To run: cd $JAVAFX_DIR && mvn javafx:run"
