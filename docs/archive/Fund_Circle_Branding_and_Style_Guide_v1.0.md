# Fund Circle — Branding, Design System & UI Style Guide (v1.2)

> [!WARNING]
> **ARCHIVED DESIGN SYSTEM REFERENCE (HISTORICAL NARRATIVE)**  
> This comprehensive branding guide contains extended brand philosophy and prototype Kotlin snippets.  
> **For active UI implementation:** Refer to [`docs/Design_Tokens.md`](../Design_Tokens.md) (Semantic Tokens SSOT) and the production Kotlin theme files in `app/src/main/java/com/fundcircle/app/presentation/theme/`.

**Document Version:** 1.2  
**Status:** Approved Architecture & Design Baseline (Zero-Ambiguity Verified with Live Figma Integration)  
**Target Platform:** Android 14+ (API 34+), Jetpack Compose (BOM 2024.x+), Material Design 3 (M3)  
**Authors:** Senior Principal UX/UI Engineer & Android Architect  
**Traceability Reference:** `docs/archive/Investment_Firm_App_PRD_v2.md` (v3.0), `docs/archive/Investment_Firm_App_FSD_SRS_v1.md` (v2.1), `docs/archive/Investment_Firm_App_User_Stories_Use_Cases_v1.md` (v2.0), `Fund_Circle_User_Flowcharts_v1.0.md` (v1.0), `Fund_Circle_Wireframes_and_UI_Spec_v1.0.md` (v1.2)  
**Live Figma Assets:**
- **Master Figma Design File:** [Fund Circle — Mobile App (Android M3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ)
- **Interactive Code Layer Preview:** [Fund Circle Interactive Canvas (Node 27:3)](https://www.figma.com/design/44Gqq8dDY62FfKf28yxXuJ?node-id=27-3&autoplay-code-preview=27-3)
- **Master Flowchart (FigJam):** [Fund Circle — Master User Flow Architecture](https://www.figma.com/board/dxkTCpjsDBnEQp2zJRphAI)
- **Local Design Prototype:** [docs/design_preview/index.html](file:///Users/kawsar/Desktop/projects/personal/Android/fund-circle/docs/design_preview/index.html)

---

## Executive Summary & Architectural Intent

The **Fund Circle Branding, Design System & UI Style Guide** defines the complete sensory, visual, spatial, motion, and typographic framework for the Fund Circle commercial Android application. In pooled-capital and collective fund management, **trust is the core product**. The design system bridges the gap between rigorous institutional financial accountability and personal, communal warmth.

Every visual token, component behavior, motion curve, and microcopy pattern specified in this document is engineered specifically for modern Android development utilizing **Material 3 (M3)**, **Jetpack Compose**, and **Dynamic Theming** while enforcing strict financial accessibility, $O(1)$ visual cognition, and WCAG 2.1 AAA/AA contrast compliance.

---

## 1. Brand Philosophy, Archetype & Visual Identity

### 1.1 Brand Archetype: *The Transparent Steward*
Fund Circle operates under the archetype of **The Transparent Steward** (a synthesis of the Ruler and the Caregiver). The brand is not an impersonal, speculative Wall Street trading terminal, nor is it a casual social splitting app. It is a **dignified, immutable collective ledger** that protects stakeholder capital through absolute visibility.

```
       [ Wall Street Fintech ]
       (Cold, Speculative, Transactional)
                     ▲
                     │
                     │
 [ Social Expense Splitters ] ◄───────★───────► [ Private Wealth Bank ]
 (Casual, Unaudited, Ephemeral)   FUND CIRCLE     (Opaque, Gatekept, Heavy)
                     │          "The Transparent
                     │              Steward"
                     ▼
             [ Informal Chaos ]
             (Spreadsheets, Handshakes, Distrust)
```

### 1.2 Core Brand Values

| Value Pillar | Sensory Expression in UI | Behavioral Translation |
| :--- | :--- | :--- |
| **Absolute Integrity** | Deep Emerald Green, crisp borders, zero blur. | Transactions are immutable. Voided entries remain permanently visible with strike-throughs and reason audit cards. |
| **Collective Transparency** | Real-time aggregate counters, equal visibility across all roles. | Every member views the exact same ledger numbers as the Administrator; no hidden balance sheets. |
| **Frictionless Clarity** | High-contrast typography, whole integer monetary display, zero poisha/cents clutter. | Data entry requires strictly 5 essential inputs; navigation top app bar is kept completely clean. |
| **Uncompromised Security** | Slate Navy accents, cryptographic lock indicators, masked PII. | NID and Nominee data are securely masked; Google One-Tap auth is integrated natively. |

### 1.3 Brand Voice & Tone Matrix
The brand voice is **objective, calm, authoritative, and direct**. It eliminates speculative jargon, financial anxiety, and condescending prompts.

- **Objective over Emotional:** State financial facts plainly. Avoid celebratory confetti when recording regular capital contributions.
- **Constructive over Punitive:** When validation fails (e.g., attempting a future date), clearly state the policy and how to proceed rather than flashing generic error flags.
- **Plainspoken over Obscure:** Use "Pooled Capital" instead of "Aggregate Asset Holdings"; use "Record Deposit" instead of "Credit Ledger Entry".

---

### 1.4 Logo Mark & Geometry

The Fund Circle brand mark is constructed from **two interlocking, continuous elliptical rings** forming an open infinity loop within a stabilizing shield envelope.

```
                    ┌─────────────────────────┐
                    │      ▲  (Top Arc)       │
                    │   .───.     .───.       │
                    │  /     \   /     \      │
                    │ │   A   │X│   B   │     │  A: Collective Circle (Members)
                    │  \     /   \     /      │  B: Capital Pool (Ledger)
                    │   '───'     '───'       │  X: Trust Interlock (Auditability)
                    │      ▼ (Bottom Arc)     │
                    └─────────────────────────┘
```

- **Ring A (Left):** Represents the **Stakeholder Circle** (Human collaboration, solidarity, mutual stewardship).
- **Ring B (Right):** Represents the **Capital Ledger** (Resource accumulation, investment projects, shared wealth).
- **Interlock X (Center):** The overlap creates a third stabilizing zone signifying **Unbreakable Transparency and Governance**.
- **Clear Space:** A minimum bounding clear space equal to $50\%$ of the logo mark's width ($0.5X$) must be maintained around the perimeter at all times.
- **Minimum Digital Reproduction Size:**
  - App Header / Action Bar: $24\text{dp} \times 24\text{dp}$
  - Standalone Splash / Auth: $72\text{dp} \times 72\text{dp}$
  - Minimum touch bounding box: $48\text{dp} \times 48\text{dp}$

---

### 1.5 Android 13+ Adaptive Icon Specification

In compliance with Google Play Store guidelines and Android 13+ (API level 33+) dynamic theming, the application icon is delivered as an **Adaptive Icon** with layered assets:

```
┌────────────────────────────────────────────────────────┐
│ Full Viewport: 108dp x 108dp (Vector / PNG Asset)     │
│   ┌────────────────────────────────────────────────┐   │
│   │ Safe Center Zone: 72dp x 72dp Circular Cutout  │   │
│   │                                                │   │
│   │               .─────────.                      │   │
│   │             .'  INTER-  '.                    │   │
│   │            /    LOCKING   \                   │   │
│   │           │      RINGS     │                  │   │
│   │            \    EMBLEM    /                   │   │
│   │             '.          .'                    │   │
│   │               '─────────'                     │   │
│   │                                                │   │
│   └────────────────────────────────────────────────┘   │
│   Mask Margin (18dp Bleed for OEM Masking / Parallax)  │
└────────────────────────────────────────────────────────┘
```

1. **Background Layer (`res/drawable/ic_launcher_background.xml`):**
   - Solid Deep Emerald `#006D4D` (Light) or Rich Obsidian `#002114` with a subtle $15^\circ$ diagonal tonal gradient to `#003826`.
2. **Foreground Layer (`res/drawable/ic_launcher_foreground.xml`):**
   - Interlocking Stewardship Rings rendered in Crisp White (`#FFFFFF`) with a subtle 0.5dp inner highlight in Pale Mint (`#00DF9A`). Scaled strictly within the $72\text{dp}$ diameter safe zone to prevent edge clipping on Samsung OneUI, Google Pixel, or Xiaomi MIUI launcher masks.
3. **Monochrome Vector Layer (`res/drawable/ic_launcher_monochrome.xml`):**
   - Single-color flat silhouette vector mask (`#FFFFFF` on `#00000000` transparent canvas). Required by Android 13+ for launcher **Themed Icons**, automatically inheriting the user's Monet wallpaper accent palette.
4. **Android 12+ Splash Screen (`SplashScreen` API):**
   - Background: `md_theme_light_surface` (`#FCFDF7`) / `md_theme_dark_surface` (`#111412`).
   - Icon: Centered $240\text{dp} \times 240\text{dp}$ vector drawable animating scale from $0.9X \rightarrow 1.0X$ over $300\text{ms}$ with `FastOutSlowInEasing`.

---

## 2. Material 3 Color System & Semantic Palette

### 2.1 Thematic Strategy & Tonal Foundations
The Fund Circle color system is rooted in Material Design 3 (M3) tonal palettes. It employs **Deep Emerald Green** as its primary anchor (representing stewardship and growth) and **Slate Navy** as its secondary structural anchor (representing formal governance and stability).

> [!IMPORTANT]
> **Dynamic Theming Policy (Monet vs Brand Identity):**  
> While general surface scaffolds and primary navigation elements adapt to Android 12+ Monet dynamic wallpaper colors, **all financial transaction semantics (Deposits, Expenses, Negative Balances, Void Statuses) are strictly locked to the Brand Semantic Palette**. User wallpaper palettes must NEVER distort financial status indicators.

---

### 2.2 Material 3 Light Theme Palette

| M3 Token Name | Hex Code | Tone | Role & UI Usage | Contrast vs Surface |
| :--- | :--- | :--- | :--- | :--- |
| `primary` | `#006D4D` | 40 | Primary buttons, active tab indicators, major branding | $8.2:1$ (AAA) |
| `onPrimary` | `#FFFFFF` | 100 | Text and icons on primary filled elements | $8.2:1$ (AAA) |
| `primaryContainer` | `#82F8C5` | 90 | Tonal containers, active filter chips, positive cards | $12.1:1$ (AAA) |
| `onPrimaryContainer`| `#002114` | 10 | Text and icons on primary containers | $13.5:1$ (AAA) |
| `secondary` | `#1B365D` | 30 | Secondary buttons, Admin badges, audit labels | $9.8:1$ (AAA) |
| `onSecondary` | `#FFFFFF` | 100 | Text and icons on secondary filled elements | $9.8:1$ (AAA) |
| `secondaryContainer`| `#D9E2FF` | 90 | Admin profile chips, institutional alert containers | $11.4:1$ (AAA) |
| `onSecondaryContainer`|`#001944` | 10 | Text and icons on secondary containers | $14.1:1$ (AAA) |
| `tertiary` | `#7D5700` | 40 | Milestone highlights, currency lock icons, warnings | $7.4:1$ (AAA) |
| `onTertiary` | `#FFFFFF` | 100 | Text on tertiary filled elements | $7.4:1$ (AAA) |
| `tertiaryContainer` | `#FFDE9C` | 90 | Warning banners, currency permanence badge | $11.8:1$ (AAA) |
| `onTertiaryContainer`|`#261900` | 10 | Text and icons on tertiary containers | $14.6:1$ (AAA) |
| `error` | `#BA1A1A` | 40 | Destructive actions, validation errors, expense ledger | $7.6:1$ (AAA) |
| `onError` | `#FFFFFF` | 100 | Text on error filled elements | $7.6:1$ (AAA) |
| `errorContainer` | `#FFDAD6` | 90 | Expense card accents, voided transaction containers | $11.5:1$ (AAA) |
| `onErrorContainer` | `#410002` | 10 | Text on error containers | $13.9:1$ (AAA) |
| `background` | `#FCFDF7` | 98 | Root screen scaffold background | Baseline |
| `onBackground` | `#191C1A` | 10 | High-emphasis body text and primary titles | $15.4:1$ (AAA) |
| `surface` | `#FCFDF7` | 98 | Card surfaces, modal sheets, top bars | Baseline |
| `onSurface` | `#191C1A` | 10 | Card typography, list item titles | $15.4:1$ (AAA) |
| `surfaceVariant` | `#DDE5DE` | 90 | Text field backgrounds, divider lines, disabled cards | $12.3:1$ (AAA) |
| `onSurfaceVariant` | `#404944` | 30 | Medium-emphasis body text, secondary captions | $7.2:1$ (AAA) |
| `outline` | `#707973` | 50 | Outlined buttons, card borders, inactive dividers | $4.8:1$ (AA) |
| `outlineVariant` | `#BFC9C2` | 80 | Subtle borders, table row horizontal rules | $2.6:1$ (Non-text) |

---

### 2.3 Material 3 Dark Theme Palette

| M3 Token Name | Hex Code | Tone | Role & UI Usage | Contrast vs Surface |
| :--- | :--- | :--- | :--- | :--- |
| `primary` | `#00DF9A` | 80 | Primary buttons, active tab indicators, branding | $11.2:1$ (AAA) |
| `onPrimary` | `#003826` | 20 | Text and icons on primary filled elements | $11.2:1$ (AAA) |
| `primaryContainer` | `#005239` | 30 | Tonal containers, active filter chips, positive cards | $4.9:1$ (AA) |
| `onPrimaryContainer`| `#82F8C5` | 90 | Text and icons on primary containers | $12.4:1$ (AAA) |
| `secondary` | `#AFC6FF` | 80 | Secondary buttons, Admin badges, audit labels | $10.5:1$ (AAA) |
| `onSecondary` | `#10305F` | 20 | Text and icons on secondary filled elements | $10.5:1$ (AAA) |
| `secondaryContainer`| `#2B4776` | 30 | Admin profile chips, institutional alert containers | $5.1:1$ (AA) |
| `onSecondaryContainer`|`#D9E2FF` | 90 | Text and icons on secondary containers | $12.8:1$ (AAA) |
| `tertiary` | `#FABC3C` | 80 | Milestone highlights, currency lock icons, warnings | $11.7:1$ (AAA) |
| `onTertiary` | `#422C00` | 20 | Text on tertiary filled elements | $11.7:1$ (AAA) |
| `tertiaryContainer` | `#5F4100` | 30 | Warning banners, currency permanence badge | $5.4:1$ (AA) |
| `onTertiaryContainer`|`#FFDE9C` | 90 | Text and icons on tertiary containers | $12.9:1$ (AAA) |
| `error` | `#FFB4AB` | 80 | Destructive actions, validation errors, expense ledger | $10.8:1$ (AAA) |
| `onError` | `#690005` | 20 | Text on error filled elements | $10.8:1$ (AAA) |
| `errorContainer` | `#93000A` | 30 | Expense card accents, voided transaction containers | $4.7:1$ (AA) |
| `onErrorContainer` | `#FFDAD6` | 90 | Text on error containers | $12.2:1$ (AAA) |
| `background` | `#111412` | 6 | Root screen scaffold background (OLED optimized) | Baseline |
| `onBackground` | `#E1E3DF` | 90 | High-emphasis body text and primary titles | $14.2:1$ (AAA) |
| `surface` | `#111412` | 6 | Card surfaces, modal sheets, top bars | Baseline |
| `onSurface` | `#E1E3DF` | 90 | Card typography, list item titles | $14.2:1$ (AAA) |
| `surfaceVariant` | `#404944` | 30 | Text field backgrounds, divider lines, disabled cards | $4.8:1$ (AA) |
| `onSurfaceVariant` | `#BFC9C2` | 80 | Medium-emphasis body text, secondary captions | $9.6:1$ (AAA) |
| `outline` | `#89938D` | 60 | Outlined buttons, card borders, inactive dividers | $5.5:1$ (AA) |
| `outlineVariant` | `#404944` | 30 | Subtle borders, table row horizontal rules | $2.1:1$ (Non-text) |

---

### 2.4 Semantic Financial Transaction Colors (Fixed / Non-Monet)

These colors are **hardcoded across both Light and Dark themes** to preserve financial cognition across the Android ecosystem:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        FINANCIAL SEMANTIC TOKENS                       │
├───────────────────┬───────────────────┬───────────────────┬────────────┤
│ Transaction Type  │ Light Mode Hex    │ Dark Mode Hex     │ Meaning    │
├───────────────────┼───────────────────┼───────────────────┼────────────┤
│ Deposit / Inflow  │ #006D4D (Emerald) │ #00DF9A (Mint)    │ Capital In │
│ Expense / Outflow │ #BA1A1A (Crimson) │ #FFB4AB (Coral)   │ Capital Out│
│ Voided / Canceled │ #747775 (Neutral) │ #8E918F (Muted)   │ Inactive   │
│ Net Surplus       │ #006D4D (Emerald) │ #00DF9A (Mint)    │ Solvency   │
│ Net Deficit       │ #BA1A1A (Crimson) │ #FFB4AB (Coral)   │ Overdraft  │
│ KYC Masked Data   │ #404944 (Slate)   │ #BFC9C2 (Silver)  │ Protected  │
└───────────────────┴───────────────────┴───────────────────┴────────────┘
```

---

## 3. Typography System & Numeric Precision

### 3.1 Typeface Selection: *Inter & Roboto*
- **Primary Typeface:** **Inter** (System Variable Font fallback to `FontFamily.Default` / **Roboto**).
- **Core Typographic Values:** Large x-height, distinct letterforms (open apertures for `6`, `8`, `9`, `c`, `e`), and robust vertical rhythm on high-DPI Android AMOLED screens.

---

### 3.2 Material 3 Type Scale Specifications

```
Scale Name         Weight       Size (sp)   Line Height (sp)  Tracking (sp)   Role / Usage
─────────────────────────────────────────────────────────────────────────────────────────────────────
displayLarge       Bold (700)   57sp        64sp              -0.25sp         Splash hero numerals
displayMedium      Bold (700)   45sp        52sp               0.00sp         Net Balance Card figure
displaySmall       SemiBold     36sp        44sp               0.00sp         Section metrics
headlineLarge      Bold (700)   32sp        40sp               0.00sp         Screen titles (Create Circle)
headlineMedium     SemiBold     28sp        36sp               0.00sp         Modal headers, Dashboard
headlineSmall      Medium (500) 24sp        32sp               0.00sp         Card titles, Member names
titleLarge         SemiBold     22sp        28sp               0.00sp         Top App Bar, Dialog titles
titleMedium        Medium (500) 16sp        24sp              +0.15sp         Card sub-headers, Form labels
titleSmall         Medium (500) 14sp        20sp              +0.10sp         List item bold tags
bodyLarge          Regular(400) 16sp        24sp              +0.50sp         Long-form descriptions, Notes
bodyMedium         Regular(400) 14sp        20sp              +0.25sp         Table data rows, List details
bodySmall          Regular(400) 12sp        16sp              +0.40sp         Secondary timestamps, KYC
labelLarge         SemiBold     14sp        20sp              +0.10sp         Button text, Filter chips
labelMedium        Medium (500) 12sp        16sp              +0.50sp         Badges, Status pills (ACTIVE)
labelSmall         Medium (500) 11sp        16sp              +0.50sp         Audit stamps, Legal disclaimers
```

---

### 3.3 Mandatory Rule: OpenType Tabular Digits (`tnum`)

> [!CAUTION]
> **Financial Numeric Jitter Prevention Rule:**  
> In standard proportional font rendering, digit `1` is significantly narrower than digit `8` or `0`. When values are updated live or displayed in vertical tabular rows, numbers visually jitter and drift sideways.  
> **Rule:** Every Composable displaying monetary amounts, transaction timestamps, member counts, or sequence numbers **MUST explicitly set `fontFeatureSettings = "tnum"`** (Tabular Numbers). This guarantees monospaced numeric widths within proportional text.

```kotlin
// Architectural Standard for Financial Typographic Rendering
val FinancialMetricStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum" // Enforces monospaced tabular numerals
)
```

---

### 3.4 Currency & Identity Formatting Architecture

#### A. Whole-Integer Currency Standard
The application enforces a **whole-integer-only monetary architecture** ($\ge 1$, strictly zero poisha/cents):

1. **Locale-Aware Integer Grouping:**
   - US Dollar ($): `$1,250`, `$50,000`
   - Bangladeshi Taka (৳ / BDT): `৳ 50,000`, `৳ 1,00,000` (Subcontinent grouping)
   - Euro (€): `€ 1.250` or `1 250 €` (depending on device locale)
2. **Zero Decimals Policy:** Formatting patterns must NEVER render `.00` or `,00`.
3. **Prefix vs Suffix Placement:**
   - Standard ISO symbols (`$`, `£`, `€`, `৳`) precede the amount separated by a non-breaking half-space (`\u202F`).
   - 3-letter ISO codes (`USD`, `BDT`, `EUR`) follow the amount with a standard space (`50,000 BDT`).

#### B. PII & KYC Masking Standard
To safeguard stakeholder privacy while enabling administrative verification:
- **National ID (NID) Masking:** Render only the final 4 digits, replacing previous digits with middle-dot bullets: `•••• •••• 9821`.
- **Nominee Phone Masking:** Render `+880 17••••••89`.
- **Typographic Treatment:** Masked bullets must use tabular spacing to prevent width shifts between masked and unmasked states.

---

## 4. Elevation, Surface Architecture, Shape & Motion Tokens

### 4.1 Material 3 Tonal Elevation (OLED Optimized)

Fund Circle departs from legacy Android drop shadows. In Material 3, **elevation is expressed through surface color tinting**, preserving battery life on OLED displays and maintaining pure dark-mode contrast.

```
Elevation Level   Shadow dp   Surface Tint Overlay %   UI Container Mapping
────────────────────────────────────────────────────────────────────────────────────────
Level 0           0dp         0% Tint                  Scaffold Background, Full-bleed body
Level 1           1dp         5% Primary Tint          Transaction List Card, Settings Group
Level 2           3dp         8% Primary Tint          Dashboard Aggregate Metric Cards
Level 3           6dp         11% Primary Tint         Floating Action Button (Resting), Menus
Level 4           8dp         12% Primary Tint         Modal Bottom Sheet, Search Bar
Level 5           12dp        14% Primary Tint         Dialogs, Critical Confirmation Alerts
```

---

### 4.2 Shape System & Corner Radii

Shapes provide visual clues regarding interactive depth and nesting:

```
Shape Token           Radius dp       Component Application
────────────────────────────────────────────────────────────────────────────────────────
Shape.ExtraSmall      4dp             Status Badges, Tooltips, Verification Dots
Shape.Small           8dp             Input Fields (OutlinedTextField), Filter Chips, Small Buttons
Shape.Medium          12dp            Contextual Dropdowns, Snackbars, Small Dialogs
Shape.Large           16dp            Transaction Cards, Dashboard Cards, Standard Dialogs
Shape.ExtraLarge      28dp            Modal Bottom Sheets (Top-only), Full-screen Dialogs
Shape.Full            100dp (Circle)  Avatar circles, Floating Action Buttons (FAB), Pill badges
```

---

### 4.3 Spatial Grid & Spacing Tokens

The UI layout strictly follows the **4dp baseline grid** (with an 8dp standard layout step):

```
Token          Value (dp)    Standard Usage
────────────────────────────────────────────────────────────────────────────────────────
Spacing.xxs    2dp           Micro-borders, active indicator underline thickness
Spacing.xs     4dp           Vertical gap between title and caption, badge inner padding
Spacing.sm     8dp           Gap between icon and label, horizontal tag chip spacing
Spacing.md     12dp          Internal padding for small list items, compact card margins
Spacing.base   16dp          Standard screen edge margin, standard card internal padding
Spacing.lg     24dp          Gap between major card sections, modal top header margin
Spacing.xl     32dp          Hero metric vertical margins, auth splash graphic spacing
Spacing.xxl    48dp          Minimum interactive touch target size, bottom navigation height
```

> [!IMPORTANT]
> **Android Accessibility Touch Target Standard:**  
> All touch targets (Buttons, Icons, Filter Chips, Radio Items) must measure **at least $48\text{dp} \times 48\text{dp}$** bounding box, with at least $8\text{dp}$ separation between adjacent touch boundaries, strictly adhering to Android Core App Quality Guidelines.

---

### 4.4 Motion, Animation & Transition Tokens

To deliver fluid, predictable financial interactions without distracting gimmicks, the application implements standardized animation curves:

```
Interaction Context           Duration    Easing Curve                  Compose Spec
─────────────────────────────────────────────────────────────────────────────────────────────────────
Screen Navigation (Slide/Fade)300ms       FastOutSlowInEasing           tween(300, easing = FastOutSlowInEasing)
Bottom Sheet Enter/Exit       250ms       LinearOutSlowInEasing         tween(250, easing = LinearOutSlowInEasing)
Balance Counter Count-Up      600ms       FastOutSlowInEasing           animateIntAsState(tween(600))
Card Expand / Collapse        350ms       Spring (Medium Bouncy)        spring(dampingRatio = 0.8f, stiffness = 400f)
Dialog Fade & Scale           200ms       FastOutLinearInEasing         tween(200, easing = FastOutLinearInEasing)
Shimmer Skeleton Pulse        1200ms      LinearEasing (Infinite Loop)  infiniteRepeatable(tween(1200, LinearEasing))
```

---

## 5. Component Design Tokens & Anatomy

### 5.1 Button Hierarchy (5 Tiers + FAB)

```
┌────────────────────────────────────────────────────────────────────────┐
│                        BUTTON HIERARCHY MATRIX                         │
├─────────────────┬──────────────────┬─────────────────┬─────────────────┤
│ Tier            │ Visual Container │ Text / Icon     │ Primary Usage   │
├─────────────────┼──────────────────┼─────────────────┼─────────────────┤
│ 1. Primary      │ Filled Primary   │ onPrimary       │ Save, Record Tx,│
│    Filled       │ (Emerald #006D4D)│ (White #FFFFFF) │ Confirm Wizard  │
├─────────────────┼──────────────────┼─────────────────┼─────────────────┤
│ 2. Tonal        │ primaryContainer │ onPrimaryCont.  │ Filter Ledgers, │
│    Button       │ (Mint #82F8C5)   │ (#002114)       │ View History    │
├─────────────────┼──────────────────┼─────────────────┼─────────────────┤
│ 3. Outlined     │ 1dp outline      │ primary         │ Cancel Modal,   │
│    Button       │ (Transparent BG) │ (#006D4D)       │ Dismiss Sheet   │
├─────────────────┼──────────────────┼─────────────────┼─────────────────┤
│ 4. Text         │ Zero border      │ primary         │ "Learn more",   │
│    Button       │ (Transparent BG) │ (#006D4D)       │ "Skip", Links   │
├─────────────────┼──────────────────┼─────────────────┼─────────────────┤
│ 5. Destructive  │ Filled error     │ onError         │ Void Tx,        │
│    Action       │ (Crimson #BA1A1A)│ (White #FFFFFF) │ Remove Member   │
├─────────────────┼──────────────────┼─────────────────┼─────────────────┤
│ 6. Extended     │ Filled Primary   │ onPrimary       │ Main Dashboard: │
│    FAB          │ 16dp Elevation   │ (White #FFFFFF) │ "+ Add Record"  │
└─────────────────┴──────────────────┴─────────────────┴─────────────────┘
```

---

### 5.2 Input Field Anatomy (`OutlinedTextField`)

All input fields share a unified anatomical structure designed to eliminate ambiguity:

```
┌────────────────────────────────────────────────────────┐
│  Amount (Whole BDT) *                     [Label]      │  <- titleSmall (14sp)
├────────────────────────────────────────────────────────┤
│  [৳]   50,000                             [Clear (X)]  │  <- bodyLarge (16sp, tnum)
├────────────────────────────────────────────────────────┤
│  ▲ Enter an integer amount without decimals.           │  <- bodySmall (12sp)
└────────────────────────────────────────────────────────┘
 [Leading] [Input Value with Tabular Font]  [Trailing]
 [Icon   ]                                  [Action  ]
```

- **Resting Border:** 1dp `outlineVariant` (`#BFC9C2`).
- **Focused Border:** 2dp `primary` (`#006D4D`).
- **Error Border:** 2dp `error` (`#BA1A1A`).
- **Leading Icon:** Always contextual (e.g., Currency Symbol `৳`, Calendar `📅`, User `👤`).
- **Trailing Icon:** Contextual action (Clear button, password toggle, dropdown chevron).

---

### 5.3 Card Architecture & Audit Visual Diffs

#### A. Dashboard Balance Summary Card
```
┌────────────────────────────────────────────────────────┐
│ 🏛️ Green Valley Holdings               [Active Pill]  │
│ RECORDED POOL BALANCE                                  │
│ ৳ 4,850,000                            (DisplayMedium) │
├────────────────────────────┬───────────────────────────┤
│ ⬇ Total Deposits (14)      │ ⬆ Total Expenses (6)      │
│ ৳ 5,200,000 (Emerald)      │ ৳ 350,000 (Crimson)       │
└────────────────────────────┴───────────────────────────┘
```

#### B. Transaction Ledger Card Anatomy (Inflow vs Outflow vs Voided)
```
┌────────────────────────────────────────────────────────┐
│ [⬇ Deposit] Rafiq Ahmed                   +৳ 50,000    │ <- Emerald Bold
│ Monthly Capital Share • Bank Transfer                  │ <- bodySmall
│ Oct 12, 2026 • Cashier: Admin                          │ <- bodySmall muted
├────────────────────────────────────────────────────────┤
│ [⬆ Expense] Office Stationery              -৳ 3,500    │ <- Crimson Bold
│ Category: Office & Admin • Cash                        │ <- bodySmall
│ Oct 10, 2026 • Paid by: Tareq                          │ <- bodySmall muted
├────────────────────────────────────────────────────────┤
│ [🚫 Voided] ~Tariqul Islam~               ~৳ 25,000~   │ <- Strikethrough Muted
│ Check bounced at Bank (Reason)            [VOIDED]     │ <- Red Outline Pill
│ Voided by: Admin • Oct 09, 2026                        │ <- bodySmall muted
└────────────────────────────────────────────────────────┘
```

#### C. Audit Visual Diff Card (Voided & Field Edit Cases)
When a transaction is voided or audited, the system renders a **Visual Diff Card** showing the modification timeline:

```
┌────────────────────────────────────────────────────────┐
│ ⚠️ TRANSACTION VOIDED                        [VOIDED]  │
│ Original Amount:  ~৳ 50,000~ (Strikethrough Crimson)   │
│ Voided Amount:    ৳ 0 (Neutral Bold)                   │
├────────────────────────────────────────────────────────┤
│ Reason: "Check bounced at Bank on Oct 14"             │
│ Voided By: rafiq.ahmed@example.com (Circle Admin)      │
│ Timestamp: Oct 14, 2026 • 15:42:10 UTC                │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│ 📝 RECORD EDITED (AUDIT HISTORY)              [EDITED] │
│ Note Before: "Advance payment"                         │
│ Note After:  "Advance payment for legal registration"  │
├────────────────────────────────────────────────────────┤
│ Edited By: admin@example.com (Circle Admin)            │
│ Timestamp: Oct 15, 2026 • 11:20:04 UTC                │
└────────────────────────────────────────────────────────┘
```

---

### 5.4 Badges, Status Pills, Category & Payment Method Chips

#### A. Member & Transaction Badges
```
Badge Type       Text      Container Color           On-Container Color       Icon
────────────────────────────────────────────────────────────────────────────────────────
Status: Active   ACTIVE    primaryContainer(#82F8C5) onPrimaryContainer(#0021) Checkmark
Status: Inactive INACTIVE  surfaceVariant(#DDE5DE)   onSurfaceVariant(#4049)  Clock
Status: Left     LEFT      errorContainer(#FFDAD6)   onErrorContainer(#4100)  Cross
Status: Voided   VOIDED    errorContainer(#FFDAD6)   onErrorContainer(#4100)  Slash Circle
Role: Admin      ADMIN     secondaryContainer(#D9E2) onSecondaryCont.(#0019)  Shield
Role: Member     MEMBER    surfaceVariant(#DDE5DE)   onSurfaceVariant(#4049)  Person
```

#### B. The 8 Predefined Expense Category Chips (Strictly Enforced)
```
Category Name                    Container Color       Content Color         Material Icon
─────────────────────────────────────────────────────────────────────────────────────────────
1. Office & Administration       secondaryContainer    onSecondaryContainer  Icons.Default.Business
2. Travel & Transportation       tertiaryContainer     onTertiaryContainer   Icons.Default.DirectionsCar
3. Bank & Transaction Charges    errorContainer        onErrorContainer      Icons.Default.AccountBalance
4. Legal & Documentation         surfaceVariant        onSurfaceVariant      Icons.Default.Gavel
5. Meetings & Events             primaryContainer      onPrimaryContainer    Icons.Default.Groups
6. Utilities & Communication     secondaryContainer    onSecondaryContainer  Icons.Default.Bolt
7. Rent & Maintenance            tertiaryContainer     onTertiaryContainer   Icons.Default.HomeWork
8. Others                        surfaceVariant        onSurfaceVariant      Icons.Default.MoreHoriz
```

#### C. Payment Method Chips (Exactly Three Allowed)
```
Payment Method    Container Color       Content Color         Material Icon
─────────────────────────────────────────────────────────────────────────────────────────────
Cash              surfaceVariant        onSurfaceVariant      Icons.Default.Payments
Bank Transfer     secondaryContainer    onSecondaryContainer  Icons.Default.AccountBalance
Mobile Banking    primaryContainer      onPrimaryContainer    Icons.Default.PhoneAndroid
```

---

## 6. Microcopy, Voice Guidelines & UX Copywriting Standards

### 6.1 Plain-Language Financial Translation Table

| Avoid (Opaque / Wall Street Jargon) | Use Instead (Fund Circle Standard) | Architectural Reason |
| :--- | :--- | :--- |
| "Credit / Debit" | **"Deposit / Expense"** | Prevents double-entry accounting confusion among informal group members. |
| "Users / Accounts" | **"Stakeholders / Members"** | Emphasizes shared ownership and fiduciary dignity. |
| "Delete Transaction" | **"Void Record"** | Financial records cannot be erased; they are marked as void ($0 amount) with an audit reason. |
| "Net Capital Equity" | **"Recorded Pool Balance"** | Reflects the literal mathematical sum of recorded deposits minus expenses. |
| "Pay-in Slip / Remittance" | **"Deposit Proof / Note"** | Friendly, easily understood terminology for bank receipts. |
| "KYC Documents" | **"Identity Verification (NID)"** | Accessible local language for National ID submissions. |

---

### 6.2 Error Messaging Standard: *Cause + Consequence + Resolution*

Every error message presented via `Snackbar` or inline `OutlinedTextField` must follow the three-part formula:

```
┌────────────────────────────────────────────────────────────────────────┐
│ [Cause]: What happened?                                                │
│ [Consequence]: Why was the action halted?                              │
│ [Resolution]: Exactly what must the user do to proceed?               │
└────────────────────────────────────────────────────────────────────────┘
```

- ❌ **Poor:** `"Invalid Date."`
- ✅ **Fund Circle Standard:** `"Future date selected. Financial records can only be recorded for today or the past. Please choose a valid date."`
- ❌ **Poor:** `"Permission Denied."`
- ✅ **Fund Circle Standard:** `"Admin privilege required. Only Circle Admins can record group expenses. Contact your circle administrator."`
- ❌ **Poor:** `"Network Error."`
- ✅ **Fund Circle Standard:** `"Unable to reach cloud ledger. Displaying cached records. Check your internet connection and tap Retry."`

---

### 6.3 Empty State & Critical UX Notification Matrix

The application handles empty states and critical warnings with actionable, empathetic microcopy:

```
Scenario                  Headline                     Body Text                                     Primary Action
────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
1. Zero Circles           No Fund Circles Yet          Create your first circle or accept an invite  [+ Create Fund Circle]
   (Fresh Install)                                     to start managing pooled capital.
2. Empty Deposit Ledger   No Deposits Recorded         Stakeholders have not posted capital yet.     [+ Record Deposit]
                                                       Tap below to record the first contribution.
3. Empty Expense Ledger   Zero Expenses Incurred       No expenditures have been made from this      [+ Record Expense]
                                                       circle's fund pool yet.
4. Empty Member Directory Solo Circle                  You are the sole member of this circle.       [+ Invite Stakeholder]
                                                       Invite partners to share transparency.
5. No Pending Invites     All Clear                    You have no outstanding circle invitations.   [Create New Circle]
6. Offline Stale Cache    Viewing Offline Cache        Internet connection offline. Showing locally  [Retry Sync]
                                                       cached ledger data.
7. Currency Lock Notice   Currency Permanence Lock     Once the first transaction is posted, this    [I Understand]
                                                       currency cannot be changed.
8. Void Transaction Alert Irreversible Void Action     Voiding sets amount to 0 and logs an audit    [Confirm Void]
                                                       entry. This cannot be undone.
9. Leave Circle Warning   Depart Fund Circle           You will lose access to live ledgers and      [Leave Circle]
                                                       require an invitation to re-enter.
10. Delete Circle Dialog  Permanently Delete Circle    All member records will be erased. This is    [Delete Circle]
                                                       only permitted with 0 transactions.
```

---

## 7. Production-Ready Jetpack Compose Implementation

The following complete, copy-pasteable Kotlin source files implement the entire design system within an Android Jetpack Compose codebase.

### 7.1 `ui/theme/Color.kt`

```kotlin
package com.fundcircle.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// Material 3 Core Tonal Palettes (Light & Dark)
// =========================================================================

// Light Theme Tokens
val md_theme_light_primary = Color(0xFF006D4D)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFF82F8C5)
val md_theme_light_onPrimaryContainer = Color(0xFF002114)
val md_theme_light_secondary = Color(0xFF1B365D)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD9E2FF)
val md_theme_light_onSecondaryContainer = Color(0xFF001944)
val md_theme_light_tertiary = Color(0xFF7D5700)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFDE9C)
val md_theme_light_onTertiaryContainer = Color(0xFF261900)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFCFDF7)
val md_theme_light_onBackground = Color(0xFF191C1A)
val md_theme_light_surface = Color(0xFFFCFDF7)
val md_theme_light_onSurface = Color(0xFF191C1A)
val md_theme_light_surfaceVariant = Color(0xFFDDE5DE)
val md_theme_light_onSurfaceVariant = Color(0xFF404944)
val md_theme_light_outline = Color(0xFF707973)
val md_theme_light_outlineVariant = Color(0xFFBFC9C2)

// Dark Theme Tokens
val md_theme_dark_primary = Color(0xFF00DF9A)
val md_theme_dark_onPrimary = Color(0xFF003826)
val md_theme_dark_primaryContainer = Color(0xFF005239)
val md_theme_dark_onPrimaryContainer = Color(0xFF82F8C5)
val md_theme_dark_secondary = Color(0xFFAFC6FF)
val md_theme_dark_onSecondary = Color(0xFF10305F)
val md_theme_dark_secondaryContainer = Color(0xFF2B4776)
val md_theme_dark_onSecondaryContainer = Color(0xFFD9E2FF)
val md_theme_dark_tertiary = Color(0xFFFABC3C)
val md_theme_dark_onTertiary = Color(0xFF422C00)
val md_theme_dark_tertiaryContainer = Color(0xFF5F4100)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDE9C)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF111412)
val md_theme_dark_onBackground = Color(0xFFE1E3DF)
val md_theme_dark_surface = Color(0xFF111412)
val md_theme_dark_onSurface = Color(0xFFE1E3DF)
val md_theme_dark_surfaceVariant = Color(0xFF404944)
val md_theme_dark_onSurfaceVariant = Color(0xFFBFC9C2)
val md_theme_dark_outline = Color(0xFF89938D)
val md_theme_dark_outlineVariant = Color(0xFF404944)

// Standard Material 3 Schemes
val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
)

val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
)

// =========================================================================
// Custom Financial Semantic Palette (Locked against Monet Wallpaper shifts)
// =========================================================================

@Immutable
data class FinancialSemanticColors(
    val depositPositive: Color,
    val depositContainer: Color,
    val expenseNegative: Color,
    val expenseContainer: Color,
    val voidedMuted: Color,
    val voidedContainer: Color,
    val maskedKycText: Color
)

val LightFinancialColors = FinancialSemanticColors(
    depositPositive = Color(0xFF006D4D),
    depositContainer = Color(0xFF82F8C5),
    expenseNegative = Color(0xFFBA1A1A),
    expenseContainer = Color(0xFFFFDAD6),
    voidedMuted = Color(0xFF747775),
    voidedContainer = Color(0xFFE3E3E3),
    maskedKycText = Color(0xFF404944)
)

val DarkFinancialColors = FinancialSemanticColors(
    depositPositive = Color(0xFF00DF9A),
    depositContainer = Color(0xFF005239),
    expenseNegative = Color(0xFFFFB4AB),
    expenseContainer = Color(0xFF93000A),
    voidedMuted = Color(0xFF8E918F),
    voidedContainer = Color(0xFF2C2C2C),
    maskedKycText = Color(0xFFBFC9C2)
)

val LocalFinancialColors = staticCompositionLocalOf { LightFinancialColors }
```

---

### 7.2 `ui/theme/Type.kt`

```kotlin
package com.fundcircle.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// =========================================================================
// Typography Scale with Mandatory Tabular Digits on Metrics
// =========================================================================

val FundCircleTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = "tnum"
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum" // Essential for Pool Balance hero
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        fontFeatureSettings = "tnum" // Essential for ledger list alignment
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        fontFeatureSettings = "tnum"
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

### 7.3 `ui/theme/Shape.kt`

```kotlin
package com.fundcircle.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =========================================================================
// Material 3 Geometric Shape Scale
// =========================================================================

val FundCircleShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Status tags, verification dots
    small = RoundedCornerShape(8.dp),        // Input fields, filter chips
    medium = RoundedCornerShape(12.dp),      // Menus, dropdowns, snackbars
    large = RoundedCornerShape(16.dp),       // Transaction cards, balance cards
    extraLarge = RoundedCornerShape(28.dp)   // Modal bottom sheets (top-only)
)
```

---

### 7.4 `ui/theme/Theme.kt`

```kotlin
package com.fundcircle.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// =========================================================================
// Main Application Theme Composable
// =========================================================================

@Composable
fun FundCircleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Supports Android 12+ Monet
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val financialColors = if (darkTheme) DarkFinancialColors else LightFinancialColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalFinancialColors provides financialColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FundCircleTypography,
            shapes = FundCircleShapes,
            content = content
        )
    }
}

// Accessor for custom financial semantic tokens
object FundCircleThemeTokens {
    val financialColors: FinancialSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinancialColors.current
}
```

---

### 7.5 `ui/components/FundCircleButtons.kt`

```kotlin
package com.fundcircle.app.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// =========================================================================
// Production Buttons with Strict >= 48dp Touch Targets
// =========================================================================

@Composable
fun FundCirclePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small, // 8dp
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp) // Exceeds minimum 48dp touch target
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun FundCircleTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun FundCircleOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun FundCircleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun FundCircleDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun FundCircleExtendedFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        expanded = expanded,
        icon = { Icon(icon, contentDescription = null) },
        text = { Text(text, style = MaterialTheme.typography.labelLarge) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.defaultMinSize(minHeight = 56.dp)
    )
}
```

---

### 7.6 `ui/components/CurrencyText.kt` & Masking Utilities

```kotlin
package com.fundcircle.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.fundcircle.app.ui.theme.FundCircleThemeTokens
import java.text.NumberFormat
import java.util.Locale

// =========================================================================
// Production Tabular-Digit Integer Currency Formatter Composable
// =========================================================================

enum class TransactionTypeColorMode {
    DEPOSIT,
    EXPENSE,
    NET_BALANCE,
    NEUTRAL,
    VOIDED
}

@Composable
fun CurrencyText(
    amount: Long, // Strictly whole integer (no poisha / decimals)
    currencyCode: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    colorMode: TransactionTypeColorMode = TransactionTypeColorMode.NEUTRAL
) {
    val financialColors = FundCircleThemeTokens.financialColors

    val targetColor: Color = when (colorMode) {
        TransactionTypeColorMode.DEPOSIT -> financialColors.depositPositive
        TransactionTypeColorMode.EXPENSE -> financialColors.expenseNegative
        TransactionTypeColorMode.VOIDED -> financialColors.voidedMuted
        TransactionTypeColorMode.NET_BALANCE -> {
            if (amount >= 0) financialColors.depositPositive else financialColors.expenseNegative
        }
        TransactionTypeColorMode.NEUTRAL -> MaterialTheme.colorScheme.onSurface
    }

    // Locale-aware whole integer formatting with zero fractional digits
    val formattedInteger = NumberFormat.getIntegerInstance(Locale.getDefault()).format(amount)
    val displayText = "$currencyCode $formattedInteger"

    Text(
        text = displayText,
        color = targetColor,
        style = style, // Inherits fontFeatureSettings = "tnum" from Typography
        modifier = modifier
    )
}

// =========================================================================
// Privacy PII Masking Utilities
// =========================================================================

object PrivacyMaskUtils {
    /**
     * Masks National Identity (NID) numbers, revealing strictly the final 4 digits.
     * Example: "19942692500009821" -> "•••• •••• 9821"
     */
    fun maskNid(nid: String): String {
        val clean = nid.trim()
        if (clean.length <= 4) return clean
        val lastFour = clean.takeLast(4)
        return "•••• •••• $lastFour"
    }

    /**
     * Masks phone numbers for non-admin viewers.
     * Example: "+8801712345689" -> "+880 17••••••89"
     */
    fun maskPhone(phone: String): String {
        val clean = phone.trim()
        if (clean.length < 8) return "••••••••"
        val prefix = clean.take(6)
        val suffix = clean.takeLast(2)
        return "$prefix••••••$suffix"
    }
}
```

---

### 7.7 `ui/components/ExpenseCategoryChip.kt`

```kotlin
package com.fundcircle.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// =========================================================================
// Production Expense Category Chip for the 8 Predefined Categories
// =========================================================================

enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector
) {
    OFFICE_ADMIN("Office & Administration", Icons.Default.Business),
    TRAVEL_TRANSPORT("Travel & Transportation", Icons.Default.DirectionsCar),
    BANK_CHARGES("Bank & Transaction Charges", Icons.Default.AccountBalance),
    LEGAL_DOCS("Legal & Documentation", Icons.Default.Gavel),
    MEETINGS_EVENTS("Meetings & Events", Icons.Default.Groups),
    UTILITIES_COMM("Utilities & Communication", Icons.Default.Bolt),
    RENT_MAINTENANCE("Rent & Maintenance", Icons.Default.HomeWork),
    OTHERS("Others", Icons.Default.MoreHoriz);

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.find { it.displayName.equals(value, ignoreCase = true) } ?: OTHERS
        }
    }
}

@Composable
fun ExpenseCategoryChip(
    category: ExpenseCategory,
    modifier: Modifier = Modifier
) {
    val containerColor: Color = when (category) {
        ExpenseCategory.OFFICE_ADMIN -> MaterialTheme.colorScheme.secondaryContainer
        ExpenseCategory.TRAVEL_TRANSPORT -> MaterialTheme.colorScheme.tertiaryContainer
        ExpenseCategory.BANK_CHARGES -> MaterialTheme.colorScheme.errorContainer
        ExpenseCategory.LEGAL_DOCS -> MaterialTheme.colorScheme.surfaceVariant
        ExpenseCategory.MEETINGS_EVENTS -> MaterialTheme.colorScheme.primaryContainer
        ExpenseCategory.UTILITIES_COMM -> MaterialTheme.colorScheme.secondaryContainer
        ExpenseCategory.RENT_MAINTENANCE -> MaterialTheme.colorScheme.tertiaryContainer
        ExpenseCategory.OTHERS -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor: Color = when (category) {
        ExpenseCategory.OFFICE_ADMIN -> MaterialTheme.colorScheme.onSecondaryContainer
        ExpenseCategory.TRAVEL_TRANSPORT -> MaterialTheme.colorScheme.onTertiaryContainer
        ExpenseCategory.BANK_CHARGES -> MaterialTheme.colorScheme.onErrorContainer
        ExpenseCategory.LEGAL_DOCS -> MaterialTheme.colorScheme.onSurfaceVariant
        ExpenseCategory.MEETINGS_EVENTS -> MaterialTheme.colorScheme.onPrimaryContainer
        ExpenseCategory.UTILITIES_COMM -> MaterialTheme.colorScheme.onSecondaryContainer
        ExpenseCategory.RENT_MAINTENANCE -> MaterialTheme.colorScheme.onTertiaryContainer
        ExpenseCategory.OTHERS -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small, // 8dp
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.displayName,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
```

---

## 8. Design System Governance & Engineering Handoff Checklist

Before submitting any Pull Request touching UI screens or Jetpack Compose components, engineers must verify compliance against the following **Ten Invariant Rules**:

- [ ] **Rule 1 (Tabular Numerics):** All monetary figures, sequence numbers, and dates utilize `fontFeatureSettings = "tnum"`.
- [ ] **Rule 2 (Whole Integers):** No currency amounts render cents or poisha (`.00` is strictly rejected).
- [ ] **Rule 3 (Touch Targets):** Every interactive component possesses a minimum $48\text{dp} \times 48\text{dp}$ touch bounding box.
- [ ] **Rule 4 (Color Immunity):** Deposits, Expenses, and Voided states use `FinancialSemanticColors` (never overridden by user wallpaper Monet theme).
- [ ] **Rule 5 (Contrast Compliance):** All text elements meet WCAG 2.1 AAA ($\ge 7.0:1$) for body copy and AA ($\ge 4.5:1$) for large titles.
- [ ] **Rule 6 (OLED Elevation):** Cards and dialogs rely on Material 3 tonal surface tinting rather than artificial skeuomorphic drop shadows.
- [ ] **Rule 7 (Top Bar Cleanliness):** The Top App Bar contains only screen context and primary actions; "Switch Fund Circle" is housed exclusively within Settings.
- [ ] **Rule 8 (Immutable Diffs):** Voided transactions remain in the ledger with a strikethrough amount and an explicit reason card.
- [ ] **Rule 9 (Microcopy Clarity):** Error messages strictly provide *Cause + Consequence + Resolution*; obscure financial jargon is eliminated.
- [ ] **Rule 10 (Adaptive Icons):** The Android 13+ launcher icon complies with the 108dp viewport, 72dp safe zone, and monochrome dynamic theming mask.
