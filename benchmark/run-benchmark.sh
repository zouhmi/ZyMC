#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
RESULTS_DIR="$SCRIPT_DIR/results"
JAVA_HOME="/opt/jdk-21.0.9+10"
export JAVA_HOME

mkdir -p "$RESULTS_DIR"

echo "========================================"
echo "  ZyMC Benchmark Runner"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"
echo ""
echo "Project:  $PROJECT_DIR"
echo "Results:  $RESULTS_DIR"
echo "Java:     $JAVA_HOME"
echo ""

cd "$PROJECT_DIR"

echo "[1/3] Building project..."
./gradlew :zymc-benchmark:classes --quiet 2>&1
echo "Build complete."
echo ""

echo "[2/3] Running benchmark harness (1GB -> 2GB -> 4GB, 30min each)..."
echo "This will take approximately 90 minutes."
echo ""

JAVA_OPTS="-Xms256m -Xmx1g" ./gradlew :zymc-benchmark:run --args="$RESULTS_DIR" \
    --no-daemon \
    -Dorg.gradle.jvmargs="-Xms256m -Xmx1g" \
    2>&1 | tee "$RESULTS_DIR/benchmark_output.log"

echo ""
echo "[3/3] Benchmark complete."
echo "Results in: $RESULTS_DIR"
echo "  benchmark_report.txt  - Full comparison report"
echo "  phase_1024mb.txt      - 1GB phase detail"
echo "  phase_2048mb.txt      - 2GB phase detail"
echo "  phase_4096mb.txt      - 4GB phase detail"
echo "  benchmark_output.log  - Raw console output"
