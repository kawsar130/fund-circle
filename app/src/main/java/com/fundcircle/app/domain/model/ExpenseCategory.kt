package com.fundcircle.app.domain.model

import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText

/**
 * Strictly 8 predefined operational expense categories as specified in API Specification § 1.2 and § 6.1.
 */
enum class ExpenseCategory(val displayName: String) {
    OFFICE_AND_ADMINISTRATION("Office & Administration"),
    TRAVEL_AND_TRANSPORTATION("Travel & Transportation"),
    BANK_AND_TRANSACTION_CHARGES("Bank & Transaction Charges"),
    LEGAL_AND_DOCUMENTATION("Legal & Documentation"),
    MEETINGS_AND_EVENTS("Meetings & Events"),
    UTILITIES_AND_COMMUNICATION("Utilities & Communication"),
    RENT_AND_MAINTENANCE("Rent & Maintenance"),
    OTHERS("Others");

    fun asUiText(): UiText = when (this) {
        OFFICE_AND_ADMINISTRATION -> UiText.StringResource(R.string.expense_category_office_and_administration)
        TRAVEL_AND_TRANSPORTATION -> UiText.StringResource(R.string.expense_category_travel_and_transportation)
        BANK_AND_TRANSACTION_CHARGES -> UiText.StringResource(R.string.expense_category_bank_and_transaction_charges)
        LEGAL_AND_DOCUMENTATION -> UiText.StringResource(R.string.expense_category_legal_and_documentation)
        MEETINGS_AND_EVENTS -> UiText.StringResource(R.string.expense_category_meetings_and_events)
        UTILITIES_AND_COMMUNICATION -> UiText.StringResource(R.string.expense_category_utilities_and_communication)
        RENT_AND_MAINTENANCE -> UiText.StringResource(R.string.expense_category_rent_and_maintenance)
        OTHERS -> UiText.StringResource(R.string.expense_category_others)
    }

    companion object {
        fun fromString(value: String): ExpenseCategory = entries.firstOrNull {
            it.displayName.equals(value.trim(), ignoreCase = true) ||
                it.name.equals(value.trim().replace(" ", "_").replace("&", "AND"), ignoreCase = true)
        } ?: OTHERS

        fun fromStringOrNull(value: String?): ExpenseCategory? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull {
                it.displayName.equals(value.trim(), ignoreCase = true) ||
                    it.name.equals(value.trim().replace(" ", "_").replace("&", "AND"), ignoreCase = true)
            }
        }
    }
}
