package com.fundcircle.app.core.common

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats whole-unit money amounts (Invariant 1) for display, e.g. 1450000 -> "1,450,000".
 *
 * Every screen shows amounts through this one function so grouping stays identical everywhere.
 * Grouping is fixed to Western thousands (Locale.US) to match the design spec; the currency
 * symbol and any sign are added by the caller's string resource.
 */
object AmountFormatter {

    fun format(amount: Long): String = NumberFormat.getIntegerInstance(Locale.US).format(amount)
}
