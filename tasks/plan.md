# 🗺️ Master Architectural Plan: Fund Circle

> **Source Skill:** `planning-and-task-breakdown`  
> **Status:** Active Execution Plan  
> **Architecture:** Clean Architecture + MVI (UDF), Jetpack Compose, Material 3, Firebase BaaS (Spark $0)

---

## 1. Dependency Graph

```
Phase 0: Scaffold & Core Baseline (Libs Catalog, Theme, Base MVI, Invariant Gates)
    │
    ├── Phase 1: Authentication & User Session (Credential Manager One-Tap, EncryptedPrefs)
    │       │
    │       └── Phase 2: Circle Hub & Creation Wizard (Circle isolation, locked currency)
    │               │
    │               ├── Phase 3: Dashboard & O(1) Aggregates (Single-read totals, offline banner)
    │               │       │
    │               │       ├── Phase 4: Transactions Ledger & Add Transaction (UUIDv4, Atomic increment)
    │               │       │       │
    │               │       │       └── Phase 5: Transaction Reversal Protocol (isVoid, Reverse increment)
    │               │       │
    │               │       └── Phase 6: Stakeholder Roster & Member Ledger (FC-ID, KYC masking)
    │               │
    │               └── Phase 7: On-Device CSV Reporting & Settings Governance (FileProvider, Admin)
    │
    └── Phase 8: Production Hardening & Play Store Verification (Security rules, R8 keep rules)
```

---

## 2. Vertical Slicing Strategy

Each phase is a self-contained vertical slice delivering working functionality through all layers:
- **Domain Layer:** Pure Kotlin models, repository interfaces, and use cases.
- **Data Layer:** DTOs, mappers, Firestore persistent cache, and repository implementations.
- **Presentation Layer:** MVI `BaseViewModel` with typed `UiText`, StateFlow, and Compose UI screens with semantic design tokens.
- **Verification:** Unit tests asserting the 7 Invariants + pre-flight script `./scripts/verify.sh`.

---

## 3. Risk & Unknowns Mitigation

| Identified Risk | Mitigation Strategy |
|---|---|
| Monetary precision loss / Poisha bugs | Enforce 64-bit `Long` whole units; regex invariant check in `scripts/verify.sh`. |
| Concurrent ledger updates causing balance skew | Firestore atomic `FieldValue.increment()` inside batch/transaction. |
| Offline ledger desync | 100MB Firestore persistent disk cache + real-time `NetworkMonitor`. |
| Unintentional transaction deletions | Zero `delete()` calls allowed; enforce Transaction Reversal Protocol. |
| Context drift across chat sessions | Living task state in `tasks/todo.md`; selective doc loading per session. |
