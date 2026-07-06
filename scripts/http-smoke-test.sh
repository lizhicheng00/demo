#!/usr/bin/env bash
set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
API_BASE="$BASE_URL/open-api-inner/v1/relay-controller"
USER_ID="${USER_ID:-user-001}"
TUNNEL_ID="${TUNNEL_ID:-000001e240}"
GRID_NAME="${GRID_NAME:-grid-a}"

request() {
  local name="$1"
  local method="$2"
  local url="$3"
  local body="${4:-}"
  local user_header="${5:-}"
  local response http_code content
  local headers=(-H "Content-Type: application/json")

  if [[ -n "$user_header" ]]; then
    headers+=(-H "X-User-Id: $USER_ID")
  fi

  if [[ -n "$body" ]]; then
    response=$(curl -sS -X "$method" "$url" "${headers[@]}" -d "$body" -w $'\nHTTP_STATUS:%{http_code}')
  else
    response=$(curl -sS -X "$method" "$url" "${headers[@]}" -w $'\nHTTP_STATUS:%{http_code}')
  fi

  http_code=$(printf '%s' "$response" | awk -F: '/HTTP_STATUS/{print $2}')
  content=$(printf '%s' "$response" | sed '/HTTP_STATUS:/d' | tr '\n' ' ' | cut -c 1-520)
  printf '%-36s %-4s %s\n' "$name" "$http_code" "$content"
}

request "01 create missing user" POST "$API_BASE/tunnel" "{\"name\":\"dev\",\"gridname\":\"$GRID_NAME\",\"type\":\"bridge\"}"
request "02 create invalid type" POST "$API_BASE/tunnel" "{\"name\":\"dev\",\"gridname\":\"$GRID_NAME\",\"type\":\"default\"}" yes
request "03 create bridge" POST "$API_BASE/tunnel" "{\"name\":\"dev\",\"gridname\":\"$GRID_NAME\",\"type\":\"bridge\"}" yes
request "04 list tunnels" GET "$API_BASE/tunnels?gridName=$GRID_NAME" "" yes
request "05 tunnel detail" GET "$API_BASE/tunnel?tunnelId=$TUNNEL_ID" "" yes
request "06 update tunnel env" PUT "$API_BASE/tunnel" "{\"tunnelId\":\"$TUNNEL_ID\",\"type\":\"env\"}" yes
request "07 delete tunnel" DELETE "$API_BASE/tunnel?tunnelId=$TUNNEL_ID" "" yes
request "08 metering" POST "$API_BASE/grids/$GRID_NAME/metering" "{\"tunnelCode\":123456,\"tunnelId\":\"$TUNNEL_ID\",\"usage\":1024}"
request "09 relay status" GET "$API_BASE/tunnel/status?tunnelId=$TUNNEL_ID" "" yes
request "10 create port" POST "$API_BASE/tunnels/$TUNNEL_ID/ports" "{\"port\":8080,\"allowAnonymous\":false}" yes
request "11 list ports" GET "$API_BASE/tunnels/$TUNNEL_ID/ports" "" yes
request "12 get port" GET "$API_BASE/tunnels/$TUNNEL_ID/ports/8080" "" yes
request "13 update port" PUT "$API_BASE/tunnels/$TUNNEL_ID/ports/8080" "{\"allowAnonymous\":true}" yes
request "14 delete port" DELETE "$API_BASE/tunnels/$TUNNEL_ID/ports/8080" "" yes
request "15 gateway port policy" GET "$API_BASE/grids/$GRID_NAME/tunnels/$TUNNEL_ID/ports/8080"
request "16 create rt" POST "$API_BASE/tokens/rt" "{\"tunnelId\":\"$TUNNEL_ID\"}" yes
request "17 openapi yaml" GET "$BASE_URL/openapi.yaml"
