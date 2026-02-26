#!/bin/bash

API_URL="http://localhost:8081/api/v1/tasks"

echo "=========================================="
echo "PERFORMANCE TESTS - High Performance API"
echo "=========================================="
echo ""

# Check if API is running
echo "Checking if API is responding..."
if ! curl -s -f "$API_URL" > /dev/null 2>&1; then
    echo "ERROR: API is not responding at $API_URL"
    echo "Make sure the API is running!"
    echo "Run: mvn spring-boot:run -Dspring-boot.run.profiles=test"
    exit 1
fi

echo "API is responding"
echo ""

# Check if tools are installed
command -v wrk >/dev/null 2>&1 || { echo "WARNING: wrk not installed. Installing..."; sudo apt install wrk -y; }
command -v ab >/dev/null 2>&1 || { echo "WARNING: Apache Bench not installed. Installing..."; sudo apt install apache2-utils -y; }

echo ""
echo "=========================================="
echo "1. THROUGHPUT TEST (> 10,000 req/s)"
echo "=========================================="
echo "Running: wrk -t12 -c400 -d30s --latency $API_URL"
echo ""
wrk -t12 -c400 -d30s --latency "$API_URL"
echo ""

echo "=========================================="
echo "2. LATENCY TEST (P95 < 50ms, P99 < 100ms)"
echo "=========================================="
echo "Running: wrk -t12 -c400 -d30s --latency $API_URL"
echo ""
wrk -t12 -c400 -d30s --latency "$API_URL" | grep -A 10 "Latency Distribution"
echo ""

echo "=========================================="
echo "3. ERROR RATE TEST (< 0.1%)"
echo "=========================================="
echo "Running: ab -n 10000 -c 100 -k $API_URL"
echo ""
ab -n 10000 -c 100 -k "$API_URL" | grep -E "Failed requests|Non-2xx responses|Requests per second"
echo ""

echo "=========================================="
echo "TESTS COMPLETED"
echo "=========================================="
echo ""
echo "Expected results:"
echo "  - Throughput: > 10,000 req/s"
echo "  - P95 Latency: < 50ms"
echo "  - P99 Latency: < 100ms"
echo "  - Error rate: < 0.1%"
echo ""
