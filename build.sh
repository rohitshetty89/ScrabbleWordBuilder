#!/usr/bin/env bash
# build.sh — Compiles and packages the Scrabble Word Builder into a self-contained JAR.
# Requires: Java 11 or later (javac, jar)

set -euo pipefail

JAR="target/scrabble-word-builder-1.0.0.jar"
SRC_DIR="src/main/java"
OUT_DIR="target/classes"

echo "Cleaning..."
rm -rf target
mkdir -p "$OUT_DIR"

echo "Compiling..."
javac -d "$OUT_DIR" "$SRC_DIR"/scrabble/*.java

echo "Copying resources..."
cp -r data "$OUT_DIR/"

echo "Packaging..."
jar cfm "$JAR" \
    <(printf 'Main-Class: scrabble.Main\n') \
    -C "$OUT_DIR" .

echo ""
echo "Build successful: $JAR"
