# 🎨 Fund Circle — Design Tokens Quick Reference

> **Purpose:** Lean, high-density design token reference for Jetpack Compose UI implementation. Use this file instead of loading the full 1,300-line branding guide into agent context.

---

## 1. Material 3 Semantic Color Palette

| Token Name | Light Theme Hex | Dark Theme Hex | Semantic Usage |
| :--- | :--- | :--- | :--- |
| `primary` | `#006D4D` | `#00DF9A` | Brand Emerald, positive balance hero, primary buttons, active indicator |
| `onPrimary` | `#FFFFFF` | `#003825` | Text/icon on primary color |
| `primaryContainer` | `#82F8C5` | `#005238` | Tonal deposit cards, search highlight, deposit badges |
| `onPrimaryContainer` | `#002114` | `#82F8C5` | Text on primary container |
| `secondary` | `#1B365D` | `#8EAFCE` | Slate Navy, Admin badge, secondary chips, filter toggles |
| `onSecondary` | `#FFFFFF` | `#001944` | Text/icon on secondary color |
| `secondaryContainer` | `#D9E2FF` | `#1A3358` | Admin card containers, secondary surface blocks |
| `onSecondaryContainer`| `#001944` | `#D9E2FF` | Text on secondary container |
| `tertiary` | `#7D5700` | `#FABC3C` | Amber Gold, pending invitation alert, currency symbol, void notice |
| `onTertiary` | `#FFFFFF` | `#422C00` | Text/icon on tertiary color |
| `tertiaryContainer` | `#FFDE9C` | `#5C3F00` | Pending invite card background, warning banner fill |
| `onTertiaryContainer`| `#261900` | `#FFDE9C` | Text on tertiary container |
| `error` | `#BA1A1A` | `#FFB4AB` | Negative balance, expense amounts, destructive buttons |
| `onError` | `#FFFFFF` | `#690005` | Text/icon on error color |
| `errorContainer` | `#FFDAD6` | `#93000A` | Expense badges, void dialog confirmation container |
| `onErrorContainer` | `#410002` | `#FFDAD6` | Text on error container |
| `background` | `#FCFDF7` | `#111412` | Root window background |
| `onBackground` | `#191C1A` | `#E2E3DE` | Default high-contrast body text |
| `surface` | `#FCFDF7` | `#111412` | Top app bars, bottom navigation bar |
| `onSurface` | `#191C1A` | `#E2E3DE` | Text on surface |
| `surfaceVariant` | `#DDE5DE` | `#404944` | Card borders, text field fills, dividers |
| `onSurfaceVariant` | `#404944` | `#BFC9C2` | Subtitles, helper text, muted labels |
| `outline` | `#707973` | `#8A938C` | Outlined text field borders, card borders |
| `outlineVariant` | `#BFC9C2` | `#404944` | Subtle separators, disabled outlines |

### Dedicated Financial Semantic Colors
- **Deposit Credit:** Light `#006D4D` / Dark `#00DF9A`
- **Expense Debit:** Light `#BA1A1A` / Dark `#FFB4AB`
- **Voided Strikethrough:** Light `#707973` / Dark `#8A938C` (with Amber `#7D5700` badge)

> [!NOTE]
> **Dynamic Color Policy:** Dynamic wallpaper-based theming (`dynamicColor`) is **strictly disabled** (`dynamicColor = false`). In financial applications, color encodes semantic meaning (Emerald `#006D4D` for positive balance/deposits, Error `#BA1A1A` for expenses). Allowing wallpaper palettes would distort financial status, and attempting dynamic colors on Android < 12 (API 26–30) causes runtime crashes.

---

## 2. Typography Scale (Inter / Roboto)

> [!IMPORTANT]
> All financial figures and currency amounts **MUST** enforce **Tabular Digits** (`FontFeatureSettings = "tnum"`) to eliminate layout jitter during updates.

| Style Name | Size | Line Height | Weight | Font Feature | Semantic Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `HeadlineLarge` | 32sp | 40sp | Bold (700) | `"tnum"` | Hero Recorded Balance |
| `HeadlineMedium` | 28sp | 36sp | SemiBold (600) | `"tnum"` | Summary card balance totals |
| `TitleLarge` | 22sp | 28sp | SemiBold (600) | — | Screen top bar headers |
| `TitleMedium` | 16sp | 24sp | Medium (500) | — | Card headings, member names |
| `BodyLarge` | 16sp | 24sp | Regular (400) | — | Text inputs, descriptions |
| `BodyMedium` | 14sp | 20sp | Regular (400) | — | List subtitles, timestamps |
| `LabelLarge` | 14sp | 20sp | SemiBold (600) | `"tnum"` | Button labels, ledger amounts |
| `LabelSmall` | 11sp | 16sp | Medium (500) | — | Status chips, Member IDs |

---

## 3. Spacing, Shapes & Touch Targets

- **Baseline Grid:** `8dp` modular increments (`4dp`, `8dp`, `12dp`, `16dp`, `24dp`, `32dp`).
- **Screen Margins:** Horizontal `16dp`, top/bottom content padding `16dp`.
- **Card Spacing:** Vertical `12dp` between items in `LazyColumn`.
- **Touch Target Floor:** Minimum **`48dp × 48dp`** for all buttons and interactive elements (`40dp` absolute minimum for compact filter chips).
- **Corner Radii:**
  - **Small (`8dp`):** Status chips, text input fields.
  - **Medium (`16dp`):** Standard list cards, action dialogs.
  - **Large (`24dp`):** Bottom sheets, hero balance card.
  - **Full (`100dp`):** Pill badges, member avatar circles.

---

## 4. Predefined Expense Categories (Strictly 8)

1. `Office & Administration`
2. `Travel & Transportation`
3. `Bank & Transaction Charges`
4. `Legal & Documentation`
5. `Meetings & Events`
6. `Utilities & Communication`
7. `Rent & Maintenance`
8. `Others`

---

## 5. Production Theme & System Bar Protection (`Theme.kt`)

Guarantees full status and navigation bar icon legibility across light/dark themes on Android 8.0 to Android 15:

```kotlin
package com.fundcircle.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun FundCircleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Dark icons for light theme (!darkTheme), light icons for dark theme
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme

            // Disable translucent navigation bar scrim on Android 10+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```
