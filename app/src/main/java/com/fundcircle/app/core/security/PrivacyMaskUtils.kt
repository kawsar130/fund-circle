package com.fundcircle.app.core.security

/**
 * Utility for masking sensitive PII fields like NID numbers.
 * Conforms to API Specification § 4.3 Masking Rule:
 * Masks the NID resulting in `•••• •••• 9821`.
 */
object PrivacyMaskUtils {
    fun maskNid(nidNumber: String): String {
        val trimmed = nidNumber.trim()
        return if (trimmed.length >= 4) {
            "•••• •••• " + trimmed.takeLast(4)
        } else {
            "•••• •••• ••••"
        }
    }
}
