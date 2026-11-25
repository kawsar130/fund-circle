# 🔍 Fund Circle — Comprehensive Visual Fidelity & UI Design System Audit Report

> **Document Type:** Production Design System & Visual Fidelity Disparity Audit  
> **Audited Platform:** Android Jetpack Compose (Material Design 3, Target SDK 35, Compile SDK 35)  
> **Device Under Test:** Motorola Edge 50 Fusion (`motorola_edge_50_fusion`, Serial: `ZA222PS5GT`, Android 14/15)  
> **Auditor:** Senior QA Lead Engineer & Android Design Systems Architect  
> **Audit Status:** Complete (Read-Only Verification Phase)  
> **SSOT References:**
> - Figma Interactive Prototype: [Fund Circle — Mobile App (Node 42:3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3)
> - Visual Screen Gallery (2x Retina): [`docs/design_preview/gallery.html`](../design_preview/gallery.html) & [`docs/design_preview/screens/`](../design_preview/screens/)
> - Standalone CSS/HTML Prototype: [`docs/design_preview/index.html`](../design_preview/index.html)
> - Design Tokens Spec: [`docs/Design_Tokens.md`](../Design_Tokens.md)
> - Wireframes & Layout Spec: [`docs/Fund_Circle_Wireframes_and_UI_Spec_v1.0.md`](../Fund_Circle_Wireframes_and_UI_Spec_v1.0.md)

---

## 1. 📊 Executive Scorecard & Systemic Drift Summary

### 🎯 Visual Fidelity Score: **68.5% (Grade: C+)**
While the backend data flow, domain use cases, and transactional invariants are strictly adhered to, the live Android client exhibits substantial systemic visual and architectural divergence from the Figma and HTML/CSS design specifications.

```
┌────────────────────────────────────────────────────────────────────────┐
│                      VISUAL FIDELITY SCORECARD                         │
├───────────────────────────────────┬──────────────┬─────────────────────┤
│ Evaluation Axis                   │ Score (100%) │ Primary Status      │
├───────────────────────────────────┼──────────────┼─────────────────────┤
│ 1. Color & Tonal Palette          │    62.0%     │ Major Palette Drift │
│ 2. Typography & Numerical Styling │    70.0%     │ Inconsistent tnum   │
│ 3. Layout, Padding & Anatomy      │    68.0%     │ Missing Bottom Nav  │
│ 4. Badges, Chips & Atoms          │    65.0%     │ Missing Role Emojis │
│ 5. Touch Targets & Ergonomics     │    82.0%     │ Generally Compliant │
├───────────────────────────────────┼──────────────┼─────────────────────┤
│ OVERALL COMPOSITE FIDELITY        │    68.5%     │ Action Required     │
└───────────────────────────────────┴──────────────┴─────────────────────┘
```

### 🚨 Critical Systemic Deficiencies (Global Themes)

1. **Missing 5-Tab Persistent Navigation Shell (`NavigationBar`):**
   - **Spec:** All screens within an active circle (`Dashboard (04)`, `Members (08)`, `Ledger (05)`, `Reports (12)`, `Settings (13)`) must feature a persistent Material 3 `NavigationBar` anchored to the bottom with active indicator pills and badges (`index.html` lines 680–702, wireframes lines 686–694).
   - **Live App:** `FundCircleNavHost.kt` provides zero bottom navigation. Navigation is instead relegated to top app bar icon buttons and inline navigation triggers with nested back stack pops, severely fragmenting screen context and user mental models.

2. **Dark Surface Palette Drift (Forest Black vs Slate Navy):**
   - **Spec:** The primary brand surfaces are deep **Slate Navy** (`#0A101D` base background, `#161F30` elevated card container, `#1E293B` stroke/outline).
   - **Live App:** `Color.kt` defines `BackgroundDark = Color(0xFF111412)` and `SurfaceDark = Color(0xFF111412)` (warm greenish/forest black). Cards lack the distinctive high-contrast financial navy elevation.

3. **Segmented Control Pills vs. M3 Full-Width TabRows:**
   - **Spec:** Filtering tabs (e.g. Screen 05 "All / Deposits / Expenses", Screen 07 "Deposit / Expense Modal Switcher") are designed as self-contained, pill-shaped segmented controls inside a containerized pill background with active sliding chips.
   - **Live App:** Implemented using default Android M3 `PrimaryTabRow` with full-width flat bottom underlines, creating an outdated layout style.

4. **Tabular Numerals (`tnum`) & Uppercase Label Inconsistency:**
   - **Spec:** All monetary values, counters, timestamps, and balance figures must declare `fontFeatureSettings = "tnum"` to prevent horizontal jitter during updates. Section header labels must be rigidly uppercase with tracking (e.g., `OVERALL RECORDED BALANCE`, `TRANSACTION SPECIFICATIONS`).
   - **Live App:** Several amounts omit tabular font features, and card labels use mixed-case or lack letter-spacing.

5. **Atomic Badge & Role Presentation:**
   - **Spec:** Two distinct circle roles: `ADMIN 🛡️` and `STAKEHOLDER 👤` in pill containers (`RoundedCornerShape(9999.dp)`). Status badges feature `LIVE 🟢` with green pulsing dot and `REVERSED ↩` with slate/amber border.
   - **Live App:** Roles show plain text `ADMIN` with no emoji; stakeholder cards omit the role badge entirely. Reversed badge uses a 4dp rectangle in red `errorContainer` rather than a refined pill.

---

## 2. 🔬 Screen-by-Screen Visual & Disparity Breakdown

---

### Screen 00: Design Tokens, Color Palette & Atomic Components
- **Source Files:** [`Color.kt`](../../app/src/main/java/com/fundcircle/app/presentation/theme/Color.kt), [`Theme.kt`](../../app/src/main/java/com/fundcircle/app/presentation/theme/Theme.kt), [`Type.kt`](../../app/src/main/java/com/fundcircle/app/presentation/theme/Type.kt), [`Shape.kt`](../../app/src/main/java/com/fundcircle/app/presentation/theme/Shape.kt), [`DesignTokens.kt`](../../app/src/main/java/com/fundcircle/app/presentation/theme/DesignTokens.kt)
- **Reference Spec:** `docs/design_preview/screens/00_Design_Tokens_and_Atoms.png`, `docs/Design_Tokens.md`

| Severity | Token / Component | Expected Specification | Actual Implementation |
| :--- | :--- | :--- | :--- |
| **Critical** | Dark Background & Surface | Slate Navy (`#0A101D` base, `#161F30` card container, `#1A2436` high) | `Color(0xFF111412)` (warm greenish olive-black) |
| **Major** | Surface Container Low/High | `#101726` (low), `#1E2A40` (high), `#263550` (highest) | M3 default tonal elevations derived from forest primary |
| **Major** | Typography Tracking | Section headers: `0.08em` letter spacing, uppercase | Default bodyMedium tracking (0em), inconsistent casing |
| **Major** | Status Badges | `LIVE 🟢` (pill with green dot), `ACTIVE` (emerald pill), `REVERSED ↩` (slate pill) | Hardcoded individual text badges without unified atom composables |
| **Minor** | Currency Symbol Spacing | Standardized non-breaking space: `৳ 250,000`, `$ 1,200` | Inconsistent: `+ ৳ 250,009` vs `+৳ 100,000` |

---

### Screen 01: Welcome & Sign-In (`auth_welcome`)
- **Composable:** [`AuthScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/auth/AuthScreen.kt)
- **Expected Spec:** `01_Sign_In_auth_welcome.png` (Figma Node `42:3` Flow 1)
- **Live Device Capture:** `/tmp/device_screen_01.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Brand Logo Glyph | 64dp rounded square container (`#006D4D`) with bold white interconnected circular monogram (`FC`) | Standard circle container with plain text `FC` font |
| **Major** | Hero Typography | Display medium: "Collective Capital, Pooled in Total Trust" with emerald highlight | Text renders in default onSurface with single uniform styling |
| **Minor** | Feature Bullet Cards | 3 elevation cards with emoji icons (`🔒 Bank-Grade Isolation`, `⚡ O(1) Atomic Reads`, `📋 Immutable Audit Trail`) | Plain vertical bullet rows without distinct elevated card containers |
| **Minor** | Google Sign-In Button | White container with official multi-colored Google 'G' asset, height 52dp, radius 12dp | Standard M3 OutlinedButton, text alignment slightly offset |

---

### Screen 02: Circle Hub (`circle_hub`)
- **Composable:** [`CircleHubScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/hub/CircleHubScreen.kt)
- **Expected Spec:** `02_Circle_Hub_circle_hub.png`
- **Live Device Capture:** `/tmp/device_screen_02.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Section Headers | Uppercase subheaders: `CIRCLES I MANAGE (X/5)` and `CIRCLES I JOINED (Y)` with counter badges | Plain text headers without dedicated styling or badge pill for counts |
| **Major** | Circle Card Layout | Left: Circle Title, Meta (`2 Members • BDT (৳)`), Role Pill (`ADMIN 🛡️`). Right: Large Balance (`৳ 250,009`) in tabular green | Role pill positioned at top-right; balance stacked vertically below with "Balance:" prefix label |
| **Major** | Empty State / Create CTA | Dashed outline button: `+ Create New Fund Circle` at bottom of list | Replaced with floating action button (FAB) at bottom-right |
| **Minor** | User Profile Header | Right-aligned circular avatar with active user initials, click to self-service profile | Avatar shows text initial without outline ring |

---

### Screen 03: Create Circle Wizard (`circle_create`)
- **Composable:** [`CreateCircleScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/create/CreateCircleScreen.kt)
- **Expected Spec:** `03_Create_Wizard_circle_create.png`
- **Live Device Capture:** Code & Preview inspection

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Top App Bar Action | Back navigation arrow (`←`) with "New Fund Circle" title | Close icon button (`✕`) |
| **Major** | Currency Selector | Styled selector box with icon `💱` and full names: `BDT - Bangladeshi Taka (৳)` | Three horizontal `FilterChip` components (`৳ BDT`, `$ USD`, `€ EUR`) |
| **Major** | Form Input Leading Icons | Input fields feature semantic leading icons (`🏛️` for Circle Name, `📝` for Purpose) | Standard M3 OutlinedTextField with no leading icons |
| **Major** | Permanence Notice Card | High-contrast amber/tertiary container with `⚠️` icon and specific warning text | M3 Card with lock icon and generic warning text |
| **Minor** | Submit Button Label | Full-width bottom button: `Create & Enter Circle` | Button reads `Create Fund Circle` |

---

### Screen 04: Circle Home Dashboard (`dashboard_home`)
- **Composable:** [`DashboardScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/dashboard/DashboardScreen.kt)
- **Expected Spec:** `04_Circle_Dashboard_dashboard_home.png`
- **Live Device Capture:** `/tmp/device_screen_04.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Critical** | Bottom Navigation Bar | Persistent 5-tab M3 `NavigationBar` anchored at bottom | **Completely Missing** (navigated via top bar icon buttons) |
| **Major** | Overall Balance Card | Emerald-accented gradient card (`#006D4D` &rarr; `#004D36`), `LIVE 🟢` status badge top right, massive tabular balance | Flat dark surface card; plain text `LIVE` badge; missing gradient depth |
| **Major** | Aggregate Statistics Grid | 2-column cards: `Total Deposits (+ ৳ ...)` in emerald, `Total Expenses (- ৳ ...)` in rose/coral | Cards use basic surface colors with minimal contrast between deposit/expense metrics |
| **Major** | Feed Transaction Items | Clean rows: Left title & subtitle; Right formatted amount (`+৳ 250,009`) & date | Live item injects an extraneous 36dp circular icon `+` / `-` shifting text leftward |
| **Minor** | Top App Bar Actions | Context subtitle `Active Circle`, title `Apex Capital`, Status pill `ACTIVE` | Top bar cluttered with 4 small action icons (Notifications, Search, Reports, Settings) |

---

### Screen 05: Transactions Ledger (`ledger_transactions`)
- **Composable:** [`LedgerScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/ledger/LedgerScreen.kt)
- **Expected Spec:** `05_Transactions_Ledger_ledger_transactions.png`
- **Live Device Capture:** `/tmp/device_screen_05.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Critical** | Bottom Navigation Bar | Persistent 5-tab M3 `NavigationBar` anchored at bottom | **Completely Missing** |
| **Major** | Segmented Filter Tabs | Pill container with 3 segmented chips: `All (2)`, `Deposits (1)`, `Expenses (1)` | Default M3 `PrimaryTabRow` with full-width underline indicator |
| **Major** | Transaction Card Anatomy | Left: Category/Member info & payment method; Right: Formatted tabular amount + time | Extraneous 36dp circular icon; amount lacks tabular spacing |
| **Major** | Reversed Item Presentation | Strikethrough text, muted color, distinct `REVERSED ↩` pill badge with amber outline | Red `errorContainer` rectangular chip (4dp radius) |
| **Minor** | FAB Action | `+ Record Transaction` extended FAB or speed-dial | Extended FAB present but styling deviates from standard emerald container |

---

### Screen 06: Transaction Detail & Audit Disclosure Card
- **Composable:** [`TransactionDetailScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/ledger/TransactionDetailScreen.kt)
- **Expected Spec:** `06_Transaction_Detail_Audit_Card.png`
- **Live Device Capture:** `/tmp/device_screen_06.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Transaction Reference Code | Formatted short reference: `TX-DEP-9821` with copy button | Raw 36-character UUID (`9728062f-9358-482b-b747-...`) wrapping awkwardly |
| **Major** | Transparency Audit Card | Permanent expandable educational card: "Transparency Audit Trail — Powered by Cloud Firestore O(1) Atomic Aggregates" | Rendered only conditionally when transaction is voided |
| **Major** | Reversal Action Button | Red destructive tonal button: `Reverse Transaction (Admin Only)` with warning icon | Plain text button inside card |
| **Minor** | Metadata Row Anatomy | 2-column key-value grid with muted labels and bold tabular values | Single vertical column list |

---

### Screen 07: Record Transaction Modal Sheet (`modal_add_tx`)
- **Composable:** [`RecordTransactionSheet.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/ledger/RecordTransactionSheet.kt)
- **Expected Spec:** `07_Add_Transaction_Modal_modal_add_tx.png`
- **Live Device Capture:** `/tmp/device_screen_07.png` & `/tmp/device_screen_07_expense.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Type Selector Tabs | Pill container segmented control: `Deposit` vs `Expense` with active pill color shift | M3 `TabRow` with sliding underline indicator |
| **Major** | Expense Submit Button Color | Destructive / Coral Red container (`#BA1A1A` / `#FF897D`) for Expense submissions | Remains green `Primary` (`#006D4D`) regardless of transaction type |
| **Major** | Monetary Input Hygiene | Large tabular number display with fixed currency prefix (`৳`, `$`, `€`), integer-only | Standard OutlinedTextField with generic keyboard |
| **Minor** | Member Selector Dropdown | Shows member name + stakeholder code `Kawsar Ahmed (FC-001)` | Standard dropdown without formatted code chip |

---

### Screen 08: Stakeholders Roster (`members_roster`)
- **Composable:** [`MembersRosterScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/members/MembersRosterScreen.kt)
- **Expected Spec:** `08_Stakeholders_Roster_members_roster.png`
- **Live Device Capture:** `/tmp/device_screen_08.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Critical** | Bottom Navigation Bar | Persistent 5-tab M3 `NavigationBar` anchored at bottom | **Completely Missing** |
| **Major** | Role Badges | Explicit badge for every member: `ADMIN 🛡️` (purple/indigo) or `STAKEHOLDER 👤` (teal/slate) | Text `ADMIN` only; stakeholder members have **no badge** |
| **Major** | Cumulative Net Balance | Right-aligned tabular green figure: `৳ 250,009 Deposited` | Stacked text without bold tabular hierarchy |
| **Minor** | Stakeholder Avatar Monogram | Two-letter initials on tonal circular background | Single letter initial |

---

### Screen 09: Member Detail & Individual Ledger (`member_detail`)
- **Composable:** [`MemberDetailScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/members/MemberDetailScreen.kt)
- **Expected Spec:** `09_Member_Detail_Ledger_member_detail.png`
- **Live Device Capture:** `/tmp/device_screen_09.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Hero Profile Card | Large avatar, Full Name, Role Pill (`ADMIN 🛡️`), Stakeholder Code (`FC-001`), Join Date | Basic card with unstyled text and missing role emoji badge |
| **Major** | KYC & Nominee Card | Dedicated section: Phone, Email, Nominee Name, Nominee Relationship, Emergency Contact | Data rendered as flat text rows without M3 container styling |
| **Minor** | Member Transaction History | Filtered ledger list showing only this member's deposits and attributed expenses | List renders without dedicated empty-state or transaction count badge |

---

### Screen 10: Add Stakeholder Modal (`modal_add_member`)
- **Composable:** [`AddStakeholderSheet.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/members/AddStakeholderSheet.kt)
- **Expected Spec:** `10_Add_Stakeholder_Modal_modal_add_member.png`
- **Live Device Capture:** `/tmp/device_screen_10.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Role Assignment Card | Visual card confirming: "Assigned Circle Role: Stakeholder 👤 (View-only ledger & personal deposits)" | Missing visual role confirmation card |
| **Major** | Input Leading Emojis | Fields feature icons: `👤 Full Name`, `✉️ Email`, `📱 Phone`, `🛡️ Nominee` | Standard text fields without leading icon assets |
| **Minor** | Stakeholder Code Preview | Auto-generated ID preview chip: `Assigned ID: FC-002` | Not displayed in the modal |

---

### Screen 11: Edit My Profile / Self-Service KYC
- **Composable:** [`ProfileEditScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/profile/ProfileEditScreen.kt)
- **Expected Spec:** `11_Edit_My_Profile_Self_Service.png`
- **Live Device Capture:** `/tmp/device_screen_11.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Phone Number Input | Dedicated contact phone field under Personal Information | Missing contact phone field in profile form |
| **Major** | Relationship Selector | Structured dropdown with predefined options (`Spouse`, `Parent`, `Sibling`, `Child`, `Business Partner`) | Free-text input field |
| **Minor** | Verification Status Pill | Top header badge: `VERIFIED STAKEHOLDER 🟢` | Simple text badge without status indicator |

---

### Screen 12: Reports & CSV Export Engine (`reports_export`)
- **Composable:** [`ReportsScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/reports/ReportsScreen.kt)
- **Expected Spec:** `12_Reports_and_CSV_Export_reports_export.png`
- **Live Device Capture:** `/tmp/device_screen_12.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Critical** | Bottom Navigation Bar | Persistent 5-tab M3 `NavigationBar` anchored at bottom | **Completely Missing** |
| **Major** | Date Range Quick Chips | Horizontal chip group: `All Time`, `This Month`, `Last 30 Days`, `Custom Range` | Only custom start/end date fields are present |
| **Major** | Audit Trail Verification Card | Card explaining: "100% On-Device Kotlin CSV Generator — Zero Cloud Parsing Fees" | Simplified banner without full technical disclosure |
| **Minor** | Export Action Button | Prominent emerald button: `Generate & Share Ledger CSV 📄` | Standard Outlined/Filled button |

---

### Screen 13: Settings & Governance (`settings_admin`)
- **Composable:** [`SettingsScreen.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/settings/SettingsScreen.kt)
- **Expected Spec:** `13_Settings_and_Governance_settings_admin.png`
- **Live Device Capture:** `/tmp/device_screen_13.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Critical** | Bottom Navigation Bar | Persistent 5-tab M3 `NavigationBar` anchored at bottom | **Completely Missing** |
| **Major** | Admin Governance Card | Red-accented container for Admin actions: "Transfer Admin Ownership" & "Archive Circle" | Styled as standard list items without danger container demarcation |
| **Major** | Offline Disk Cache Status | Tile showing: "100 MB Persistent Disk Cache" with `ACTIVE 🟢` status badge | Present but lacks proper tabular badge layout |
| **Minor** | Currency Lock Indicator | Row: "Base Currency: BDT (৳) 🔒 (Permanent)" | Text displayed without dedicated badge container |

---

### Screen 14: Reverse Confirmation Dialog (`dialog_reverse_tx`)
- **Composable:** [`ReverseTransactionDialog.kt`](../../app/src/main/java/com/fundcircle/app/presentation/screens/ledger/ReverseTransactionDialog.kt)
- **Expected Spec:** `07b_Reverse_Confirmation_Dialog_dialog_reverse_tx.png`
- **Live Device Capture:** `/tmp/device_screen_14.png`

| Severity | Element Name | Expected Specification | Observed Device Implementation |
| :--- | :--- | :--- | :--- |
| **Major** | Dialog Header Icon | Red alert triangle `⚠️` or reversal arrow `↩` in circular errorContainer | Standard text-only dialog title |
| **Major** | Original Transaction Summary | Distinct nested card showing: `Amount: - ৳ 5,000`, `Category: Office Stationery`, `Recorded By: ...` | Text summary lacking dedicated containerized preview |
| **Major** | Audit Warning Notice | Explanatory note: "Reversal is permanent. A reversal record will be posted to the ledger and aggregates atomically adjusted." | Text present but lacks prominent warning styling |
| **Minor** | Confirm Button Styling | High-emphasis destructive button: `Confirm Reversal` in red | Standard M3 TextButton / Button |

---

## 3. 🧩 Systemic Design Token & Component Gaps

To resolve disparities across all 14 screens efficiently, the following global design system components and tokens must be updated:

### 1. Slate Navy Theme Colors (`Color.kt` & `Theme.kt`)
Replace olive/forest dark backgrounds with authentic Slate Navy design tokens:
```kotlin
// Target Slate Navy Foundations
val SlateNavyDarkBase       = Color(0xFF0A101D) // Main Window Background
val SlateNavyDarkSurface    = Color(0xFF101726) // Surface
val SlateNavyDarkContainer  = Color(0xFF161F30) // Elevated Card Container
val SlateNavyDarkHigh       = Color(0xFF1E2A40) // High-Emphasis Container
val SlateNavyDarkOutline    = Color(0xFF263550) // Card Border Stroke
val EmeraldPrimary          = Color(0xFF006D4D) // Emerald Brand Primary
val CoralExpense            = Color(0xFFFF5449) // Destructive Expense
val AmberWarning            = Color(0xFFFFB951) // Audit / Warning Token
```

### 2. Standardized Atomic Badges (`AtomicBadges.kt`)
Create unified composable atoms replacing hardcoded ad-hoc chips:
- `RoleBadge(role: CircleRole)` &rarr; `ADMIN 🛡️` / `STAKEHOLDER 👤`
- `StatusBadge(status: CircleStatus)` &rarr; `LIVE 🟢` / `ACTIVE`
- `ReversedBadge()` &rarr; `REVERSED ↩` (pill shape, border outline, tnum font)
- `CurrencyLockBadge(currency: Currency)` &rarr; `BDT (৳) 🔒`

### 3. Containerized Segmented Pill Control (`SegmentedControl.kt`)
Build a reusable segmented pill composable for tab filtering:
- Outer pill container: `RoundedCornerShape(9999.dp)` with background `surfaceContainerHighest`
- Inner animated selected pill: `RoundedCornerShape(9999.dp)` with `primaryContainer` or `surface`
- Tabular digit count badges: `(12)`

### 4. Tabular Digits Formatting Helper
Enforce `FontFeatureSettings = "tnum"` globally across all monetary composables:
```kotlin
val TabularNumStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontFeatureSettings = "tnum"
)
```

---

## 4. 🚀 Prioritized, Step-by-Step Remediation Plan

The remediation roadmap is split into 5 distinct, independently testable phases:

### 📍 Phase 1: Global Design Tokens, Slate Navy Surfaces & Atomic Badges
- [ ] Update `Color.kt` & `Theme.kt` with Slate Navy tokens (`#0A101D`, `#161F30`, `#1E2A40`).
- [ ] Create `AtomicBadges.kt` (`RoleBadge`, `LiveBadge`, `ReversedBadge`, `CurrencyLockBadge`).
- [ ] Implement `SegmentedControl.kt` (reusable pill switcher).
- [ ] Verify Material 3 color roles and dynamic color disabled flag (`dynamicColor = false`).

### 📍 Phase 2: Navigation Shell Architecture (5-Tab Persistent NavigationBar)
- [ ] Refactor `FundCircleNavHost.kt` and `MainActivity.kt` to introduce a persistent `Scaffold` shell with `NavigationBar` for active circle screens.
- [ ] Define the 5 bottom navigation tabs:
  1. `Home` &rarr; `DashboardScreen` (Screen 04)
  2. `Members` &rarr; `MembersRosterScreen` (Screen 08)
  3. `Ledger` &rarr; `LedgerScreen` (Screen 05)
  4. `Reports` &rarr; `ReportsScreen` (Screen 12)
  5. `Settings` &rarr; `SettingsScreen` (Screen 13)
- [ ] Clean up redundant top app bar navigation icons on `DashboardScreen.kt`.

### 📍 Phase 3: Core Financial Surfaces (Dashboard 04 & Ledger 05)
- [ ] **DashboardScreen.kt:** Implement emerald gradient hero balance card, tabular digits, and clean feed item anatomy (remove extraneous 36dp plus/minus circle icons).
- [ ] **LedgerScreen.kt:** Replace `PrimaryTabRow` with `SegmentedControl` (`All`, `Deposits`, `Expenses`). Update `ReversedBadge` to pill style.

### 📍 Phase 4: Recording, Reversals & Details (Screens 06, 07, 14)
- [ ] **RecordTransactionSheet.kt:** Containerized segmented pill for Deposit vs Expense; dynamic submit button color (Emerald for deposit, Coral Red for expense).
- [ ] **TransactionDetailScreen.kt:** Replace raw UUID with formatted short reference (`TX-DEP-XXXX`); render permanent educational audit card.
- [ ] **ReverseTransactionDialog.kt:** Add alert header icon, transaction summary container, and prominent confirmation styling.

### 📍 Phase 5: Stakeholder Roster, KYC & Administration (Screens 01, 02, 03, 08, 09, 10, 11)
- [ ] **AuthScreen.kt & CircleHubScreen.kt:** Standardize brand logo monogram glyph, uppercase section headers, and circle card layout.
- [ ] **MembersRosterScreen.kt & MemberDetailScreen.kt:** Add mandatory `STAKEHOLDER 👤` and `ADMIN 🛡️` role badges; KYC info layout cards.
- [ ] **AddStakeholderSheet.kt & ProfileEditScreen.kt:** Add leading emoji icons, phone number KYC field, and relationship selector dropdown.
- [ ] **ReportsScreen.kt & SettingsScreen.kt:** Quick date filter chips, on-device engine disclosure card, and governance danger card styling.

---
*Report certified by Senior QA Lead Engineer & Android Design Systems Architect.*  
*Ready for Phase 1 remediation upon approval.*
