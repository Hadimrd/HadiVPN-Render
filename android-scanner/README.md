# Hadi NetCheck Android Scanner

This is a native Android scanner for testing Cloudflare edge IP candidates from the phone's current network.

## What it measures

- Pulls the currently published IPv4 CIDR list from Cloudflare when reachable; it includes a fallback list of commonly published IPv4 prefixes.
- Samples 100, 250, or 500 candidate IPs from those prefixes.
- Tests the selected TLS ports directly from the phone.
- Sends the chosen domain as TLS SNI and verifies the presented certificate against that hostname.
- Measures elapsed TCP-connect + TLS-handshake time; re-tests the fastest candidates three times and ranks by measured latency/stability.

The latency shown is not ICMP ping. Android apps without privileged/raw-socket access cannot send ordinary ICMP echo requests reliably. TCP+TLS timing is a real test of the same kind of path used by HTTPS-based proxy transports, but does not prove that every VPN protocol or WebSocket route will work.

## Important

Use the exact SNI/Host domain that your client configuration will use. The default www.cloudflare.com is only a baseline test. A candidate IP that works for this SNI is not automatically clean for every other hostname, port, or carrier.

Turn off any existing VPN/proxy if you want to test the direct mobile/fixed-line connection. If a VPN remains enabled, the scan follows that VPN route.

No scan history is uploaded or stored by the app. Candidate probes are made directly from the Android device to each IP.

## Build

The GitHub Actions workflow builds a debug APK on pushes affecting this folder. Open the repository's Actions tab and download the Hadi-NetCheck-debug-APK artifact from the completed run. Debug APK artifacts are temporary build outputs and expire after 14 days.
