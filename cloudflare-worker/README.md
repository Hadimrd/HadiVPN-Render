# Cloudflare Worker

The active Worker is deployed in the connected Cloudflare account as `hadi-netcheck`:

https://hadi-netcheck.hadimoradogt.workers.dev/

Endpoints:
- `/`: responsive Persian-language online diagnostics panel.
- `/api/ping`: returns the requesting client's observed public IP and available Cloudflare colo/city/country/ASN metadata. Values are returned to the requester and are not intentionally stored by this Worker.
- `/api/ranges`: fetches and validates Cloudflare's current official IPv4 CIDR list and returns it as plain text for the Android app's fallback.
- `/health`: basic liveness response.

The Worker is an HTTP diagnostics panel, not a VPN tunnel or a remote IP scanner. Browser RTT is measured on the visitor's device; arbitrary-IP TLS + SNI + WebSocket verification is done in the native Android scanner, from the user's own network.

The runtime deployment was uploaded through the Cloudflare API and its workers.dev subdomain was enabled. To edit the live UI/API, open the Worker in the Cloudflare dashboard, or update the deployed Worker script through a trusted deployment flow. Do not store account API tokens in this repository.
