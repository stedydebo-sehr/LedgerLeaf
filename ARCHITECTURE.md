# LedgerLeaf Architecture

LedgerLeaf is an offline-first Android application using Kotlin, Jetpack Compose, Material 3, MVVM, Clean Architecture, Repository Pattern, Hilt, Coroutines/Flow, Room and Preferences DataStore.

## Data flow

`Compose UI → ViewModel → Use Case / Domain → Repository → Room`

Financial records never use DataStore. DataStore contains lightweight preferences only. Reports are generated deterministically from repository data; PDFs render report models rather than recalculating finance values. Backup/restore uses a versioned logical format and validates UUID/relationship integrity before transactional Room replacement.

## Privacy boundary

There is no login, analytics, advertising, tracking or required network service. Android automatic cloud backup is disabled; user backup/export is explicit.

## Release hardening

Critical logic has JVM tests; Room/repository/restore/PDF/destructive behavior has Android integration tests; launch UI has a Compose smoke test. Migrations are centralized in `DatabaseMigrations` so production and migration verification use the same migration objects.
