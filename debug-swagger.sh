#!/bin/bash

echo "=== DEBUG SWAGGER UI ==="
echo ""
echo "1. Testando endpoint diretamente com curl:"
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"johndoe","password":"password123"}' | \
  grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Erro ao obter token"
  exit 1
fi

echo "✓ Token obtido: ${TOKEN:0:30}..."
echo ""

echo "2. Testando GET /api/v1/tasks:"
RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}\nTIME:%{time_total}" \
  -X GET "http://localhost:8080/api/v1/tasks?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/json")

HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
TIME=$(echo "$RESPONSE" | grep "TIME" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | grep -v "HTTP_CODE\|TIME" | head -1)

echo "  HTTP Code: $HTTP_CODE"
echo "  Tempo: ${TIME}s"
echo "  Tamanho resposta: $(echo "$BODY" | wc -c) bytes"
echo "  Primeiros 200 chars: ${BODY:0:200}..."
echo ""

echo "3. Testando com diferentes headers (simulando Swagger UI):"
curl -s -w "\nHTTP_CODE:%{http_code}\nTIME:%{time_total}\n" \
  -X GET "http://localhost:8080/api/v1/tasks?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: */*" \
  -H "Origin: http://localhost:8080" \
  -H "Referer: http://localhost:8080/swagger-ui.html" \
  -H "User-Agent: Mozilla/5.0" | tail -3

echo ""
echo "4. Verificando CORS preflight:"
curl -s -w "\nHTTP_CODE:%{http_code}\n" \
  -X OPTIONS "http://localhost:8080/api/v1/tasks" \
  -H "Origin: http://localhost:8080" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: authorization" | tail -2

echo ""
echo "5. Verificando se há tasks no banco:"
curl -s -X GET "http://localhost:8080/api/v1/tasks?page=0&size=1" \
  -H "Authorization: Bearer $TOKEN" | jq '.totalElements' 2>/dev/null || \
  curl -s -X GET "http://localhost:8080/api/v1/tasks?page=0&size=1" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"totalElements":[0-9]*' | cut -d: -f2

echo ""
echo "=== FIM DEBUG ==="
