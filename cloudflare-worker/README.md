# Cloudflare Worker

The active Worker is deployed in the connected Cloudflare account as `hadi-netcheck`.

- **Network diagnostics:** https://hadi-netcheck.hadimoradogt.workers.dev/
- **VLESS config builder:** https://hadi-netcheck.hadimoradogt.workers.dev/v2ray

Endpoints:
- `/`: responsive Persian-language diagnostics dashboard.
- `/v2ray`: mobile-friendly VLESS + WebSocket + TLS URI builder; parses/imports an existing VLESS link, creates a new URI, copies it, and downloads a TXT copy.
- `/api/ping`: returns the requesting client's observed public IP and available Cloudflare colo/city/country/ASN metadata. Values are returned to the requester and are not intentionally stored by this Worker.
- `/api/ranges`: fetches and validates Cloudflare's current official IPv4 CIDR list and returns it as plain text for the Android app's fallback.
- `/api/health` and `/health`: basic liveness responses.

The VLESS builder processes form values and UUID in the browser; it does not submit them to the Worker. It creates a connection URI only, not a VPN server. A generated link works only when the address, UUID, SNI, Host, path, port, Cloudflare proxy, and origin server are correctly configured. The builder currently supports VLESS + WebSocket + TLS.

The Worker is an HTTP diagnostics/configuration panel, not a VPN tunnel or a remote IP scanner. Browser RTT is measured on the visitor's device; arbitrary-IP TLS + SNI + WebSocket verification is done in the native Android scanner, from the user's own network.

The runtime deployment was uploaded through the Cloudflare API and its workers.dev subdomain was enabled. To edit the live UI/API, use a trusted deployment flow. Do not store account API tokens, UUIDs, passwords, or private keys in this repository.
