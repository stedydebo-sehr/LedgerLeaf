# Architecture
`Compose UI → ViewModel → Use Case / Domain Logic → Repository → Room`

UI must never access Room directly. UUID is the identity strategy for persisted/domain entities. Only packages needed by implemented features are committed.
