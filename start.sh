#!/bin/sh
set -eu

: "${PORT:=10000}"
export PORT

if [ -z "${VPN_UUID:-}" ]; then
  echo "ERROR: VPN_UUID must be set in Render environment settings." >&2
  exit 1
fi

# Accept canonical UUID syntax only; never store the secret in the repository.
case "$VPN_UUID" in
  ????????-????-????-????-????????????) ;;
  *)
    echo "ERROR: VPN_UUID must be a UUID such as xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx." >&2
    exit 1
    ;;
esac

sed "s/UUID_PLACEHOLDER/$VPN_UUID/g" /etc/xray-config.json.template > /tmp/xray-config.json
xray run -test -config /tmp/xray-config.json
xray run -config /tmp/xray-config.json &
XRAY_PID=$!

cleanup() {
  kill "$XRAY_PID" 2>/dev/null || true
  wait "$XRAY_PID" 2>/dev/null || true
}
trap cleanup INT TERM EXIT

caddy run --config /etc/caddy/Caddyfile --adapter caddyfile
