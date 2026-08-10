# LedgerLeaf 1.0 Release Checklist

## Automated gates

- [ ] `gradle clean assembleDebug`
- [ ] `gradle testDebugUnitTest`
- [ ] `gradle connectedDebugAndroidTest` on Android 12/API 31+
- [ ] `gradle assembleRelease`
- [ ] `gradle bundleRelease` if an AAB is required
- [ ] Verify release APK/AAB signing certificate

## Data-integrity smoke test

- [ ] Fresh install opens Home without crash.
- [ ] Add normal and backdated expenses.
- [ ] Verify mandatory notes and positive amount validation.
- [ ] Edit an expense and preserve its UUID.
- [ ] Favorite and reuse an entry as an editable draft.
- [ ] Exercise weekly/monthly recurring drafts.
- [ ] Verify History groups and Search filters.
- [ ] Verify Monthly Closing totals after a backdated edit.
- [ ] Delete with double confirmation; verify active views exclude the row.
- [ ] Restore from Recycle Bin and preserve UUID.
- [ ] Archive/restore and verify active/report semantics.
- [ ] Generate report and PDF; inspect totals, breakdowns and multi-page output.
- [ ] Export backup, change data, restore backup, and re-check totals.
- [ ] Reject malformed/newer-format backups without replacing data.

## UX/accessibility smoke test

- [ ] Light theme matches approved cream/brown/deep-green ledger design.
- [ ] Dark theme matches approved dark-forest ledger design.
- [ ] Large font scaling remains usable.
- [ ] TalkBack reaches primary navigation/actions with meaningful labels.
- [ ] Expense/Search/Settings forms remain usable with keyboard open.
- [ ] Test with a large local dataset for scrolling/report responsiveness.

## Release policy

Sprints 18–20 are feature-frozen. Only confirmed defects, data-integrity issues, accessibility regressions, security/privacy issues or release-blocking usability fixes should change v1.0.0.
