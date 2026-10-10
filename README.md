# HadiVPN-Render

Experimental VLESS-over-WebSocket proxy prototype. It is not a guarantee of connectivity, privacy, or anonymity, and it has not yet been deployed or end-to-end tested.

> **Provider eligibility matters.** As checked on 2026-10-10, Render's Terms of Service state that users must not be located in, residents/citizens of, or otherwise covered by the listed excluded regions, including Iran. Do not deploy this project to Render if that restriction applies to you; confirm the current provider terms before choosing a host.

## Limitations

- A WebSocket tunnel over an HTTP web-service ingress is not the same as a conventional UDP/TUN VPN.
- A free hosting service may sleep, restart, throttle, or be suspended; availability under severe filtering is not guaranteed.
- Render's free plan currently has a low monthly outbound-bandwidth allowance. Do not use an untested prototype for sensitive traffic, emergencies, or as a promise of anonymity.

## Portable deployment design

The Docker image starts Caddy on the public HTTP port and Xray on loopback. Caddy serves `/health` and forwards WebSocket traffic under `/vpn` to Xray.

For a compatible provider, set a private UUID as `VPN_UUID` in that provider's environment settings. Never commit the UUID or share it publicly.

Client parameters for a host that permits this deployment:
- protocol: VLESS
- address: the host's public hostname
- port: 443 when HTTPS/WSS is provided by the host
- security: TLS
- transport: WebSocket
- path: `/vpn`
- UUID: the private value stored as `VPN_UUID`
- SNI/Host: the public hostname

`/health` tests only the web front-end. End-to-end tunnel functionality must be verified separately.
