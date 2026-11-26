package com.fundcircle.app.domain.model

/**
 * Strictly 8 predefined operational expense categories as specified in API Specification § 1.2 and § 6.1.
 */
enum class ExpenseCategory(
    /**
     * The exact string stored in Firestore and exported to CSV (API Specification § 1.2).
     * Not for display: show `asUiText()` from the presentation layer so the label is localized.
     */
    val wireValue: String
) {
    OFFICE_AND_ADMINISTRATION("Office & Administration"),
    TRAVEL_AND_TRANSPORTATION("Travel & Transportation"),
    BANK_AND_TRANSACTION_CHARGES("Bank & Transaction Charges"),
    LEGAL_AND_DOCUMENTATION("Legal & Documentation"),
    MEETINGS_AND_EVENTS("Meetings & Events"),
    UTILITIES_AND_COMMUNICATION("Utilities & Communication"),
    RENT_AND_MAINTENANCE("Rent & Maintenance"),
    OTHERS("Others");

    companion object {
        fun fromString(value: String): ExpenseCategory = entries.firstOrNull {
            it.wireValue.equals(value.trim(), ignoreCase = true) ||
                it.name.equals(value.trim().replace(" ", "_").replace("&", "AND"), ignoreCase = true)
        } ?: OTHERS

        fun fromStringOrNull(value: String?): ExpenseCategory? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull {
                it.wireValue.equals(value.trim(), ignoreCase = true) ||
                    it.name.equals(value.trim().replace(" ", "_").replace("&", "AND"), ignoreCase = true)
            }
        }
    }
}
