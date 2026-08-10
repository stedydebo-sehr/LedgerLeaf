# LedgerLeaf Database

Room is the sole financial datastore. Current Room schema version: **4**.

## Entities

- `_foundation` — internal bootstrap entity.
- `categories` — UUID-backed categories; system defaults are immutable.
- `subcategories` — UUID-backed category children.
- `payment_methods` — system and custom payment methods.
- `expenses` — UUID-backed expenses using integer minor units.
- `expense_subcategories` — many-to-many expense/subcategory references.

## Lifecycle fields

`expenses.deletedAtEpochMillis` powers the six-month Recycle Bin. `expenses.archivedAtEpochMillis` powers Archive independently. Active queries exclude both states; reportable historical queries can include archived records while excluding deleted records.

## Migrations

The production migration chain is centralized in `DatabaseMigrations` and registered by `DatabaseModule`:

- 1 → 2: category/subcategory/expense schema.
- 2 → 3: payment methods plus non-destructive expense-table rebuild to enforce the payment-method foreign key while preserving subcategory relationships.
- 3 → 4: archive-query index.

No destructive fallback is configured.
