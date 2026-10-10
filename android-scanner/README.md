# Hadi NetCheck Android Scanner

A native Android tool for testing Cloudflare IPv4 candidates from the phone's current network.

## Parameters to copy from your client config

- **SNI / Server Name:** the exact TLS server name in the working config.
- **Host header:** the HTTP Host value, if present. Leave blank when it is the same as SNI.
- **WebSocket path:** exactly the path in the config, including its leading slash and any query string.
- **Port:** use the port from the config, typically 443 or another HTTPS port supported by the proxied hostname.

The default SNI is only a placeholder. Replace it with the actual SNI before scanning. The default /ws path is also a placeholder.

## What a successful result means

For each sampled IP and port, the app connects directly to the IP from the Android device, sets TLS SNI, checks the certificate hostname, sends a WebSocket upgrade request with the configured Host and path, and validates the 101 Switching Protocols response and Sec-WebSocket-Accept. It then re-tests the fastest 20 candidates up to three times and displays only those that passed the repeat check.

This is designed for a VLESS + WebSocket + TLS configuration. It does not test VLESS UUID/authentication or the whole data tunnel, and it does not support gRPC or raw TCP testing. A 101 response is strong evidence that the TLS+WebSocket frontend matches the given parameters, not a guarantee that the complete VPN config will transfer traffic successfully.

The displayed measurement is elapsed TCP connect + TLS handshake + WebSocket upgrade time. It is not ICMP ping. Because Cloudflare uses anycast, a candidate working from one network or at one moment can fail from another network or later.

## Offline behavior

- The app fetches Cloudflare's official IPv4 CIDR list directly when possible.
- If the official list URL cannot be reached but the Hadi NetCheck Worker is reachable, it tries the Worker relay.
- A successfully fetched list is stored locally on the device and can be reused during later disruptions.
- If there has never been a successful fetch, it uses a bundled fallback list that may be older.
- In a total international connection outage, the app cannot fetch new IP ranges or make blocked destinations reachable. It only preserves the scan tool and list offline.

Candidate scan results are not uploaded to Hadi's Worker and are not persisted as a scan history. Only non-secret scan settings and the IP range list are stored locally in app preferences.

## Build

A GitHub Actions workflow builds a debug APK on pushes affecting this folder:
https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml

After a successful run, download the Hadi-NetCheck-debug-APK artifact. Artifacts expire after 14 days.
