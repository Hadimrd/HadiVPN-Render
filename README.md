# Hadi NetCheck / HadiVPN

[![Build Hadi NetCheck APK](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml/badge.svg)](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml)

## Live Cloudflare panel

**Open:** https://hadi-netcheck.hadimoradogt.workers.dev/

The active Cloudflare Worker currently serves the Hadi NetCheck v1.3 dashboard. It includes:
- a browser-side RTT measurement to the deployed Worker;
- the public IP and available Cloudflare edge metadata seen by the Worker;
- HTTPS reachability checks from the browser;
- a current Cloudflare official IPv4 CIDR-list endpoint at `/api/ranges`;
- a local-only VLESS URI parser for extracting SNI, Host, WebSocket path, and port;
- a profile saved in the visitor's browser, without uploading the whole config string or UUID;
- links to the Android scanner source and APK build workflow.

The HTTP timing shown by the web panel is not ICMP ping and does not prove that an arbitrary Cloudflare IP works for a proxy configuration.

## Android scanner

Source: `android-scanner/`

The Android app is intended for **VLESS + WebSocket + TLS** configurations. From the phone itself, it samples Cloudflare IPv4 candidates and checks TCP reachability, TLS/SNI certificate validation, the WebSocket upgrade response, Host, and path; it then re-tests the fastest candidates.

Provide the exact SNI, Host, WebSocket path, and port from your own config. The scanner does not test gRPC, raw TCP, VLESS UUID authentication, or the complete tunnel. A verified WebSocket handshake is stronger evidence than an open port but is not a guarantee the whole VPN will work.

The official IP-range list is refreshed when reachable and cached on-device. If the official URL is blocked, the app tries the Worker relay; if that also fails, it uses the cached or bundled fallback list. With a complete international internet blackout, it cannot fetch new addresses or force blocked destinations to become reachable.

## APK build

[Open the GitHub Actions workflow](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml). After a successful run, download the `Hadi-NetCheck-debug-APK` artifact. GitHub Actions artifacts expire after 14 days.

## Important limitations

- IP performance depends on carrier/routing, time, SNI, Host, port, and path. Results are network-specific.
- The panel and scanner are diagnostics tools, not themselves a VPN tunnel.
- Never commit account tokens, UUIDs, passwords, or private keys.
