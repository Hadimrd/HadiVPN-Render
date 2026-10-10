# HadiVPN / Hadi NetCheck

This repository currently contains two experiments:

- **Hadi NetCheck web probe**: https://hadi-netcheck.hadimoradogt.workers.dev/ . It measures browser-to-Cloudflare-Worker HTTP round-trip time and checks reachability of a few public HTTPS endpoints. Those are client-side measurements, not ICMP ping, and the web page cannot truthfully scan arbitrary candidate IPs with a custom TLS SNI.
- **Hadi NetCheck Android scanner**: source in android-scanner/. It probes sampled Cloudflare IPv4 candidates from the Android device using TCP + TLS with the selected SNI/hostname and certificate validation. This is the component intended for actual per-IP network testing.

## Status and limitations

The web Worker is deployed on Cloudflare. The Android scanner source and a GitHub Actions APK build workflow are included; do not consider an APK available until the corresponding Actions run finishes successfully.

A successful TLS test measures reachability for the tested hostname, port, and network at that moment. It does not guarantee that a VPN protocol, WebSocket path, or another hostname will work. Results can change by ISP, time, SNI, and port.

Never put private keys, UUIDs, or account tokens in this repository.
