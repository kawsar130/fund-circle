## 🎯 Summary of Changes
<!-- Provide a concise 2-3 sentence overview of what this pull request introduces or fixes. -->

## 📑 Related Specifications & CUJ
- **Screen / Feature**: <!-- e.g., SCR-04 (Circle Ledger), SCR-03 (Home Dashboard), etc. -->
- **Task Reference**: <!-- e.g., Task #, Issue #, or documentation section -->

---

## 📋 Mandatory Pre-Flight Architecture Checklist
*All checks must be verified and satisfied before merging into `main` (trunk):*

- [ ] **1. Monetary Precision:** Are all financial figures represented as 64-bit integer (`Long`, $\ge 1$)? (Zero `Float` or `Double`).
- [ ] **2. Whole-Integer Units:** Are all amounts whole currency units without decimals, poisha, or cents?
- [ ] **3. Atomic Aggregates:** Are parent circle totals updated via Firestore `FieldValue.increment()` in a batch/transaction? ($O(1)$ read).
- [ ] **4. Multi-Tenant Isolation:** Are all subcollections explicitly partitioned under `/funds/{fundId}/...`?
- [ ] **5. Immutable Audit Trail:** Are transaction reversals handled via reversal metadata (`isVoid = true`, `voidReason`, `voidedAt`, `voidedBy`) rather than physical deletion?
- [ ] **6. Localization & UiText:** Are all user-facing strings in `res/values/strings.xml` and emitted from ViewModels via `UiText`?
- [ ] **7. Semantic Design Tokens:** Are all UI colors and shapes derived directly from `MaterialTheme.colorScheme` (Emerald `#006D4D`, Slate Navy tokens)? (Zero inline hex codes).
- [ ] **8. Touch Target Hygiene:** Are all interactive touch targets $\ge 40\text{dp}$ / $48\text{dp}$ for primary buttons?
- [ ] **9. Quality Gates:** Do `./gradlew testDebugUnitTest` and `./gradlew lintDebug` pass with 0 errors?

---

## 🧪 Verification & Evidence
<!-- Mention which tests were executed and their results -->
- `./scripts/verify.sh`
- `./gradlew testDebugUnitTest`
