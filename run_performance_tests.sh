#!/bin/bash

API_URL="http://localhost:8081/api/v1/tasks"

echo "=========================================="
echo "TESTES DE PERFORMANCE - High Performance API"
echo "=========================================="
echo ""

# Verificar se API está rodando
echo "Verificando se API está respondendo..."
if ! curl -s -f "$API_URL" > /dev/null 2>&1; then
    echo "ERRO: API não está respondendo em $API_URL"
    echo "Certifique-se de que a API está rodando!"
    echo "Execute: mvn spring-boot:run -Dspring-boot.run.profiles=test"
    exit 1
fi

echo "API está respondendo"
echo ""

# Verificar se ferramentas estão instaladas
command -v wrk >/dev/null 2>&1 || { echo "AVISO: wrk não instalado. Instalando..."; sudo apt install wrk -y; }
command -v ab >/dev/null 2>&1 || { echo "AVISO: Apache Bench não instalado. Instalando..."; sudo apt install apache2-utils -y; }

echo ""
echo "=========================================="
echo "1. TESTE DE THROUGHPUT (> 10.000 req/s)"
echo "=========================================="
echo "Executando: wrk -t12 -c400 -d30s --latency $API_URL"
echo ""
wrk -t12 -c400 -d30s --latency "$API_URL"
echo ""

echo "=========================================="
echo "2. TESTE DE LATÊNCIA (P95 < 50ms, P99 < 100ms)"
echo "=========================================="
echo "Executando: wrk -t12 -c400 -d30s --latency $API_URL"
echo ""
wrk -t12 -c400 -d30s --latency "$API_URL" | grep -A 10 "Latency Distribution"
echo ""

echo "=========================================="
echo "3. TESTE DE TAXA DE ERRO (< 0.1%)"
echo "=========================================="
echo "Executando: ab -n 10000 -c 100 -k $API_URL"
echo ""
ab -n 10000 -c 100 -k "$API_URL" | grep -E "Failed requests|Non-2xx responses|Requests per second"
echo ""

echo "=========================================="
echo "TESTES CONCLUÍDOS"
echo "=========================================="
echo ""
echo "Resultados esperados:"
echo "  - Throughput: > 10.000 req/s"
echo "  - Latência P95: < 50ms"
echo "  - Latência P99: < 100ms"
echo "  - Taxa de erro: < 0.1%"
echo ""
