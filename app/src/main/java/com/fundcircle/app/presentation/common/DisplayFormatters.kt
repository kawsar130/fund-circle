package com.fundcircle.app.presentation.common

import java.util.Locale

/**
 * Two-letter monogram for a member avatar: first letters of the first two words,
 * or the first two letters of a single word. Falls back to "M" (member) when blank.
 */
fun extractInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "M"
        parts.size == 1 -> parts[0].take(2).uppercase(Locale.US)
        else -> "${parts[0].first()}${parts[1].first()}".uppercase(Locale.US)
    }
}

/**
 * Short human-readable transaction reference, e.g. `TX-DEP-633B`, built from the last
 * four characters of the document UUID. IDs already in that form are returned unchanged.
 */
fun shortTransactionRef(txId: String, isExpense: Boolean): String {
    if (txId.startsWith("TX-")) return txId
    val prefix = if (isExpense) "TX-EXP" else "TX-DEP"
    val suffix = txId.replace("-", "").takeLast(4).uppercase(Locale.US)
    return "$prefix-$suffix"
}
