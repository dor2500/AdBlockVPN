# AdBlock VPN (Kotlin / Jetpack Compose)

A system-wide, DNS-filtering ad blocker built on Android's local `VpnService`,
with conditional bypass ("pass-through mode") for user-defined excluded
networks (home Wi-Fi, specific carriers) — the tunnel stays up, only the
filtering turns off.

## Build

You need Android Studio (Koala+) or a standalone Android SDK + JDK 17.

```bash
cd AdBlockerVPN
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

(No `gradlew` wrapper JAR is bundled here — open the project in Android
Studio once and it will regenerate `gradle/wrapper/gradle-wrapper.jar`
automatically, or run `gradle wrapper` if you have Gradle installed locally.)

## Where things live

| Concern | File |
|---|---|
| TUN setup, packet loop, pass-through switch | `vpn/AdBlockVpnService.kt` |
| IPv4/UDP/DNS parsing + synthetic block responses | `vpn/DnsPacketParser.kt` |
| Upstream resolution (protected socket) | `vpn/DnsProxy.kt` |
| Blocklist (hosts-format) storage + matching | `vpn/BlocklistManager.kt` |
| Network change detection (gateway IP / SSID / MCC-MNC) | `vpn/NetworkMonitor.kt` |
| Excluded-network persistence | `data/db/*`, `data/repository/ExcludedNetworkRepository.kt` |
| Blocklist URL / upstream / whitelist / blacklist settings | `data/datastore/SettingsDataStore.kt` |
| Boot auto-start | `receiver/BootReceiver.kt` |
| UI (Dashboard, Excluded Networks, Settings) | `ui/dashboard`, `ui/excluded`, `ui/settings` |

## Known limitation — please read before relying on this

The packet engine only intercepts and answers **DNS (UDP/53)** traffic itself.
Everything else that hits the TUN (the actual HTTP/HTTPS traffic to the sites
you visit) is currently **dropped**, not relayed — see the comment in
`runTunnelLoop()`. A real "route everything through the VPN and only filter
DNS" implementation needs a full user-space NAT/session table (TCP/UDP
passthrough), which normally means embedding a native `tun2socks`-style core
(e.g. via JNI to `badvpn`/`gVisor netstack`/`hev-socks5-tunnel`) rather than
pure Kotlin. That's a substantial native-code subsystem in its own right and
is out of scope for what's written here. To get a fully working blocker
quickly, the two realistic paths are:

1. Wire in an existing open-source `tun2socks` binding (e.g. AdGuard's
   `AdguardDnsLib`/`gomobile`-based tun2socks, or `hev-socks5-tunnel`) for the
   passthrough, and keep this repo's DNS-filtering logic as the "socks5 DNS"
   layer in front of it — this is the architecture apps like AdGuard Home /
   Blokada actually use.
2. Or narrow the product to **DNS-only filtering** (still fully functional
   and useful — it's exactly what Intra/Personal DNS Filter do) and update
   the VPN route to `0.0.0.0/0` for DNS packets only isn't expressible via
   `VpnService.Builder`, so you'd instead only add the TUN's own address as
   the DNS server (already done here) and rely on apps to actually use the
   system resolver — which is what already happens today in this codebase.

Everything else in the spec — conditional bypass without dropping the
tunnel, gateway/SSID/carrier-based network identification without requiring
GPS, Room-backed excluded-network manager, DataStore-backed blocklist/
whitelist/blacklist/upstream settings, boot auto-start, foreground
notification reflecting Active/Paused/Disconnected, and the full Compose UI
— is implemented and wired end-to-end.

## Permissions

- `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` — networking.
- `FOREGROUND_SERVICE` (+ `FOREGROUND_SERVICE_SPECIAL_USE`) — persistent VPN.
- `RECEIVE_BOOT_COMPLETED` — auto-reconnect after reboot.
- `POST_NOTIFICATIONS` — Android 13+ status notification.
- `READ_PHONE_STATE` — carrier MCC-MNC / name for cellular exclusion rules.
- Location is deliberately **not** requested; gateway IP is the primary
  Wi-Fi identifier, SSID is read best-effort and simply comes back `null`
  on devices/OS versions that gate it behind location permission.
