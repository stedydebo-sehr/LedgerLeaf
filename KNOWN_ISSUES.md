# Known Issues

## Sprint 2 generated batch
The batch was generated from the user's known-good LL-002 repository, but this execution environment does not include the Android/Gradle build installation used by the LedgerLeaf Codespace. Verification must be performed in Codespaces with:

```bash
gradle clean
gradle assembleDebug
```

Do not commit Sprint 2 until that command succeeds.
