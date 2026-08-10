# Decisions

- Android 12 / API 31 minimum; compile/target SDK 36.
- Kotlin + Compose + Material 3 only for UI.
- Room is the sole financial datastore; current schema version 4.
- Preferences DataStore is only for lightweight configuration.
- Hilt DI, MVVM, Clean Architecture and Repository Pattern.
- UUID identity for persisted/domain entities.
- Money stored as integer minor units, never floating point.
- Mandatory expense notes.
- System categories remain immutable; custom categories are user-created UUID records.
- Recycle Bin and Archive remain separate lifecycle states.
- Reports are on-demand; PDF rendering consumes the report model.
- Backup/restore is offline, versioned, user-controlled and validated before transactional replacement.
- No login, ads, analytics, tracking or automatic cloud backup.
- v1.0 release signing secrets are read only from environment variables and never committed.
- Sprint 18 onward is feature-frozen; only release defects are eligible for change.
