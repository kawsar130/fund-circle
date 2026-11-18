# 🤖 AGENTS.md — Instructions for AI Coding Agents

> **Project:** Fund Circle — Enterprise Collective Capital & Pooled Fund Management for Android  
> **Target Platform:** Android 8.0+ (Min SDK 26, Target SDK 35, Compile SDK 35), Jetpack Compose (BOM 2024.10.01+), Kotlin 2.0.21+, Material Design 3  
> **Target Audience:** All AI coding assistants (Antigravity, Gemini Code Assist, Claude Code, Cursor, Copilot Workspace, OpenAI Codex) and human engineers.  
> **Primary References:**
> - System Architecture & Tech Stack: [`docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md`](docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md)
> - API Specification & Data Contracts: [`docs/Fund_Circle_API_Specification_v1.0.md`](docs/Fund_Circle_API_Specification_v1.0.md)
> - Design Tokens (Semantic Styling): [`docs/Design_Tokens.md`](docs/Design_Tokens.md)
> - Wireframes & UI Specification: [`docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md`](docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md)
> - Figma Interactive Prototype: [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)
> - Visual Screen Gallery (2x Retina): [`docs/design_preview/gallery.html`](docs/design_preview/gallery.html)

---

## 📖 Documentation Hierarchy & Single Source of Truth (SSOT)

To avoid context drift, hallucinations, and conflicting directives, agents must strictly observe the following order of precedence:

1. **Level 1 (Highest Authority):** `AGENTS.md` (The 7 Invariants and architectural guardrails).
2. **Level 2 (Data Contracts & Wire Boundary):** [`docs/Fund_Circle_API_Specification_v1.0.md`](docs/Fund_Circle_API_Specification_v1.0.md) (Wire protocols, DTOs, repository interfaces, UUID idempotency, and error taxonomy).
3. **Level 3 (Architecture & Infrastructure):** [`docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md`](docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md) (Clean architecture boundaries, Firestore security rules, composite indexes, and Gradle versions).
4. **Level 4 (UI Layouts & Components):** [`docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md`](docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md), [Figma Prototype](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3), and [Visual Screen Gallery](docs/design_preview/gallery.html) (14-screen specs, layout anatomy, and state definitions).
5. **Level 5 (Tokens & Styling):** [`docs/Design_Tokens.md`](docs/Design_Tokens.md) (Material 3 semantic tokens, tabular typography scale, spacing and touch targets).
6. **Level 6 (Interaction Flows):** [`docs/Fund_Circle_User_Flowcharts_v1.0.md`](docs/Fund_Circle_User_Flowcharts_v1.0.md) (Branching logic and user journeys).
7. **Level 7 (Modular Features):** `docs/features/*.md` (Self-contained feature slices for v1.1+ extensions).
8. **Archived / Historical Documents:** Files under `docs/archive/` (`Investment_Firm_App_*`, `Fund_Circle_Branding_and_Style_Guide_v1.0.md`) represent upstream discovery and design narrative. **They must NEVER override active Level 1–7 specifications.**

### ⚖️ Explicit Conflict Resolutions (Resolving Legacy Drift)
- **Voiding vs. 0-Amount Neutralization:** The **Voiding Protocol** (`isVoid = true`, `voidReason`, `voidedAt`, `voidedBy`) is the sole valid correction mechanism. Amount fields remain their original whole positive integer ($\ge 1$). Setting `amount = 0` or arbitrary field edits are forbidden by Firestore security rules.
- **Reporting Engine:** The definitive export pipeline is an **On-Device Kotlin CSV Engine** (`LedgerCsvGenerator.kt`) outputting to cache and shared via Android `FileProvider` (`Intent.ACTION_SEND`). Zero server fees, 100% offline-ready, no Apache POI.
- **Push Notifications:** Handled client-side via Firestore real-time snapshot listeners; FCM available for optional cloud alerts.
- **Base Currencies:** Locked exclusively to `"BDT" | "USD" | "EUR"`.

### 🎯 Selective Context Loading Guide
Do NOT load the entire `docs/` folder into an agent context. Load only the specific document relevant to the active task:
- **Data / Domain / Backend tasks:** Read `Fund_Circle_API_Specification_v1.0.md` & `Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md`.
- **UI / Jetpack Compose tasks:** Read `Fund_Circle_Wireframes_and_UI_Spec_v1.0.md` & `Design_Tokens.md`.
- **Navigation / CUJ tasks:** Read `Fund_Circle_User_Flowcharts_v1.0.md`.
- **Feature Extension tasks:** Read `AGENTS.md` & `docs/features/feature_<name>.md`.

---

## 📌 Project Architecture & Tech Stack

- **Architecture:** Clean Architecture + MVI (Model-View-Intent) with Unidirectional Data Flow (UDF).
- **Presentation Layer:** Jetpack Compose, Material 3, Navigation Compose, `BaseViewModel<UiState, UiAction, UiEffect>`, StateFlow.
- **Domain Layer:** Pure Kotlin (Zero Android SDK dependencies), Immutable Domain Models, 14 Dedicated Use Cases.
- **Data Layer:** Repository Pattern, DTO Mappers, Firestore Offline Persistent Cache (100MB), EncryptedSharedPreferences for local session.
- **Cloud Backend:** 100% Client-Side BaaS on Firebase Spark Plan ($0 Free Tier): Firebase Auth (Google Credential Manager One-Tap), Cloud Firestore (Subcollection Tenancy + Atomic Aggregates + 100MB Disk Cache), On-Device Kotlin CSV Reporting.
- **Dependency Injection:** Dagger Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).

---

## ⛔ The 7 Non-Negotiable Invariants

Any agent making changes to this repository **MUST STRICTLY ADHERE** to these invariants. Violations break data integrity, security authorization, and automated quality gates.

### 1. Whole-Integer Monetary Invariant
- **Strict Prohibition:** `Double` and `Float` **MUST NEVER** be used for financial computations, balances, or database storage.
- **Whole-Integer Model (`Long`):** Every financial figure (deposits, expenses, balances, targets) is stored and validated as a 64-bit integer (`Long`, $\ge 1$).
- **Zero Poisha / Cents:** Decimals, poisha, and fractional cents are strictly forbidden (`.00` is invalid input).
- **Currency Locking:** Currency (`"BDT"` | `"USD"` | `"EUR"`) is permanently locked upon circle creation and can never be modified.

### 2. $O(1)$ Atomic Aggregate Data Pipeline
- **Zero Client Ledger Scanning:** The client **MUST NEVER** scan or aggregate deposit or expense documents to compute totals. The home dashboard must load in exactly **one document read**.
- **Atomic Increments:** Every transaction execution must atomically update parent totals on `/funds/{fundId}` using Firestore `FieldValue.increment()` within a `runBatch` or `runTransaction`:
  ```kotlin
  batch.update(
      fundRef,
      mapOf(
          "totalDeposits" to FieldValue.increment(deposit.amount),
          "recordedBalance" to FieldValue.increment(deposit.amount)
      )
  )
  ```

### 3. Multi-Tenant Physical Isolation & Zero-Read Security
- **Subcollection Isolation:** All circle assets reside strictly under `/funds/{fundId}/...` (`members`, `deposits`, `expenses`, `audit_logs`). Cross-circle querying is strictly prohibited.
- **Zero-Read Authorization:** Circle documents store a `memberEmails: List<String>` array. Security rules authorize read/write operations in $O(1)$ without secondary `get()` document queries.

### 4. Immutable Audit Trail (Zero Hard Deletes)
- **No Physical Deletions:** Transactions cannot be deleted from Firestore.
- **Voiding Protocol:** Voiding a transaction sets `isVoid = true`, records mandatory `voidReason`, `voidedAt`, and `voidedBy`, and atomically reverses the parent aggregate balance via `FieldValue.increment(-originalAmount)`.
- **Permanent Visibility:** Voided transactions remain in the ledger permanently, displayed with strike-through styling and an expandable audit disclosure card.

### 5. Offline-First Resilience & Optimistic UI
- **Persistent Cache:** The Firestore client operates with 100MB persistent disk cache.
- **Network Awareness:** The app continuously observes network connectivity via `NetworkMonitor`.
- **Status Indication:** When offline, an unobtrusive M3 warning banner (`"Viewing Offline Cache"`) is displayed. Optimistic local updates show a pending-sync indicator until acknowledged by cloud nodes.

### 6. Localization & `UiText` Invariant
- **Zero Hardcoded Strings:** Strings in `@Composable` functions or dialogs are **STRICTLY FORBIDDEN**. All user-facing text, labels, error messages, and button titles must be placed in `res/values/strings.xml`.
- **ViewModel Decoupling:** ViewModels must **never** reference Android `Context` or emit raw `String` for feedback. ViewModels emit typed `UiText` (`UiText.StringResource` or `UiText.DynamicString`).

### 7. Semantic Design Tokens & WCAG Touch Target Hygiene
- **Semantic Mapping:** Colors must resolve strictly from `MaterialTheme.colorScheme` (Emerald Primary `#006D4D`, Slate Navy surface tokens). Never hardcode hex colors or static alert palettes inline.
- **Touch Target Hygiene:** Every interactive element must maintain a minimum touch target size of **$\ge 40\text{dp}$**, with **$48\text{dp}$** standard for primary action buttons.
- **Fluid Motion:** All `LazyColumn` items must declare `Modifier.animateItem()`. Expandable cards must use `AnimatedVisibility(enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut())`.

---

## 📚 Canonical Architecture & File Placement

```
com.fundcircle.app/
├── FundCircleApplication.kt                   <--- Application class (@HiltAndroidApp)
├── MainActivity.kt                            <--- Single Activity entry point (@AndroidEntryPoint)
│
├── core/                                      <--- Cross-Cutting Utilities & Infrastructure
│   ├── network/                               <--- NetworkMonitor, ConnectivityManager Flow
│   ├── security/                              <--- EncryptedSharedPreferences, Keystore
│   ├── database/                              <--- Firestore instance provider, TypeConverters
│   └── common/                                <--- Result<T>, UiText, DispatchersModule, DateUtils
│
├── domain/                                    <--- Pure Kotlin Business Logic (Zero Android SDK)
│   ├── model/                                 <--- Immutable Domain Entities (Fund, Member, Transaction)
│   ├── repository/                            <--- Repository Interfaces (FundRepository, AuthRepository)
│   └── usecase/                               <--- Use Cases (RecordDepositUseCase, VoidTransactionUseCase, etc.)
│
├── data/                                      <--- Data Layer Implementation
│   ├── datasource/
│   │   ├── remote/                            <--- FirestoreRemoteDataSource, AuthRemoteDataSource
│   │   └── local/                             <--- SessionLocalDataSource (EncryptedPrefs)
│   ├── mapper/                                <--- DTO <-> Domain Entity Mappers
│   └── repository/                            <--- Repository Implementations
│
└── presentation/                              <--- UI Layer (Jetpack Compose & MVI)
    ├── navigation/                            <--- Type-Safe NavHost & Route Definitions
    ├── theme/                                 <--- Color, Type, Shape, Theme tokens (Material 3)
    ├── components/                            <--- Reusable Atoms & Molecules (Buttons, Cards, Chips)
    └── screens/                               <--- Verified MVI Screens
            ├── auth/                          <--- AuthViewModel, AuthScreen
            ├── hub/                           <--- CircleHubViewModel, CircleHubScreen
            ├── dashboard/                     <--- DashboardViewModel, DashboardScreen
            ├── ledgers/                       <--- DepositsViewModel, ExpensesViewModel
            ├── members/                       <--- MembersViewModel, MemberDetailSheet
            ├── analytics/                     <--- AnalyticsViewModel, AnalyticsScreen
            └── settings/                      <--- SettingsViewModel, SettingsScreen
```

---

## 📋 Mandatory Agent Pre-Flight Architecture Checklist

Before submitting code or proposing changes, verify every requirement:
- [ ] **1. Monetary Precision:** Are all money fields 64-bit `Long`? (Zero `Float`/`Double`).
- [ ] **2. Whole-Integer Units:** Are amounts whole currency units without decimals/cents?
- [ ] **3. Atomic Aggregates:** Are parent totals updated via Firestore `FieldValue.increment()`?
- [ ] **4. Multi-Tenancy:** Are all document paths scoped under `/funds/{fundId}/...`?
- [ ] **5. Audit Trail:** Are transaction voids handled via metadata instead of physical deletions?
- [ ] **6. Localization:** Are all user-facing strings in `strings.xml` and emitted via `UiText`?
- [ ] **7. Semantic Design:** Are colors and shapes derived from `MaterialTheme`?
- [ ] **8. Touch Targets:** Are all interactive touch targets $\ge 40\text{dp}$ / $48\text{dp}$?
- [ ] **9. Quality Gates:** Do unit tests and lint checks pass cleanly with 0 errors?

---

## ⚡ Fast Targeted CLI Commands & Quality Gates

When verifying your work, use targeted commands for maximum speed:

```bash
# 1. Run unit test suite
./gradlew testDebugUnitTest

# 2. Run static analysis & Android Lint
./gradlew lintDebug

# 3. Render Compose preview in sidecar pane
android preview render

# 4. Inspect UI layout and accessibility hierarchy
android layout

# 5. Build, install, and run on connected device/emulator
android run --variant=debug

# 6. Check Firebase security rules validity
firebase emulators:exec --only firestore "./gradlew testDebugUnitTest"
```

---

## 💡 Common Pitfalls to Avoid

- **Do NOT query Firestore directly from ViewModels or Composables**: Always route data access through `Domain UseCases` &rarr; `Repositories` &rarr; `DataSources`.
- **Do NOT calculate circle balances on the Android client**: Read `recordedBalance` directly from the parent `/funds/{fundId}` document ($O(1)$ read).
- **Do NOT accept future dates for deposits or expenses**: Enforce `selectedDate <= System.currentTimeMillis()`.
- **Do NOT use raw system back handlers**: Use Compose `BackHandler` / `PredictiveBackHandler` from `androidx.activity.compose` with `android:enableOnBackInvokedCallback="true"` for smooth predictive back support.
- **Do NOT hardcode dark-mode colors or enable dynamic color**: Use semantic tokens from `MaterialTheme.colorScheme` (`primary`, `surface`, `errorContainer`, etc.) with `dynamicColor = false` to preserve financial status colors and prevent crashes on Android < 12.
- **Do NOT post notifications without a `NotificationChannel`**: Android 8.0+ (API 26+) silently drops notifications posted without an initialized channel; always request `POST_NOTIFICATIONS` at runtime on Android 13+ (API 33+).
- **Do NOT ignore `Scaffold` inner padding**: Android 15 enforces edge-to-edge; ignoring `innerPadding` causes UI controls to clip behind transparent system bars.
