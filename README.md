# LedgerLeaf 🍁

LedgerLeaf is an offline-first Android personal finance journal built with Kotlin, Jetpack Compose, Material 3, Room, Hilt, MVVM and Clean Architecture.

## Version

**1.0.0** — production release candidate implementation after Sprints 1–20.

## Product guarantees

- Android 12 / API 31+
- Fully offline; no login or cloud account
- No ads, analytics or user tracking
- Room is the financial database
- UUID-backed records
- Integer minor-unit money storage
- Mandatory notes for final expenses
- Six-month Recycle Bin
- 18-month Archive lifecycle
- On-demand reports and offline PDF export
- User-controlled offline backup/restore
- Light/dark ledger themes

## Build

```bash
gradle assembleDebug
```

Run local unit tests:

```bash
gradle testDebugUnitTest
```

Run connected Android tests when an emulator/device is available:

```bash
gradle connectedDebugAndroidTest
```

Build the optimized release artifacts:

```bash
gradle assembleRelease
gradle bundleRelease
```

Release signing is intentionally secret-free in source control. Set `LEDGERLEAF_KEYSTORE`, `LEDGERLEAF_STORE_PASSWORD`, `LEDGERLEAF_KEY_ALIAS` and `LEDGERLEAF_KEY_PASSWORD` in the build environment to produce signed release artifacts.

## Release verification

See `RELEASE_CHECKLIST.md` before distributing v1.0.0.
