# 🛡️ CONSTRAINTS.md — Technical Quality Contract

> **Project:** Fund Circle — Enterprise Collective Capital & Pooled Fund Management  
> **Source Skill:** `constraint-driven-development`  
> **Authority:** Derived from `AGENTS.md` (Level 1 Authority)

This document establishes the non-negotiable engineering constraints, numerical quality thresholds, and mechanical gates for this repository. Every commit, PR, and agent turn is judged against these standards.

---

## 1. The 7 Architectural Invariants (Zero Tolerance)

| # | Invariant | Mechanical Rule / Threshold | Failure Action |
|---|---|---|---|
| **1** | **Whole-Integer Money** | Zero `Float` or `Double` allowed for monetary variables (`amount`, `balance`, `deposit`, `expense`, `target`, `total`). Must be 64-bit integer (`Long`, $\ge 1$). Zero decimals (`.00` is illegal). | `./scripts/verify.sh` fails build. |
| **2** | **$O(1)$ Atomic Aggregates** | Zero client-side ledger loops/sums. Home dashboard reads in exactly **one document read**. Mutations update parent via `FieldValue.increment()`. | Code review gate blocks PR. |
| **3** | **Multi-Tenant Physical Isolation** | All circle data paths scoped under `/funds/{fundId}/...`. Read/write authorization via parent `memberEmails: List<String>` without secondary `get()` queries. | Security rules test failure. |
| **4** | **Immutable Audit Trail** | Zero physical deletions (`delete()` forbidden on transaction collections). Reversals update `isVoid = true`, `voidReason` ($\ge 5$ chars), `voidedAt`, `voidedBy`, and atomically reverse parent balance. | `./scripts/verify.sh` fails build. |
| **5** | **Offline-First Resilience** | Firestore disk cache fixed at 100MB. Continuous `NetworkMonitor` observation. Optimistic local updates show syncing indicator until synced. | Unit / integration test assertion. |
| **6** | **Localization & UiText** | Zero hardcoded user-facing strings in composables or ViewModels. ViewModels emit typed `UiText`. All strings live in `res/values/strings.xml`. | Lint check / review failure. |
| **7** | **Semantic Design & Touch Targets** | All colors resolve from `MaterialTheme.colorScheme` (Emerald `#006D4D` primary). `dynamicColor = false` permanently. Minimum touch target $\ge 40\text{dp}$ (`48\text{dp}` standard). | Lint / Layout accessibility failure. |

---

## 2. Numerical Quality Thresholds

- **Compiler Warnings:** 0 (all warnings treated as errors or explicitly resolved).
- **Android Lint Errors:** 0 (clean run on `./gradlew lintDebug`).
- **Unit Test Pass Rate:** 100% on `./gradlew testDebugUnitTest`.
- **Target SDK:** 35 (Android 15), **Min SDK:** 26 (Android 8.0 Oreo).
- **Memory / Bitmap Bounds:** On-device CSV export avoids memory exhaustion; stream directly to disk cache.
- **Touch Target Floor:** $\ge 48\text{dp} \times 48\text{dp}$ for primary buttons, $\ge 40\text{dp} \times 40\text{dp}$ for filter chips.
- **Typography:** All currency amounts MUST use Tabular Figures (`FontFeatureSettings = "tnum"`).

---

## 3. Trunk-Based Development & Git Workflow Gates

- **Protected Main:** Direct commits to `main` are strictly forbidden. All development must occur on dedicated short-lived feature branches (`feat/slice-<N>-<feature>`).
- **PR Merge Discipline:** `main` is updated exclusively via squash-merge or rebase-merge of approved Pull Requests.
- **Atomic Commits:** Each commit represents a single logical increment with descriptive conventional commit messages.

---

## 4. Automated Quality Gates

Every slice execution MUST satisfy:
```bash
# 1. Pre-flight invariant check
./scripts/verify.sh

# 2. Focused unit test suite
./gradlew testDebugUnitTest

# 3. Static analysis & lint
./gradlew lintDebug
```
