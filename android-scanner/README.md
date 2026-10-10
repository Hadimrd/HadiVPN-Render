# HadiVPN Tools Android

Current app version: **1.1.0** (versionCode 2). The app has a HadiVPN-themed launcher icon and a button to open the GitHub Pages portal.

This native Android utility tests Cloudflare IPv4 candidates from the phone's current network; it is not itself a full V2Ray/Xray tunnel client. For an actual VPN connection, import a trusted subscription or config into a compatible client such as the official [V2RayNG releases](https://github.com/2dust/v2rayNG/releases/latest).

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
- If the official list URL cannot be reached but the optional Hadi NetCheck Worker is reachable, it tries the Worker relay.
- A successfully fetched list is stored locally on the device and can be reused during later disruptions.
- If there has never been a successful fetch, it uses a bundled fallback list that may be older.
- In a total international connection outage, the app cannot fetch new IP ranges or make blocked destinations reachable.

Candidate scan results are not uploaded to Hadi's Worker and are not persisted as a scan history. Only non-secret scan settings and the IP range list are stored locally in app preferences.

## APK build and signing

[GitHub Actions build workflow](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml).

Download the `HadiVPN-Tools-v1.1.0-debug-APK` artifact after a successful run. Debug artifacts expire after 30 days. The workflow can build a stable release-signed APK if the private keystore is configured through Actions secrets. Never commit a signing keystore or its password to this repository. APKs installed outside a trusted app store can still trigger Android's unknown-source or Play Protect warnings.
