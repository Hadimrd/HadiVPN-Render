# HadiVPN-Render

Experimental VLESS-over-WebSocket proxy for testing on Render's HTTP web-service ingress.

> This is an experiment, not a guarantee of connectivity or anonymity. A Render free web service is not a conventional UDP/TUN VPN server. The configuration uses WebSocket over HTTPS at Render's public HTTP endpoint, and may be blocked, throttled, suspended, or go offline due to free-tier limits. Do not use it for sensitive traffic or rely on it for emergency access.

## Before deploying

1. Set a private UUID in the Render environment as `VPN_UUID`. Do not put the UUID in this repository or share it in public.
2. Set `PORT` to `10000` only if Render does not supply it automatically.
3. Configure the client using the VLESS + WebSocket + TLS parameters:
   - address: the deployed `.onrender.com` hostname
   - port: 443
   - security: TLS
   - transport: WebSocket
   - path: `/vpn`
   - UUID: the value stored in `VPN_UUID`
   - SNI/Host: the deployed `.onrender.com` hostname
4. Test only after Render reports the service as Live. `/health` checks the web front-end, not end-to-end tunnel functionality.

## Run

The Docker image starts Caddy on the public Render port and Xray on loopback. Caddy serves `/health` and forwards WebSocket traffic under `/vpn` to Xray.

Keep secrets outside Git, and do not add a public unauthenticated account.
