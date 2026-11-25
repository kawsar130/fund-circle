# 🏛️ Fund Circle — Enterprise Collective Capital & Pooled Fund Management

[![Android Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--35)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.10.01%2B-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material Design 3](https://img.shields.io/badge/Material%20Design-3%20Expressive-7D5260?logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI%20(UDF)-006D4D)](docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md)
[![Firebase BaaS](https://img.shields.io/badge/Backend-Firebase%20Spark%20($0%20Tier)-FFCA28?logo=firebase&logoColor=black)](docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md)
[![License](https://img.shields.io/badge/License-Proprietary-red)](#)

> **Enterprise Collective Capital & Pooled Fund Management for Android**  
> Built for investment syndicates, cooperative savings clubs, land purchase pools, and SME joint ventures. Eliminates informal spreadsheets with an immutable audit trail, atomic $O(1)$ single-read aggregate accounting, multi-tenant physical isolation, and on-device CSV reporting.

---

## 🎨 Live Design & Cloud Assets

| Asset | Link | Description |
| :--- | :--- | :--- |
| **Figma Interactive Prototype** | [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3) | Live cloud-hosted interactive prototype demonstrating all 14 screens + Design Tokens directly inside Figma. |
| **Visual Screen Gallery (2x Retina)** | [`docs/design_preview/gallery.html`](docs/design_preview/gallery.html) | High-resolution visual gallery showcasing all 14 Android screens with routes and invariant compliance. |
| **Master FigJam Architecture** | [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI) | Full visual state machines mapping CUJ-01 through CUJ-08. |
| **Local Design Prototype** | [`docs/design_preview/index.html`](docs/design_preview/index.html) | Standalone responsive HTML/CSS/JS prototype with instant theme toggling and screen switching. |

---

## ⛔ The 7 Non-Negotiable Invariants

All development in this repository strictly adheres to the core architectural invariants defined in [`AGENTS.md`](AGENTS.md):

1. **Whole-Integer Monetary Invariant (`Long` $\ge 1$):** `Double` and `Float` are strictly forbidden for financial calculations or storage. All figures represent whole currency units; fractional cents or poisha are rejected at the input boundary.
2. **$O(1)$ Atomic Aggregate Data Pipeline:** Client-side ledger scans are strictly prohibited. Circle totals (`totalDeposits`, `totalExpenses`, `recordedBalance`) load in exactly **one document read** and are updated atomically via Firestore `FieldValue.increment()`.
3. **Multi-Tenant Physical Isolation & Zero-Read Security:** All circle assets reside strictly under `/funds/{fundId}/...`. Read/write authorization is evaluated in $O(1)$ using the parent document's `memberEmails` array without secondary queries.
4. **Immutable Audit Trail (Zero Hard Deletes):** Transactions are never deleted from Firestore. Voiding marks a record with `isVoid = true`, records mandatory audit metadata (`voidReason`, `voidedAt`, `voidedBy`), and atomically reverses parent aggregates.
5. **Offline-First Resilience & Optimistic UI:** Firestore operates with a 100MB persistent disk cache. Unobtrusive indicators warn users when displaying cached data, and optimistic local writes sync automatically upon reconnection.
6. **Localization & `UiText` Decoupling:** Composable functions never hardcode strings. ViewModels emit typed `UiText` (`StringResource` or `DynamicString`), keeping business logic free from Android `Context`.
7. **Semantic Design Tokens & WCAG Touch Target Hygiene:** Colors derive strictly from `MaterialTheme.colorScheme` (Emerald `#006D4D` primary). Interactive controls enforce $\ge 40\text{dp}$ touch targets ($48\text{dp}$ standard).

---

## 📚 Canonical Documentation Suite

| Document | Authority Level | Description |
| :--- | :--- | :--- |
| **[`AGENTS.md`](AGENTS.md)** | **Level 1 (Highest)** | The 7 Invariants, architectural guardrails, pre-flight checklist, and fast verification CLI commands. |
| **[`docs/Fund_Circle_API_Specification_v1.0.md`](docs/Fund_Circle_API_Specification_v1.0.md)** | **Level 2 (Data & Contracts)** | Wire protocols, DTOs, Repository interfaces, UUID idempotency, and error taxonomy. |
| **[`docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md`](docs/Fund_Circle_System_Architecture_and_Tech_Stack_v1.0.md)** | **Level 3 (Infra & Security)** | Clean architecture layers, Firestore security rules, composite indexes, and Gradle catalogs. |
| **[`docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md`](docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md)** | **Level 4 (UI Layouts)** | 14-screen inventory, Material 3 layout grids, empty/loading/error states, and interactive dialogs. |
| **[`docs/Design_Tokens.md`](docs/Design_Tokens.md)** | **Level 5 (Styling Tokens)** | Material 3 tonal palettes, tabular typography scale (`tnum`), spacing rules, and elevation tokens. |
| **[`docs/Fund_Circle_User_Flowcharts_v1.0.md`](docs/Fund_Circle_User_Flowcharts_v1.0.md)** | **Level 6 (Interaction Flows)** | Mermaid state machine diagrams mapping core user journeys (CUJ-01 to CUJ-08). |
| **[`docs/playstore/`](docs/playstore/)** | **Policy & Compliance** | [Play Store Listing & 20-Tester Guide](docs/playstore/Play_Store_Listing_and_Policy_Guide.md) and [Production Privacy Policy](docs/playstore/Privacy_Policy.md). |
| **[`docs/features/`](docs/features/)** | **Feature Extensions** | Standardized slice templates and isolation guide for v1.1+ feature extensions. |

---

## 📱 14-Screen Verified UI Inventory

```
docs/design_preview/index.html (Figma Node 42:3)
├── 01. Screen 01: Sign-In Screen (auth_welcome) — Google One-Tap Credential Manager
├── 02. Screen 02: Circle Hub (circle_hub) — Managed circles (1/5) & joined circles (1)
├── 03. Screen 03: Create Circle Wizard (circle_create) — Name, locked base currency (BDT/USD/EUR)
├── 04. Screen 04: Circle Home Dashboard (dashboard_home) — O(1) balance metrics, member progress bar
├── 05. Screen 05: Transactions Ledger (ledger_transactions) — Segmented tabs [Deposits] & [Expenses]
├── 06. Screen 06: Transaction Details (modal_tx_detail) — Immutable values, Admin reverse action, audit disclosure
├── 07. Screen 07: Add Transaction Modal (modal_add_tx) — Segmented switcher, 5 validated fields
├── 08. Screen 08: Stakeholders Roster (members_roster) — Search bar, FC-ID badges, status chips
├── 09. Screen 09: Member Details & Ledger (member_detail) — Cumulative total, personal deposit history
├── 10. Screen 10: Add Stakeholder Modal (modal_add_member) — 11-digit phone, Gmail, sequential FC-ID preview
├── 11. Screen 11: Edit My Profile (edit_profile) — Private self-service KYC, NID masked preview (•••• •••• 0123)
├── 12. Screen 12: Reports Screen & Live Preview (reports_export) — Period filters, statement preview, CSV export
├── 13. Screen 13: Settings & Governance (settings_admin) — Circle switcher, theme toggle, Danger Zone
└── 14. Screen 14: Reverse Transaction Dialog (dialog_reverse_tx) — Immutable warning, mandatory reason (≥5 chars)
```

---

## 🛠️ Technology Stack & Platform Compatibility

- **Target OS Range:** Android 8.0 (API 26) through Android 15 (API 35) — 95.5%+ global device reach.
- **UI Framework:** Jetpack Compose (BOM 2024.10.01+), Material Design 3, Navigation Compose.
- **Language & Runtime:** Kotlin 2.0.21+, Coroutines, StateFlow, JVM 17.
- **Dependency Injection:** Dagger Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
- **Cloud Backend:** 100% Client-Side BaaS on Firebase Spark Plan ($0 Free Tier):
  - Firebase Authentication (Google Credential Manager One-Tap)
  - Cloud Firestore (100MB persistent disk cache, atomic `FieldValue.increment()`)
- **Reporting Engine:** On-device Kotlin CSV Generator (`LedgerCsvGenerator.kt`) shared via Android `FileProvider` (`Intent.ACTION_SEND`). Zero server fees.
- **Security & Storage:** Android Jetpack Security (`EncryptedSharedPreferences`, Hardware Keystore).

---

## ⚡ Fast Quality Gates & Verification Commands

```bash
# Run unit test suite
./gradlew testDebugUnitTest

# Run Android Lint & static analysis
./gradlew lintDebug

# Verify Roborazzi screenshot tests
./gradlew verifyRoborazziDebug

# Build and install on connected device / emulator
android run --variant=debug
```

### 🔐 Release Signing & Required GitHub Secrets

Pushing a `v*` tag runs [`.github/workflows/release.yml`](.github/workflows/release.yml), which runs the unit tests, builds a signed AAB with `./gradlew bundleRelease`, and uploads it to the Play internal track. Configure these repository secrets (names only — never commit values):

| Secret | Purpose |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | Base64 of the upload keystore; decoded to `app/release-keystore.jks` (git-ignored). |
| `KEYSTORE_PASSWORD` | Keystore password. |
| `KEY_ALIAS` | Upload key alias. |
| `KEY_PASSWORD` | Upload key password. |
| `WEB_CLIENT_ID` | OAuth web client ID for Google Credential Manager sign-in. |
| `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` | *Optional.* Play Console service account JSON; without it the Play upload is skipped and the AAB is kept as a workflow artifact. |

Gradle resolves each value from `-P<NAME>`, then the `<NAME>` environment variable, then `local.properties` (set `KEYSTORE_FILE` to use a keystore other than `app/release-keystore.jks`). Without signing values, release builds fall back to the debug key so `assembleRelease` still works in CI, but `bundleRelease` fails fast unless both release signing and `WEB_CLIENT_ID` are configured.

---

## 📄 License & Intellectual Property

Proprietary. All rights reserved. Built for enterprise and cooperative fund management.
