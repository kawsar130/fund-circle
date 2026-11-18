package com.fundcircle.app.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object DesignTokens {
    // Financial Semantics
    val DepositCreditLight = Color(0xFF006D4D)
    val DepositCreditDark = Color(0xFF00DF9A)

    val ExpenseDebitLight = Color(0xFFBA1A1A)
    val ExpenseDebitDark = Color(0xFFFFB4AB)

    val ReversedStrikethroughLight = Color(0xFF707973)
    val ReversedStrikethroughDark = Color(0xFF8A938C)

    // Touch Target Hygiene (Invariant 7)
    val MinTouchTarget = 48.dp
    val CompactTouchTarget = 40.dp

    // Spacing Grid
    val Spacing4 = 4.dp
    val Spacing8 = 8.dp
    val Spacing12 = 12.dp
    val Spacing16 = 16.dp
    val Spacing24 = 24.dp
    val Spacing32 = 32.dp
}
