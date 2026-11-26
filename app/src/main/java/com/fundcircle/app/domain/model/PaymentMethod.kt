package com.fundcircle.app.domain.model

/**
 * Predefined transaction payment methods as specified in API Specification § 1.2 and § 6.1.
 */
enum class PaymentMethod(
    /**
     * The exact string stored in Firestore and exported to CSV (API Specification § 1.2).
     * Not for display: show `asUiText()` from the presentation layer so the label is localized.
     */
    val wireValue: String
) {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_BANKING("Mobile Banking");

    companion object {
        fun fromString(value: String): PaymentMethod = entries.firstOrNull {
            it.wireValue.equals(value.trim(), ignoreCase = true) ||
                it.name.equals(value.trim(), ignoreCase = true) ||
                it.name.equals(value.trim().replace(" ", "_"), ignoreCase = true)
        } ?: CASH

        fun fromStringOrNull(value: String?): PaymentMethod? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull {
                it.wireValue.equals(value.trim(), ignoreCase = true) ||
                    it.name.equals(value.trim(), ignoreCase = true) ||
                    it.name.equals(value.trim().replace(" ", "_"), ignoreCase = true)
            }
        }
    }
}
