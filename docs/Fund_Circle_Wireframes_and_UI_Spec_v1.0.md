# Fund Circle — Wireframes & UI Specification

**Document version:** 1.2 (Complete 14-Screen Commercial Play Store UI Specification with Live Figma Integration)  
**Platform:** Android 8.0+ (Min SDK 26, Target SDK 35, Compile SDK 35), Jetpack Compose, Material 3  
**Design language:** Google Material 3 (Expressive & Financial Clarity)  
**Status:** Approved Engineering & Design Blueprint  
**Live Figma Assets:**
- **Figma Interactive Prototype:** [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=42-3&autoplay-code-preview=42-3) (Unified 14-screen interactive code prototype)
- **Visual Screen Gallery (2x Retina):** [docs/design_preview/gallery.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/gallery.html)
- **Master Flowchart (FigJam):** [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)
- **Local Design Prototype:** [docs/design_preview/index.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/index.html)  

---

## 1. Executive design system & tokens

This document provides definitive structural wireframes, layout grids, component anatomies, and Material 3 design tokens for **Fund Circle**. It serves as the direct implementation contract for Android UI engineers building with Jetpack Compose and for high-fidelity prototyping in Figma.

### 1.1 Material 3 color tokens (Financial Trust Theme)

The palette balances financial growth (Emerald Green) with corporate governance (Deep Slate Navy) and attention accents (Warm Amber Gold).

| Token Name | Light Theme Hex | Dark Theme Hex | Semantic Usage |
|---|---|---|---|
| `md_theme_primary` | `#006D4D` | `#00DF9A` | Brand accent, primary buttons, positive balance hero, active tab indicator |
| `md_theme_onPrimary` | `#FFFFFF` | `#003825` | Text/icon on primary color |
| `md_theme_primaryContainer` | `#74F8BD` | `#005238` | Tonal cards, deposit badges, search bar highlight |
| `md_theme_onPrimaryContainer` | `#002114` | `#74F8BD` | Text on primary container |
| `md_theme_secondary` | `#1B365D` | `#8EAFCE` | Admin badges, secondary chips, filter toggles |
| `md_theme_tertiary` | `#7D5700` | `#FABC3C` | Pending invitation alerts, currency symbols, correction indicators |
| `md_theme_error` | `#BA1A1A` | `#FFB4AB` | Negative balance, reversed transactions, leave/deactivate dialogs |
| `md_theme_background` | `#FCFDF7` | `#111412` | Root window background |
| `md_theme_surface` | `#FCFDF7` | `#111412` | Top app bars, bottom navigation bar |
| `md_theme_surfaceContainerLow` | `#F6F7F1` | `#191D1A` | Screen base background, ledger backgrounds |
| `md_theme_surfaceContainer` | `#F0F1EC` | `#1D211E` | Standard list items, neutral card backgrounds |
| `md_theme_surfaceContainerHigh` | `#EAECE6` | `#272B28` | Elevated dialogs, bottom sheets, filter popovers |
| `md_theme_outline` | `#707973` | `#8A938C` | Card borders, text field outlines, dividers |

### 1.2 Typography scale (Inter / Roboto with Tabular Digits)

All financial numbers and currency amounts strictly use **Tabular Digits** (`FontFeatureSettings = "tnum"`) to prevent layout jitter during real-time updates.

| Style Name | Font Size | Line Height | Weight | Usage |
|---|---|---|---|---|
| `HeadlineLarge` | 32sp | 40sp | Bold (700, tnum) | Hero Overall Recorded Balance |
| `HeadlineMedium` | 28sp | 36sp | Semi-Bold (600, tnum) | Dashboard summary card balances |
| `TitleLarge` | 22sp | 28sp | Semi-Bold (600) | Top app bar titles, screen header titles |
| `TitleMedium` | 16sp | 24sp | Medium (500) | Card headings, member names, report titles |
| `BodyLarge` | 16sp | 24sp | Regular (400) | Form input text, narrative descriptions |
| `BodyMedium` | 14sp | 20sp | Regular (400) | List item subtitles, timestamp text |
| `LabelLarge` | 14sp | 20sp | Semi-Bold (600, tnum) | Button labels, ledger transaction amounts |
| `LabelSmall` | 11sp | 16sp | Medium (500) | Status chips (`ACTIVE`, `INACTIVE`), Member IDs |

### 1.3 Grid, spacing, and safe areas

- **Baseline Grid:** 8dp modular grid; 4dp for compact internal chip paddings.
- **Screen Margins:** Horizontal 16dp; top/bottom content padding 16dp.
- **Card Spacing:** 12dp vertical spacing between list cards.
- **Corner Radius:**
  - Small (Chips, text fields): 8dp
  - Medium (Cards, dialogs): 16dp
  - Large (Bottom sheets, hero container): 24dp
  - Full (Circular avatars, pill badges): 100dp
- **Touch Target Floor:** Minimum **48dp × 48dp** for all interactive targets.
- **Edge-to-Edge:** Full compliance with Android `WindowInsets.systemBars` (transparent status and navigation bars).

---

## 2. Screen inventory and layout packages

```
                       ┌──────────────────────────────────────────────┐
                       │   Fund Circle — Screen Architecture (14)     │
                       └──────────────────────┬───────────────────────┘
                                              │
         ┌───────────────────┬────────────────┼───────────────────┬───────────────────┐
         │                   │                │                   │                   │
    [Package A]         [Package B]      [Package C]         [Package D]         [Package E]
    Platform & Hub      Circle Home      Transactions        Membership          Reports & Settings
    - Screen 01: Login  - Screen 04: Home- Screen 05: Ledger - Screen 08: Roster - Screen 12: Reports
    - Screen 02: Hub                     - Screen 06: Details- Screen 09: Details- Screen 13: Settings
    - Screen 03: Create                  - Screen 07: Add Txn- Screen 10: Add Mem- Screen 14: Reverse Txn
                                                             - Screen 11: Profile
```

---

## Package A: Authentication & Workspace Hub

### Screen 01: Login Screen (Branded One-Tap Hero)
*   **Purpose:** Frictionless Google One-Tap authentication.
*   **User Roles:** All users.
*   **Navigation:** Successful authentication $\rightarrow$ Screen 02 (Hub) or Screen 04 (Smart Launch).

```text
┌────────────────────────────────────────────────────────┐
│ [status bar: 100% transparent edge-to-edge]            │
│                                                        │
│                                                        │
│                    ┌──────────────┐                    │
│                    │  [FC LOGO]   │                    │ <-- 96x96dp Circular Icon
│                    │   (Emerald)  │                    │     (Circle with interlocking rings)
│                    └──────────────┘                    │
│                                                        │
│                      Fund Circle                       │ <-- HeadlineLarge (Bold)
│            Transparent Capital Management              │ <-- BodyMedium (Outline color)
│                                                        │
│                                                        │
│   ┌────────────────────────────────────────────────┐   │
│   │  • Multi-tenant investment syndicates          │   │ <-- Feature Highlights Card
│   │  • Immutable, versioned financial audit trail  │   │     (SurfaceContainerLow, 16dp padding)
│   │  • Real-time shared deposits and expenses      │   │
│   │  • Instant on-device CSV ledger export         │   │
│   └────────────────────────────────────────────────┘   │
│                                                        │
│                                                        │
│   ┌────────────────────────────────────────────────┐   │
│   │  [G]  Continue with Google                     │   │ <-- M3 Elevated Button (Primary)
│   └────────────────────────────────────────────────┘   │     Height: 56dp, Full Width
│                                                        │
│         By continuing, you agree to the Terms          │ <-- LabelSmall (Muted)
│                                                        │
│ [navigation bar: home pill inset]                      │
└────────────────────────────────────────────────────────┘
```

---

### Screen 02: My Fund Circles Hub (Workspace Switcher)
*   **Purpose:** Central hub to manage owned circles, view joined circles, accept/decline invites, or create new circles.
*   **User Roles:** All authenticated users.
*   **Navigation:** Tap Circle Card $\rightarrow$ Screen 04 (Circle Home); Tap "+ Create" $\rightarrow$ Screen 03.

```text
┌────────────────────────────────────────────────────────┐
│ My Fund Circles                                [Avatar]│ <-- TopAppBar (TitleLarge)
├────────────────────────────────────────────────────────┤
│ [!] PENDING INVITATIONS (1)                            │ <-- Tertiary Container Header
│ ┌────────────────────────────────────────────────────┐ │
│ │ Apex Syndicate                                     │ │ <-- Card (SurfaceContainerHigh)
│ │ Invited by: karim.admin@gmail.com                  │ │
│ │ ┌──────────────────────┐  ┌──────────────────────┐ │ │
│ │ │ [X] Decline          │  │ [✓] Accept           │ │ │ <-- 48dp Action Buttons
│ │ └──────────────────────┘  └──────────────────────┘ │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ CIRCLES I MANAGE (2/5)                                 │ <-- LabelMedium (Outline color)
│ ┌────────────────────────────────────────────────────┐ │
│ │ Green Valley Property Pool           [Admin Badge] │ │ <-- Card (SurfaceContainer)
│ │ Currency: ৳ BDT • 14 Members • Balance: ৳1,450,000 │ │
│ └────────────────────────────────────────────────────┘ │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Angel Syndicate 2026                 [Admin Badge] │ │
│ │ Currency: $ USD • 6 Members • Balance: $85,000     │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ CIRCLES I JOINED (1)                                   │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Batch '14 Partnership Fund          [Member Badge] │ │
│ │ Currency: ৳ BDT • Joined as: FC-005                │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│                                                        │
│                                   ┌────────────────┐   │
│                                   │ [+ New Circle] │   │ <-- Extended FAB (Primary)
│                                   └────────────────┘   │
└────────────────────────────────────────────────────────┘
```

---

### Screen 03: Create Fund Circle (Modal Bottom Sheet)
*   **Purpose:** Setup a new circle, choose currency, and enforce the 5-circle limit.
*   **User Roles:** All users (`createdCircleCount < 5`).
*   **Navigation:** Success $\rightarrow$ Screen 04 (Direct entry into new circle).

```text
┌────────────────────────────────────────────────────────┐
│                        [Drag Handle]                   │ <-- 32x4dp Pill
│                                                        │
│ Create New Fund Circle                        [X Close]│ <-- TitleLarge
│ Set up an isolated ledger for your investment group.   │ <-- BodyMedium
│                                                        │
│ Circle Name *                                          │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Green Valley Property Pool                         │ │ <-- OutlinedTextField
│ └────────────────────────────────────────────────────┘ │     Character counter: 26/50
│                                                        │
│ Primary Currency * (Locked after first transaction)    │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [৳] BDT — Bangladeshi Taka                      [▼]│ │ <-- ExposedDropdownMenu
│ └────────────────────────────────────────────────────┘ │     Locked to: BDT (৳), USD ($), EUR (€)
│                                                        │
│ Description / Purpose (Optional)                       │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Joint land acquisition and development pool.       │ │ <-- OutlinedTextField (Multi-line)
│ │                                                    │ │     Character counter: 44/200
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Create Fund Circle                                 │ │ <-- FilledButton (Primary)
│ └────────────────────────────────────────────────────┘ │     Height: 56dp
│                                                        │
└────────────────────────────────────────────────────────┘
```

---

## Package B: Circle Home & Financial Ledgers

### Screen 04: Circle Home Dashboard
*   **Purpose:** Central financial command center showing real-time balance, totals, and recent 10 transactions.
*   **User Roles:** Admin and Stakeholders.
*   **Design Note:** Top app bar remains completely clean (no Switch Circle icon).

```text
┌────────────────────────────────────────────────────────┐
│ Green Valley Property Pool             [Status: ACTIVE]│ <-- TopAppBar (Clean Title)
├────────────────────────────────────────────────────────┤
│ OVERALL RECORDED BALANCE                               │
│ ৳ 1,450,000                                            │ <-- HeadlineLarge (32sp Bold, Tabular)
│ ────────────────────────────────────────────────────── │
│ ┌─────────────────────────┬──────────────────────────┐ │
│ │ Total Deposits          │ Total Expenses           │ │ <-- Hero Card (PrimaryContainer)
│ │ ৳ 1,600,000             │ ৳ 150,000                │ │     Loaded in 1 read (O(1))
│ └─────────────────────────┴──────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ Recent Transactions (Top 10)               [View All]  │ <-- Section Header
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [DEP] Rahim Uddin (FC-002)             + ৳ 100,000 │ │ <-- Transaction Card
│ │ 12 Nov 2026 • Bank Transfer                        │ │     Green '+' indicator
│ ├────────────────────────────────────────────────────┤ │
│ │ [EXP] Office & Administration           - ৳ 12,000 │ │
│ │ 10 Nov 2026 • Stationery & Printing    [CORRECTED] │ │ <-- Amber 'Corrected' Chip
│ ├────────────────────────────────────────────────────┤ │
│ │ [DEP] Tanvir Ahmed (FC-003)             + ৳ 50,000 │ │
│ │ 08 Nov 2026 • Mobile Banking                       │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│                                    ┌───────────────┐   │
│                                    │  [+] Add Txn  │   │ <-- Admin FAB (Hidden for Stakeholder)
│                                    └───────────────┘   │
├────────────────────────────────────────────────────────┤
│  [Home]   [Members]   [Transactions]   [Reports]   [*] │ <-- M3 NavigationBar
└────────────────────────────────────────────────────────┘     ('*' = Settings tab with Switch)
```

---

### Screen 05: Transactions Ledger Screen (Segmented Tabs)
*   **Purpose:** Searchable, filterable ledger of all deposits and expenses.
*   **User Roles:** Admin and Stakeholders.
*   **Navigation:** Tap item $\rightarrow$ Screen 06 (Transaction Details & Version History).

```text
┌────────────────────────────────────────────────────────┐
│ Transactions Ledger                                [Q] │ <-- TopAppBar with Search icon
├────────────────────────────────────────────────────────┤
│ ┌──────────────────────────┬─────────────────────────┐ │
│ │    Deposits (42)         │     Expenses (18)       │ │ <-- M3 PrimaryTabRow (Segmented)
│ └──────────────────────────┴─────────────────────────┘ │
│                                                        │
│ [Filter: All Dates ▼]  [Method: All ▼]  [Sort: Newest] │ <-- Filter Chips Horizontal Row
├────────────────────────────────────────────────────────┤
│ NOVEMBER 2026                                          │ <-- Sticky Date Header
│ ┌────────────────────────────────────────────────────┐ │
│ │ Rahim Uddin (FC-002)                   + ৳ 100,000 │ │ <-- Card (SurfaceContainer)
│ │ 12 Nov 2026 • Bank Transfer                        │ │
│ │ Note: Monthly contribution tranche 1               │ │
│ ├────────────────────────────────────────────────────┤ │
│ │ Kawsar Ahmed (FC-001)                  + ৳ 100,000 │ │
│ │ 10 Nov 2026 • Cash                                 │ │
│ ├────────────────────────────────────────────────────┤ │
│ │ Sajjad Hossain (FC-004)              ~+ ৳ 50,000~  │ │ <-- Strikethrough Original Amount
│ │ 05 Nov 2026 • Cash                    [REVERSED ↩]  │ │ <-- Outline Badge (isVoid=true)
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ OCTOBER 2026                                           │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Tanvir Ahmed (FC-003)                   + ৳ 50,000 │ │
│ └────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│  [Home]   [Members]   [Transactions]   [Reports]   [*] │
└────────────────────────────────────────────────────────┘
```

---

### Screen 06: Transaction Details & Audit Disclosure Card
*   **Purpose:** Complete transparency for a single transaction, showing immutable values, reversal status, and audit disclosures.
*   **User Roles:** Both roles (Admin has 'Reverse Transaction' button if active).

```text
┌────────────────────────────────────────────────────────┐
│ [<- Back] Transaction Details                  [Admin] │ <-- TopAppBar
├────────────────────────────────────────────────────────┤
│ TRANSACTION RECORD                                     │
│ Deposit Amount: ৳ 100,000                              │ <-- HeadlineMedium (tnum)
│ Contributor:    Rahim Uddin (FC-002)                   │
│ Date:           12 November 2026                       │
│ Payment Method: Bank Transfer                          │
│ Note:           Monthly contribution tranche 1         │
│ Status:         Posted • Active                        │
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [↩] Reverse Transaction (Admin Only)                │ │ <-- Admin Only Action Button (Opens Screen 14)
│ └────────────────────────────────────────────────────┘ │     (Disabled / hidden if already reversed)
├────────────────────────────────────────────────────────┤
│ [🛡️] TRANSPARENCY AUDIT NOTE                          │ <-- Visible only if isVoid == true
│ ┌────────────────────────────────────────────────────┐ │
│ │ REVERSED ON: 13 Nov 2026, 14:32                     │ │ <-- Immutable Reversal Audit Card
│ │ Reversed by: kawsar@example.com (Admin)            │ │
│ │ Reason: "Duplicate bank payment voucher entry."     │ │
│ │ Impact: Balance atomically reversed by -৳ 100,000  │ │
│ └────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

---

### Screen 07: Add Transaction Modal (Admin — Deposit & Expense Tabs)
*   **Purpose:** Modal bottom sheet for the Admin to record a new deposit or company expense.
*   **User Roles:** Admin only.
*   **Validation:** 5 fields, whole integers $\ge 1$, Date $\le$ Today, 8 predefined expense categories.

```text
┌────────────────────────────────────────────────────────┐
│                        [Drag Handle]                   │
│                                                        │
│ Record Financial Transaction                  [X Close]│ <-- TitleLarge
│ ┌──────────────────────────┬─────────────────────────┐ │
│ │    [✓] Deposit (Member)  │         Expense         │ │ <-- Segmented Tab Switcher
│ └──────────────────────────┴─────────────────────────┘ │
│                                                        │
│ [IF DEPOSIT TAB SELECTED]:                             │
│ Select Contributing Stakeholder *                      │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Rahim Uddin (FC-002)                            [▼]│ │ <-- ExposedDropdownMenu (Active only)
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Deposit Amount (৳ BDT) * (Whole integer >= 1)          │
│ ┌────────────────────────────────────────────────────┐ │
│ │ 50000                                              │ │ <-- OutlinedTextField (Keyboard: Number)
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Deposit Date * (Date <= Today)                         │
│ ┌────────────────────────────────────────────────────┐ │
│ │ 13 / 11 / 2026                                 [Cal]│ │ <-- DatePicker Field
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Payment Method *                                       │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [Bank Transfer                                  ▼] │ │ <-- Dropdown (Cash, Bank, Mobile)
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Note (Optional)                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Monthly contribution tranche 2                     │ │ <-- OutlinedTextField (max 250)
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Post Deposit to Firm Ledger                        │ │ <-- FilledButton (Primary)
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ [IF EXPENSE TAB SELECTED]:                             │
│ Expense Date * (Date <= Today)                         │
│ ┌────────────────────────────────────────────────────┐ │
│ │ 13 / 11 / 2026                                 [Cal]│ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Expense Amount (৳ BDT) * (Whole integer >= 1)          │
│ ┌────────────────────────────────────────────────────┐ │
│ │ 12000                                              │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Expense Category *                                     │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [Office & Administration                         ▼] │ │ <-- 8 Predefined Categories
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Description or Purpose *                               │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Office supplies and report printing papers         │ │ <-- OutlinedTextField (3..250 chars)
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Payment Method *                                       │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [Cash                                           ▼] │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Post Expense to Firm Ledger                        │ │ <-- FilledButton (Primary)
│ └────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

---

## Package C: Membership & Profiles

### Screen 08: Members Roster Screen
*   **Purpose:** Searchable roster of all members with cumulative deposit totals and status badges.
*   **User Roles:** Both roles (Admin has FAB to add member).

```text
┌────────────────────────────────────────────────────────┐
│ Stakeholders Roster (14)                           [Q] │ <-- TopAppBar
├────────────────────────────────────────────────────────┤
│ Search by name, phone, or Member ID...                 │ <-- SearchBar (SurfaceContainer)
├────────────────────────────────────────────────────────┤
│ ┌────────────────────────────────────────────────────┐ │
│ │ (KA) Kawsar Ahmed (FC-001)         [ADMIN] [ACTIVE]│ │ <-- Member Card
│ │ Deposits: ৳ 300,000 • 3 Transactions               │ │     Avatar Initials (40x40dp)
│ ├────────────────────────────────────────────────────┤ │
│ │ (RU) Rahim Uddin (FC-002)                  [ACTIVE]│ │
│ │ Deposits: ৳ 250,000 • 2 Transactions               │ │
│ ├────────────────────────────────────────────────────┤ │
│ │ (TA) Tanvir Ahmed (FC-003)               [INACTIVE]│ │ <-- Amber Status Chip
│ │ Deposits: ৳ 100,000 • 1 Transaction                │ │
│ ├────────────────────────────────────────────────────┤ │
│ │ (SH) Sajjad Hossain (FC-004)                 [LEFT]│ │ <-- Muted Gray Status Chip
│ │ Deposits: ৳ 50,000 • 1 Transaction                 │ │     Past records preserved
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│                                   ┌────────────────┐   │
│                                   │  [+] Add Member│   │ <-- Admin FAB
│                                   └────────────────┘   │
├────────────────────────────────────────────────────────┤
│  [Home]   [Members]   [Transactions]   [Reports]   [*] │
└────────────────────────────────────────────────────────┘
```

---

### Screen 09: Member Details & Individual Ledger
*   **Purpose:** Individual member profile and their chronological contribution history.
*   **Privacy Note:** Private NID and nominee fields are completely hidden from other members.

```text
┌────────────────────────────────────────────────────────┐
│ [<- Back] Member Profile                       [Admin] │
├────────────────────────────────────────────────────────┤
│                ┌──────────────┐                        │
│                │     (RU)     │                        │ <-- 64x64dp Avatar
│                └──────────────┘                        │
│                  Rahim Uddin                           │ <-- TitleLarge
│              Member ID: FC-002                         │ <-- LabelSmall Badge
│            Status: ACTIVE • Joined: 10 Jan 2026        │
│                                                        │
│ ┌─────────────────────────┬──────────────────────────┐ │
│ │ Cumulative Deposits     │ Transactions Count       │ │ <-- Summary Tonal Card
│ │ ৳ 250,000               │ 2 Deposits               │ │
│ └─────────────────────────┴──────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ Deposit History                                        │ <-- Section Header
│ ┌────────────────────────────────────────────────────┐ │
│ │ 12 Nov 2026 • Bank Transfer            + ৳ 100,000 │ │
│ │ Note: Tranche 1                                    │ │
│ ├────────────────────────────────────────────────────┤ │
│ │ 15 Oct 2026 • Bank Transfer            + ৳ 150,000 │ │
│ │ Note: Initial seed capital                         │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ [Deactivate Member]                [Transfer Admin]    │ <-- Admin Only Danger Actions
└────────────────────────────────────────────────────────┘
```

---

### Screen 10: Add Stakeholder Modal (Admin)
*   **Purpose:** Quick onboarding requiring only contact info; auto-assigns sequential ID.
*   **User Roles:** Admin only.

```text
┌────────────────────────────────────────────────────────┐
│                        [Drag Handle]                   │
│                                                        │
│ Add New Stakeholder                                    │ <-- TitleLarge
│ System will allocate Member ID: FC-005                 │ <-- LabelSmall (Primary)
│                                                        │
│ Full Name *                                            │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Tariqul Islam                                      │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Mobile Phone Number * (11 digits)                      │
│ ┌────────────────────────────────────────────────────┐ │
│ │ 01712345678                                        │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Stakeholder Gmail Address * (Used for Google Sign-In)  │
│ ┌────────────────────────────────────────────────────┐ │
│ │ tariqul.invest@gmail.com                           │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ An in-app invitation will be sent to this email.      │ <-- BodySmall (Muted)
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Send Invitation                                    │ │ <-- FilledButton (Primary)
│ └────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

---

### Screen 11: Edit My Profile (Self-Service)
*   **Purpose:** Stakeholder privately enters/updates their NID and Nominee details for this circle.
*   **User Roles:** Active stakeholders (disabled for Inactive/Left).

```text
┌────────────────────────────────────────────────────────┐
│ [<- Back] Edit My Profile                      [Save]  │ <-- TopAppBar with Save Action
├────────────────────────────────────────────────────────┤
│ Circle: Green Valley Property Pool (FC-002)            │
│ Private fields are isolated to this Fund Circle.       │
│                                                        │
│ Full Name                                              │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Rahim Uddin                                        │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ National Identity Number (NID) *                       │
│ ┌────────────────────────────────────────────────────┐ │
│ │ 19882692015000123                                  │ │ <-- OutlinedTextField
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ BENEFICIARY / NOMINEE INFORMATION                      │ <-- Section Header
│ Nominee Full Name *                                    │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Nasrin Akter                                       │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Relationship *                                         │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Spouse                                             │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Nominee Contact Phone *                                │
│ ┌────────────────────────────────────────────────────┐ │
│ │ 01812345678                                        │ │
│ └────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

---

## Package D: Reports, Governance & Settings

### Screen 12: Reports Screen & Live Preview
*   **Purpose:** Filter reports by period, review live on-screen, and export formatted CSV ledger files via Android Share Sheet.
*   **User Roles:** Admin and Stakeholders.

```text
┌────────────────────────────────────────────────────────┐
│ Financial Reports Engine                               │ <-- TopAppBar
├────────────────────────────────────────────────────────┤
│ REPORTING PERIOD                                       │
│ ┌──────────────────────────┬─────────────────────────┐ │
│ │    [✓] Month & Year      │        All Time         │ │ <-- Segmented Button
│ └──────────────────────────┴─────────────────────────┘ │
│ Selected: [November ▼]  [2026 ▼]                       │
├────────────────────────────────────────────────────────┤
│ REPORT TYPE                                            │
│ (•) Monthly Financial Summary                          │ <-- Radio Selector Group
│ ( ) Member-wise Deposit Statement                      │
│ ( ) Complete Deposit History                           │
│ ( ) Complete Expense History                           │
├────────────────────────────────────────────────────────┤
│ ON-SCREEN PREVIEW                                      │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Fund Circle: Green Valley Property Pool            │ │ <-- Live Rendered Preview
│ │ Statement for: November 2026                       │ │
│ │ ────────────────────────────────────────────────── │ │
│ │ Opening Balance:                ৳ 1,362,000        │ │
│ │ Total Posted Deposits (Nov):  + ৳   100,000        │ │
│ │ Total Posted Expenses (Nov):  - ৳    12,000        │ │
│ │ Net Monthly Change:           + ৳    88,000        │ │
│ │ Closing Recorded Balance:       ৳ 1,450,000        │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │  [CSV] Export Ledger (CSV)                         │ │ <-- 56dp Primary Action Button
│ └────────────────────────────────────────────────────┘ │     Dispatches Android Share Sheet
├────────────────────────────────────────────────────────┤
│  [Home]   [Members]   [Transactions]   [Reports]   [*] │
└────────────────────────────────────────────────────────┘
```

---

### Screen 13: Settings & Circle Governance
*   **Purpose:** Account settings, circle switching (clean top bar rule), voluntary leave, and Admin ownership/archival.
*   **User Roles:** Admin and Stakeholders.

```text
┌────────────────────────────────────────────────────────┐
│ Settings & Circle Options                              │ <-- TopAppBar
├────────────────────────────────────────────────────────┤
│ WORKSPACE NAVIGATION                                   │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [Exchange] Switch Fund Circle                   [>]│ │ <-- Prominent Switch Entry
│ │ Return to Hub to switch or manage other circles    │ │     (Keeps Top Bar clean)
│ └────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ MY MEMBERSHIP                                          │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [User] My Profile (FC-001)                      [>]│ │
│ │ View personal and nominee information              │ │
│ ├────────────────────────────────────────────────────┤ │
│ │ [Door] Leave Fund Circle                        [>]│ │ <-- Stakeholder Voluntary Exit
│ │ Step down and retain historical records only       │ │
│ └────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ CIRCLE GOVERNANCE (Admin Only)                         │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [Crown] Transfer Admin Ownership                [>]│ │
│ │ Hand over circle leadership to another member      │ │
│ ├────────────────────────────────────────────────────┤ │
│ │ [Archive] Archive Fund Circle                   [>]│ │ <-- Permanent Read-Only Freeze
│ │ Lock ledger permanently (Hard delete blocked)      │ │
│ └────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│ APP PREFERENCES & ACCOUNT                              │
│ ┌────────────────────────────────────────────────────┐ │
│ │ [Moon] Dark Theme                               [X]│ │
│ ├────────────────────────────────────────────────────┤ │
│ │ [Exit] Sign Out of Google Account               [>]│ │
│ └────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────┤
│  [Home]   [Members]   [Transactions]   [Reports]   [*] │
└────────────────────────────────────────────────────────┘
```

---

### Screen 14: Reverse Transaction Dialog (Admin)
*   **Purpose:** Immutable transaction reversal protocol with mandatory reason and atomic balance reversal.
*   **User Roles:** Admin only.

```text
┌────────────────────────────────────────────────────────┐
│                        [Drag Handle]                   │
│                                                        │
│ Reverse this Transaction?                     [X Close]│
│ This will subtract the amount back from the pool       │
│ balance. Financial records are immutable—this entry    │
│ will permanently remain marked as REVERSED.            │
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ TX-001 • Deposit • Bank Transfer                  │ │ <-- Read-Only Transaction Summary
│ │ Original Amount: ৳ 50,000                          │ │     (SurfaceContainer, 12dp padding)
│ │ Date: 12 / 11 / 2026 • Recorded by Admin           │ │
│ └────────────────────────────────────────────────────┘ │
│                                                        │
│ Reason for Reversal *                                  │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Duplicate payment entry reported by bank           │ │ <-- OutlinedTextField (Required)
│ │ statement reconciliation.                          │ │     Character counter: 53/250
│ └────────────────────────────────────────────────────┘ │     Helper: 5-250 characters
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Confirm & Reverse Balance                          │ │ <-- FilledButton (Error Container)
│ └────────────────────────────────────────────────────┘ │     Disabled until reason >= 5 chars
│                                                        │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Keep Entry                                         │ │ <-- TextButton (Neutral)
│ └────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────┘
```

---

## 3. UI Component hierarchy in Jetpack Compose

```
MainActivity (enableEdgeToEdge)
  └── NavHost (startDestination = "auth_gate")
        ├── "auth_gate" ──────────▶ LoginScreen (Screen 01)
        ├── "hub" ────────────────▶ HubScreen (Screen 02)
        │                            └── ModalBottomSheet: CreateCircle (Screen 03)
        │
        └── "circle/{fundId}" ────▶ CircleScaffold
                                      ├── TopAppBar (Clean Title: Circle Name)
                                      ├── BottomBar: NavigationBar (4 tabs + Settings)
                                      │     ├── Tab 0: HomeScreen (Screen 04)
                                      │     │     └── FloatingActionButton (Add Txn) ──▶ ModalBottomSheet: AddTxn (Screen 07)
                                      │     ├── Tab 1: MembersScreen (Screen 08)
                                      │     │     └── FloatingActionButton (Add Member) ──▶ ModalBottomSheet: AddMember (Screen 10)
                                      │     ├── Tab 2: TransactionsScreen (Screen 05)
                                      │     ├── Tab 3: ReportsScreen (Screen 12)
                                      │     └── Tab 4: SettingsScreen (Screen 13)
                                      │           └── Navigates back to "hub" on Switch Circle
                                      │
                                      └── Modals & Sub-Screens:
                                            ├── AddTransactionSheet (Screen 07)
                                            ├── ReverseTransactionDialog (Screen 14)
                                            ├── MemberDetailsScreen (Screen 09)
                                            ├── TransactionDetailsScreen (Screen 06)
                                            └── EditProfileScreen (Screen 11)
```

---

## 4. Architectural verification & compliance

1. **Clean Top Bar Rule:** Verified across all screens. The top app bar remains uncluttered, and **"Switch Fund Circle"** is cleanly situated inside Screen 13 (Settings).
2. **Whole Integer Currency:** Verified in Screen 03 (Creation), Screen 04 (Dashboard), Screen 05 (Transactions), Screen 07 (Add Transaction), Screen 12 (Reports), and Screen 14 (Reverse Dialog). Currency locked strictly to `"BDT" | "USD" | "EUR"`.
3. **WCAG AAA Accessibility:** Every touch target is explicitly mapped to $\ge 48\text{dp}$. All typography mappings specify `tnum` tabular alignment for financial figures.
4. **Offline & Edge-to-Edge Compatibility:** `MainActivity` invokes `enableEdgeToEdge()` before `setContent`. The `Scaffold` hierarchy explicitly configures `WindowInsets.safeDrawing`. For scrollable screens (Dashboard, Ledgers, Members), pass `innerPadding` directly into `LazyColumn(modifier = Modifier.fillMaxSize().consumeWindowInsets(innerPadding), contentPadding = innerPadding)` rather than applying it to the outer container, allowing content to scroll fluidly behind the system navigation bar without clipping or double-inset padding bugs. All `ModalBottomSheet` inputs apply `Modifier.imePadding()` to guarantee zero clipping behind transparent system bars across Android 8.0 through Android 15.
5. **No Layout Gaps:** All 14 primary screens, modal sheets, and sub-screens now have exhaustive ASCII wireframes and interaction specs.
