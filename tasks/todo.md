# 📋 Fund Circle Living Task Tracker

> **Source Skill:** `planning-and-task-breakdown`  
> **Rule:** Check off items as completed. Add notes for the next session at restartable session boundaries.

---

## 🎯 Slice 0: Project Scaffold & Core Platform Baseline
**Status:** Completed ✅  
**Estimated Scope:** Medium (Scaffolding, Build Catalog, Core Theme, Invariant Verification)

- [x] Task 0.1: Initialize Gradle Version Catalog (`gradle/libs.versions.toml`), `settings.gradle.kts`, `build.gradle.kts`, and Gradle wrapper (8.11.1).
- [x] Task 0.2: Configure `app/build.gradle.kts` targeting Android 15 (Compile SDK 35, Min SDK 26) with Compose, Hilt, Firebase, and JUnit 5/Truth/MockK.
- [x] Task 0.3: Implement Material 3 Semantic Design Tokens (`Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`, `DesignTokens.kt`) adhering strictly to `docs/Design_Tokens.md`.
- [x] Task 0.4: Implement Core MVI & Infrastructure foundation (`BaseViewModel.kt`, `UiText.kt`, `Result.kt`, `DispatchersModule.kt`, `FundCircleApplication.kt`, `MainActivity.kt`).
- [x] Task 0.5: Add baseline unit test (`SanityUnitTest.kt`) and verify `./scripts/verify.sh` passes cleanly with 0 errors.

## 🏁 Checkpoint: End of Slice 0 ✅
- [x] Invariant 1 verified (zero Double/Float in financial fields).
- [x] Invariant 4 verified (zero hard delete calls).
- [x] Gradle build succeeds and unit test suite passes.
- [x] Android Lint passes with 0 errors (`./gradlew lintDebug`).

---

## 🎯 Slice 1: Authentication & User Session (CUJ-01, Screen 01)
**Status:** Pending  
**Estimated Scope:** Medium (Credential Manager One-Tap, EncryptedPrefs, AuthViewModel)
- [ ] Task 1.1: Domain layer: `AuthRepository` interface, `UserSession` model, `SignInWithGoogleUseCase`, `ObserveAuthStateUseCase`.
- [ ] Task 1.2: Data layer: Google One-Tap Credential Manager integration, `SessionLocalDataSource` with `EncryptedSharedPreferences`.
- [ ] Task 1.3: Presentation layer: `AuthViewModel` (MVI), `WelcomeScreen` (Screen 01) with Material 3 buttons, touch target hygiene, and Compose preview.
- [ ] Task 1.4: Unit tests for Auth flows.

## 🏁 Checkpoint: End of Slice 1
- [ ] Auth state machine verified.
- [ ] Unit tests pass: `./gradlew testDebugUnitTest`.

---

## 🎯 Slice 2: Circle Hub & Creation Wizard (CUJ-02 & CUJ-03, Screens 02 & 03)
**Status:** Pending  
**Estimated Scope:** Medium (Circle creation, locked currency, subcollection isolation)
- [ ] Task 2.1: Domain layer: `Fund` entity, `FundRepository` interface, `CreateFundUseCase`, `GetUserFundsUseCase`.
- [ ] Task 2.2: Data layer: Firestore `/funds/{fundId}` creation, currency lock validation (`BDT`, `USD`, `EUR`).
- [ ] Task 2.3: Presentation layer: `CircleHubScreen` (Screen 02), `CreateCircleScreen` (Screen 03).
- [ ] Task 2.4: Unit tests asserting currency lock and multi-tenancy.

---

## 🎯 Slice 3: Circle Home Dashboard & $O(1)$ Aggregates (CUJ-04, Screen 04)
**Status:** Pending  
**Estimated Scope:** Medium ($O(1)$ single-read aggregates, offline persistent cache, network banner)
- [ ] Task 3.1: Domain layer: `GetCircleDashboardUseCase`, `NetworkMonitor` integration.
- [ ] Task 3.2: Data layer: Firestore persistent cache (100MB) settings, single document read of `recordedBalance`, `totalDeposits`, `totalExpenses`.
- [ ] Task 3.3: Presentation layer: `DashboardScreen` (Screen 04) with hero balance, tabular figures (`tnum`), and offline cache banner.
- [ ] Task 3.4: Unit tests for dashboard state and network observer.

---

## 🎯 Slice 4: Transactions Ledger & Add Transaction (CUJ-05, Screens 05 & 07)
**Status:** Pending  
**Estimated Scope:** Large (UUIDv4 idempotency, atomic parent increment, segmented tabs)
- [ ] Task 4.1: Domain layer: `Transaction` models, `RecordDepositUseCase`, `RecordExpenseUseCase`.
- [ ] Task 4.2: Data layer: Batch write to `/funds/{fundId}/deposits` and atomic `FieldValue.increment()` on `/funds/{fundId}`.
- [ ] Task 4.3: Presentation layer: `LedgerScreen` (Screen 05) with segmented tabs, `ModalAddTxScreen` (Screen 07) with 8 expense categories and whole-integer validator.
- [ ] Task 4.4: Unit tests asserting atomic increments and whole-integer monetary invariant.

---

## 🎯 Slice 5: Transaction Details & Reversal Protocol (CUJ-06, Screens 06 & 14)
**Status:** Pending  
**Estimated Scope:** Medium (Immutable audit trail, isVoid, reverse aggregate increment)
- [ ] Task 5.1: Domain layer: `VoidTransactionUseCase` asserting mandatory reason ($\ge 5$ chars) and admin role.
- [ ] Task 5.2: Data layer: Firestore transaction updating `isVoid = true`, `voidReason`, `voidedAt`, `voidedBy` and atomically decrementing parent balance.
- [ ] Task 5.3: Presentation layer: `TransactionDetailScreen` (Screen 06) with transparency audit card, `ReverseTransactionDialog` (Screen 14).
- [ ] Task 5.4: Unit tests verifying immutability (zero physical deletes).

---

## 🎯 Slice 6: Stakeholder Roster & Member Details (CUJ-07, Screens 08–11)
**Status:** Pending  
**Estimated Scope:** Large (Sequential FC-ID, member invitation, self-service KYC mask)
- [ ] Task 6.1: Domain layer: `Member` entity, `AddMemberUseCase`, `GetMemberLedgerUseCase`, `UpdateProfileUseCase`.
- [ ] Task 6.2: Data layer: Firestore subcollection `/funds/{fundId}/members`, sequential FC-ID preview, parent `memberEmails` update.
- [ ] Task 6.3: Presentation layer: `MembersRosterScreen` (Screen 08), `MemberDetailScreen` (Screen 09), `ModalAddMemberScreen` (Screen 10), `EditProfileScreen` (Screen 11).
- [ ] Task 6.4: Unit tests for member ledger and KYC masking.

---

## 🎯 Slice 7: On-Device CSV Reporting & Settings (CUJ-08, Screens 12 & 13)
**Status:** Pending  
**Estimated Scope:** Medium (Pure Kotlin CSV generator, FileProvider, governance)
- [ ] Task 7.1: Domain/Data layer: `LedgerCsvGenerator.kt` streaming directly to cache without Apache POI.
- [ ] Task 7.2: Presentation layer: Android `FileProvider` share sheet (`Intent.ACTION_SEND`), `ReportsScreen` (Screen 12), `SettingsScreen` (Screen 13).
- [ ] Task 7.3: Unit tests verifying CSV formatting and streaming.

---

## 🎯 Slice 8: Production Hardening & Play Store Verification
**Status:** Pending  
**Estimated Scope:** Medium (Firestore security rules, R8 optimization, Play compliance)
- [ ] Task 8.1: Firestore security rules validation in Firebase emulator.
- [ ] Task 8.2: ProGuard / R8 keep rules optimization.
- [ ] Task 8.3: Full quality gate execution (`./scripts/verify.sh`, `./gradlew lintDebug`).
