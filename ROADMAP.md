# LedgerLeaf Roadmap

## Sprint 1 — Foundation

### LL-001 — Project Setup ✅
### LL-002 — Core Foundation & Navigation ✅
### LL-003 — Preferences & App Settings Foundation ✅
- DataStore Preferences
- System / Light / Dark theme persistence
- Currency preference
- Optional monthly budget preference
- Monthly period start preference
- Settings repository + Hilt + Flow ViewModel

## Sprint 2 — Expense Core

### LL-004 — Expense & Category Data Foundation ✅
- UUID-backed Room entities
- Normalized categories/subcategories/expense relations
- Non-destructive migration 1 → 2
- Integer minor-unit money storage
- Soft-delete/archive fields reserved in expense schema

### LL-005 — Repositories & System Defaults ✅
- Category and Expense repositories
- Immutable system category seed definitions
- Locked default subcategories
- Custom category creation foundation
- Hilt repository bindings

### LL-006 — Add Expense Vertical Slice ✅
- Amount, category, subcategory details
- Mandatory detailed notes
- Backdated date/time entry
- Favorite and recurring toggles
- Weekly/monthly recurring template choice
- Save validation through use case
- Currency from Settings

### LL-007 — Dashboard & History Integration ✅
- Live monthly total
- Optional budget display
- Recent expense entries
- History backed by Room/Flow
- Day / Week / Month / Year grouping

**Sprint 2 status: GENERATED — awaiting Codespace `gradle clean && gradle assembleDebug` verification.**

## Next — Sprint 3
Planned only after Sprint 2 build verification and commit. Existing placeholder foundations for Reports, Search, Archive, Recycle Bin, and Budgets remain preserved.
