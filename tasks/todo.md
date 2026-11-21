# 📋 Fund Circle Living Task Tracker

> **Source Skill:** `planning-and-task-breakdown`  
> **Rule:** Check off items as completed. Add notes for the next session at restartable session boundaries.

---

## 🔒 Session Start Protocol (Trunk-Based Development Gate)
> **MANDATORY FOR ALL CHATS:** Never commit directly to `main`. Every chat implementing a slice MUST start by syncing `main` and checking out a dedicated feature branch:
> ```bash
> git checkout main && git pull origin main
> git checkout -b feat/slice-<N>-<feature-name>
> ```
> Direct commits to `main` are mechanically rejected by `.githooks/pre-commit`.

---

## 🎯 Slice 0: Project Scaffold & Core Platform Baseline
**Status:** Completed ✅  
**Branch:** `feat/slice-0-scaffold-baseline`  
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
- [x] Full APK build succeeds (`./gradlew assembleDebug`).

---

## 🎯 Slice 1: Authentication & User Session (CUJ-01, Screen 01)
**Status:** Completed ✅  
**Branch Mandate:** `git checkout main && git pull origin main && git checkout -b feat/slice-1-auth-welcome`  
**Estimated Scope:** Medium (Credential Manager One-Tap, EncryptedPrefs, AuthViewModel)
- [x] Task 1.1: Domain layer: `AuthRepository` interface, `UserSession` model, `SignInWithGoogleUseCase`, `ObserveAuthStateUseCase`, `SignOutUseCase`, `GetActiveCircleUseCase`, `SetActiveCircleUseCase`.
- [x] Task 1.2: Data layer: Google One-Tap Credential Manager integration (`GoogleAuthClient`), `SessionLocalDataSource` with `EncryptedSharedPreferences`, `AuthRemoteDataSource` with Firebase Auth and `/users/{userId}` sync.
- [x] Task 1.3: Presentation layer: `AuthViewModel` (MVI), `AuthScreen` (Screen 01: `auth_welcome`) with Material 3 buttons, touch target hygiene ($\ge 48\text{dp}$), typed `UiText`, zero hardcoded strings, and Compose previews (Light, Dark, Loading).
- [x] Task 1.4: Unit tests for Auth flows (Domain use cases, Data repository, Local session data source, ViewModel MVI state machine).

## 🏁 Checkpoint: End of Slice 1 ✅
- [x] Auth state machine verified.
- [x] Unit tests pass: `./gradlew testDebugUnitTest` (39 tests passed across 5 test suites).
- [x] Pre-flight verification passed: `./scripts/verify.sh`.
- [x] Full APK build succeeds: `./gradlew assembleDebug`.

---

## 🎯 Slice 2: Circle Hub & Creation Wizard (CUJ-02 & CUJ-03, Screens 02 & 03)
**Status:** Completed ✅  
**Branch Mandate:** `git checkout main && git pull origin main && git checkout -b feat/slice-2-circle-hub`  
**Estimated Scope:** Medium (Circle creation, locked currency, subcollection isolation)
- [x] Task 2.1: Domain layer: `FundCircle` model, `Currency` enum, `MemberRole` enum, `FundRepository` interface, `CreateCircleUseCase`, `ObserveUserCirclesUseCase`, `ObserveCircleUseCase`.
- [x] Task 2.2: Data layer: `FundCircleDto` and `MemberDto`, Firestore `/funds/{fundId}` creation with UUIDv4 and subcollection `/funds/{fundId}/members/{adminUid}`, atomic 0L aggregates, 5-circle ceiling validation, `FundRepositoryImpl`, Hilt `FundModule`.
- [x] Task 2.3: Presentation layer: `CircleHubScreen` (Screen 02) and `CreateCircleScreen` (Screen 03) with MVI state machines, 48dp touch targets, tabular digits (`tnum`), typed `UiText`, zero hardcoded strings, wired in `FundCircleNavHost`.
- [x] Task 2.4: Unit tests for Repository, UseCases, and ViewModels (71/71 tests passed across 9 suites).

## 🏁 Checkpoint: End of Slice 2 ✅
- [x] Invariant 1 verified (zero Double/Float in financial fields).
- [x] Invariant 4 verified (zero hard delete calls).
- [x] Multi-tenancy subcollection isolation verified (`/funds/{fundId}/members/{adminUid}`).
- [x] Unit tests pass: `./gradlew testDebugUnitTest` (71 tests passed across 9 test suites).
- [x] Pre-flight verification passed: `./scripts/verify.sh` (tests + Android Lint 0 errors).
- [x] Full APK build succeeds: `./gradlew assembleDebug`.

---

## 🎯 Slice 3: Circle Home Dashboard & $O(1)$ Aggregates (CUJ-04, Screen 04)
**Status:** Completed ✅  
**Branch:** `feat/slice-3-dashboard`  
**Estimated Scope:** Medium ($O(1)$ single-read aggregates, offline persistent cache, network banner)
- [x] Task 3.1: Domain layer: `GetCircleDashboardUseCase`, `NetworkMonitor` integration.
- [x] Task 3.2: Data layer: Firestore persistent cache (100MB) settings, single document read of `recordedBalance`, `totalDeposits`, `totalExpenses`.
- [x] Task 3.3: Presentation layer: `DashboardScreen` (Screen 04) with hero balance, tabular figures (`tnum`), and offline cache banner.
- [x] Task 3.4: Unit tests for dashboard state and network observer.

## 🏁 Checkpoint: End of Slice 3 ✅
- [x] Invariant 2 verified: $O(1)$ atomic single-document read dashboard.
- [x] Invariant 5 verified: 100MB persistent cache + NetworkMonitor.
- [x] Unit tests pass: `./gradlew testDebugUnitTest`.
- [x] Pre-flight verification passed: `./scripts/verify.sh`.
- [x] Full APK build succeeds: `./gradlew assembleDebug`.

---

## 🎯 Slice 4: Transactions Ledger & Add Transaction (CUJ-05, Screens 05 & 07)
**Status:** Completed ✅  
**Branch:** `feat/slice-4-ledger`  
**Estimated Scope:** Large (UUIDv4 idempotency, atomic parent increment, segmented tabs)
- [x] Task 4.1: Domain layer: `Transaction` models, `RecordDepositUseCase`, `RecordExpenseUseCase`.
- [x] Task 4.2: Data layer: Batch write to `/funds/{fundId}/deposits` and atomic `FieldValue.increment()` on `/funds/{fundId}`.
- [x] Task 4.3: Presentation layer: `LedgerScreen` (Screen 05) with segmented tabs, `ModalAddTxScreen` (Screen 07) with 8 expense categories and whole-integer validator.
- [x] Task 4.4: Unit tests asserting atomic increments and whole-integer monetary invariant.

## 🏁 Checkpoint: End of Slice 4 ✅
- [x] Invariant 1 verified: Whole-integer positive Long amounts $\ge 1$.
- [x] Invariant 2 verified: Atomic FieldValue.increment() on parent totals.
- [x] Unit tests pass: `./gradlew testDebugUnitTest`.
- [x] Pre-flight verification passed: `./scripts/verify.sh`.
- [x] Full APK build succeeds: `./gradlew assembleDebug`.

---

## 🎯 Slice 5: Transaction Details & Reversal Protocol (CUJ-06, Screens 06 & 14)
**Status:** Completed ✅  
**Branch:** `feat/slice-5-reversals`  
**Estimated Scope:** Medium (Immutable audit trail, isVoid, reverse aggregate increment)
- [x] Task 5.1: Domain layer: `VoidTransactionUseCase` asserting mandatory reason ($\ge 5$ chars) and admin role.
- [x] Task 5.2: Data layer: Firestore transaction updating `isVoid = true`, `voidReason`, `voidedAt`, `voidedBy` and atomically decrementing parent balance.
- [x] Task 5.3: Presentation layer: `TransactionDetailScreen` (Screen 06) with transparency audit card, `ReverseTransactionDialog` (Screen 14).
- [x] Task 5.4: Unit tests verifying immutability (zero physical deletes).

## 🏁 Checkpoint: End of Slice 5 ✅
- [x] Invariant 4 verified: Immutable audit trail with zero physical deletes.
- [x] Reversal protocol verified: isVoid flag + balance neutralization via decrement.
- [x] Unit tests pass: `./gradlew testDebugUnitTest`.
- [x] Pre-flight verification passed: `./scripts/verify.sh`.
- [x] Full APK build succeeds: `./gradlew assembleDebug`.

---

## 🎯 Slice 6: Stakeholder Roster & Member Details (CUJ-07, Screens 08–11)
**Status:** Completed ✅  
**Branch:** `feat/slice-6-members`  
**Estimated Scope:** Large (Sequential FC-ID, member invitation, self-service KYC mask)
- [x] Task 6.1: Domain layer: `Member` entity, `AddMemberUseCase`, `GetMemberLedgerUseCase`, `UpdateProfileUseCase`.
- [x] Task 6.2: Data layer: Firestore subcollection `/funds/{fundId}/members`, sequential FC-ID preview, parent `memberEmails` update.
- [x] Task 6.3: Presentation layer: `MembersRosterScreen` (Screen 08), `MemberDetailScreen` (Screen 09), `ModalAddMemberScreen` (Screen 10), `EditProfileScreen` (Screen 11).
- [x] Task 6.4: Unit tests for member ledger and KYC masking.

## 🏁 Checkpoint: End of Slice 6 ✅
- [x] Invariant 3 verified: Subcollection physical isolation under /funds/{fundId}/members.
- [x] Sequential FC-ID generation and KYC masking verified.
- [x] Unit tests pass: `./gradlew testDebugUnitTest`.
- [x] Pre-flight verification passed: `./scripts/verify.sh`.
- [x] Full APK build succeeds: `./gradlew assembleDebug`.

---

## 🎯 Slice 7: On-Device CSV Reporting & Settings (CUJ-08, Screens 12 & 13)
**Status:** Completed ✅  
**Branch:** `feat/slice-7-reports`  
**Estimated Scope:** Medium (Pure Kotlin CSV generator, FileProvider, governance)
- [x] Task 7.1: Domain/Data layer: `LedgerCsvGenerator.kt` streaming directly to cache without Apache POI.
- [x] Task 7.2: Presentation layer: Android `FileProvider` share sheet (`Intent.ACTION_SEND`), `ReportsScreen` (Screen 12), `SettingsScreen` (Screen 13).
- [x] Task 7.3: Unit tests verifying CSV formatting and streaming.

## 🏁 Checkpoint: End of Slice 7 ✅
- [x] On-device pure Kotlin RFC 4180 CSV export engine verified ($0 server fees).
- [x] Secure internal FileProvider sharing verified.
- [x] Unit tests pass: `./gradlew testDebugUnitTest`.
- [x] Pre-flight verification passed: `./scripts/verify.sh`.
- [x] Full APK build succeeds: `./gradlew assembleDebug`.

---

## 🎯 Slice 8: Production Hardening & Play Store Verification
**Status:** Completed ✅  
**Branch:** `feat/slice-8-hardening`  
**Estimated Scope:** Medium (Firestore security rules, R8 optimization, Play compliance)
- [x] Task 8.1: Cloud Firestore security rules (`firestore.rules`) enforcing multi-tenant isolation, zero-read tenant check, Invariants 1, 2, and 4, and Admin KYC PII isolation.
- [x] Task 8.2: Composite database indexing (`firestore.indexes.json` & `firebase.json`) declaring 4 composite indexes for pagination and filtering.
- [x] Task 8.3: R8 bytecode optimization & resource shrinking (`app/build.gradle.kts` & `app/proguard-rules.pro`) enabling full minification (4.8 MB release APK).
- [x] Task 8.4: Google Play policy & OS hygiene audit (Android 15 edge-to-edge, internal FileProvider, predictive back, zero unnecessary permissions).
- [x] Task 8.5: Full quality gate execution (`./scripts/verify.sh`, `./gradlew lintRelease`, `./gradlew assembleRelease`, `./gradlew assembleDebug`).

## 🏁 Checkpoint: End of Slice 8 ✅
- [x] All 7 Architectural Invariants verified in code, build config, and security rules.
- [x] Unit tests pass: `./gradlew testDebugUnitTest` (277/277 passed).
- [x] Pre-flight verification passed: `./scripts/verify.sh` (Invariants 1 & 4 + Slice 8 hardening + tests + lint).
- [x] Android Lint Release passes with 0 errors (`./gradlew lintRelease`).
- [x] Release APK build succeeds with R8 minification and resource shrinking (`./gradlew assembleRelease`, 4.8 MB).
- [x] Debug APK build succeeds (`./gradlew assembleDebug`).
- [x] GitHub Actions CI green (`✓ Checks passing` on PR #12).
