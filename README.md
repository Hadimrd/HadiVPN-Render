# HadiVPN — مرکز کانفیگ و ابزار اتصال

[![Publish HadiVPN to GitHub Pages](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/deploy-pages.yml/badge.svg)](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/deploy-pages.yml)
[![Build HadiVPN Tools APK](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml/badge.svg)](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml)

## سایت اصلی

**HadiVPN Center:** https://hadimrd.github.io/HadiVPN-Render/

The GitHub Pages site is the primary website and does not depend on the Cloudflare Worker. It contains a responsive mobile dashboard, public subscription mirrors, a config preview/copy tool, connection instructions, PWA install support, and links to Android tools and official V2RayNG releases.

### Public subscription feeds

The Pages workflow refreshes these mirrors every six hours from the public `Alirewa/V2ray-Configs` repository:

- `/feeds/best.txt` — top subscription bundle (Base64 encoded).
- `/feeds/backup.txt` — secondary subscription bundle (Base64 encoded).
- `/feeds/all.txt` — larger plaintext config list.

Example full URLs:

- https://hadimrd.github.io/HadiVPN-Render/feeds/best.txt
- https://hadimrd.github.io/HadiVPN-Render/feeds/backup.txt
- https://hadimrd.github.io/HadiVPN-Render/feeds/all.txt

These are third-party public configurations, not servers operated by HadiVPN. They can fail or be untrusted. No client-side test can guarantee a connection will work on a specific operator; do not treat free public proxies as private or fully trustworthy.

Source and documented refresh interval: https://github.com/Alirewa/V2ray-Configs

## Android app

The current APK is **HadiVPN Tools 1.1.0**. It has the new HadiVPN launcher icon, a link to the GitHub Pages portal, and a direct link to the official V2RayNG client. The native app still tests Cloudflare IPv4 candidates for a VLESS + WebSocket + TLS setup; it is not itself a full V2Ray/Xray VPN tunnel client.

[Open build workflow](https://github.com/Hadimrd/HadiVPN-Render/actions/workflows/build-android-scanner.yml). After a successful build, download the `HadiVPN-Tools-v1.1.0-debug-APK` artifact.

### Stable APK signing

The workflow supports a release-signed APK when the repository is configured with these **GitHub Actions secrets**:

- `HADIVPN_KEYSTORE_BASE64`
- `HADIVPN_KEYSTORE_PASSWORD`
- `HADIVPN_KEY_ALIAS`
- `HADIVPN_KEY_PASSWORD`

The keystore and passwords must be kept private and should never be committed into the repository. Without those secrets, CI builds a debug-signed APK; Android can warn about sideloaded apps, and a later debug build might not upgrade over an earlier CI build signed with a different temporary key. A signed APK still may show an “install unknown apps” prompt outside an app store. Avoid claiming this warning can be disabled safely by changing the app icon.

## Legacy Worker endpoints

The Cloudflare Worker remains available as an optional diagnostics tool:

- https://hadi-netcheck.hadimoradogt.workers.dev/
- https://hadi-netcheck.hadimoradogt.workers.dev/v2ray

If the Worker is inaccessible on a carrier such as Mokhaberat, use the GitHub Pages site above. GitHub Pages itself can still be blocked by network filtering; this is not something a static site can override.

## Project limitations and privacy

- The static website does not relay VPN traffic and does not create a server.
- Public subscription configs are controlled by third parties and may be offline at any time.
- The native scanner runs connection tests from the phone, but it does not test VLESS UUID authentication or the entire VPN tunnel.
- Config parsing/generation in the web UI is local to the browser. Never enter account tokens, private keys, or personal subscription URLs into a public issue.
