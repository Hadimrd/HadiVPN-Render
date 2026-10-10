# Hadi NetCheck / HadiVPN

![Android APK build status](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml/badge.svg)

This repository contains the current Hadi NetCheck diagnostics tools and the earlier HadiVPN-Render experiment.

## Live Cloudflare panel

**Open the online panel:** https://hadi-netcheck.hadimoradogt.workers.dev/

The Cloudflare Worker provides:
- browser-to-Worker round-trip timing measured from the visitor's browser;
- the public client IP that Cloudflare observes for that request, plus available Cloudflare edge metadata;
- reachability checks for a few public HTTPS destinations;
- an endpoint that relays Cloudflare's current official IPv4 CIDR list when the Worker and upstream source are reachable.

The panel's HTTP timing is not ICMP ping and is not a per-candidate clean-IP scanner.

## Android scanner

Source: android-scanner/

The native app tests sampled Cloudflare IPv4 candidates from the phone itself. For each candidate, it:
1. opens a TCP connection to the candidate IP and selected TLS port;
2. sets the chosen TLS SNI and validates the certificate for that name;
3. sends an HTTP WebSocket upgrade using the configured Host header and path;
4. accepts a candidate only when the response contains a valid WebSocket 101 upgrade, including the expected Sec-WebSocket-Accept;
5. tests the fastest 20 candidates three more times and ranks the successful candidates by measured TCP+TLS+HTTP-upgrade time and repeatability.

That is stricter than treating an open port or TLS handshake alone as a “clean” IP. It targets VLESS/WebSocket/TLS-style configs. It does not validate gRPC, raw TCP, a UUID/authentication exchange, or every client implementation. Always use the exact SNI, Host, port, and path from the config you intend to use.

The official IPv4 list is refreshed from Cloudflare when possible and stored on-device. If that lookup is blocked, the app tries the Hadi NetCheck Worker relay. If both are unavailable, it uses the last saved list; if none exists, it falls back to the bundled list, which may be older. During a complete internet blackout, the app cannot discover new IPs or make blocked connections succeed.

## APK build

GitHub Actions workflow: https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml

The workflow is intended to build a debug APK and publish a temporary artifact. The APK should be considered available only after the workflow run completes successfully; artifacts expire after 14 days.

## Limitations

- IP quality depends on carrier/routing, location, time, TLS SNI, Host, port, and path. An IP that works for one hostname/config is not automatically clean for another.
- TCP+TLS+WebSocket timing is not ICMP ping. Android app sandboxing does not provide ordinary ICMP echo sockets without privileged/raw-socket support.
- Cloudflare uses anycast and shared edge IPs, so scanner results are time- and network-specific.
- These tools are diagnostic utilities, not a VPN tunnel by themselves. The earlier Render prototype should not be treated as a deployed or working VPN.

Never commit account tokens, UUIDs, passwords, or private keys.
