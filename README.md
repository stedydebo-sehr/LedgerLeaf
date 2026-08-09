# LedgerLeaf
Offline-first Android personal finance journal.

## Current status
Sprint 1 generated complete through LL-003. Sprint 2 Expense Core generated complete through LL-007 and awaits Codespace build verification.

## Locked build baseline
- Android 12+ (`minSdk = 31`)
- compile/target SDK 36
- JDK 17
- Gradle 8.13
- AGP 8.13.2
- Kotlin 2.3.21
- KSP 2.3.11
- Hilt 2.58
- Room 2.8.4
- DataStore Preferences 1.2.1
- Compose + Material 3

Build from the repository root:
```bash
gradle clean
gradle assembleDebug
```
Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
