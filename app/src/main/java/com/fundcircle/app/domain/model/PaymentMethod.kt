package com.fundcircle.app.domain.model

/**
 * Predefined transaction payment methods as specified in API Specification § 1.2 and § 6.1.
 */
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_BANKING("Mobile Banking");

    companion object {
        fun fromString(value: String): PaymentMethod = entries.firstOrNull {
            it.displayName.equals(value.trim(), ignoreCase = true) ||
                it.name.equals(value.trim(), ignoreCase = true) ||
                it.name.equals(value.trim().replace(" ", "_"), ignoreCase = true)
        } ?: CASH

        fun fromStringOrNull(value: String?): PaymentMethod? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull {
                it.displayName.equals(value.trim(), ignoreCase = true) ||
                    it.name.equals(value.trim(), ignoreCase = true) ||
                    it.name.equals(value.trim().replace(" ", "_"), ignoreCase = true)
            }
        }
    }
}
