package com.fundcircle.app.presentation.common

import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod

fun ExpenseCategory.asUiText(): UiText = when (this) {
    ExpenseCategory.OFFICE_AND_ADMINISTRATION -> UiText.StringResource(R.string.expense_category_office_and_administration)
    ExpenseCategory.TRAVEL_AND_TRANSPORTATION -> UiText.StringResource(R.string.expense_category_travel_and_transportation)
    ExpenseCategory.BANK_AND_TRANSACTION_CHARGES -> UiText.StringResource(R.string.expense_category_bank_and_transaction_charges)
    ExpenseCategory.LEGAL_AND_DOCUMENTATION -> UiText.StringResource(R.string.expense_category_legal_and_documentation)
    ExpenseCategory.MEETINGS_AND_EVENTS -> UiText.StringResource(R.string.expense_category_meetings_and_events)
    ExpenseCategory.UTILITIES_AND_COMMUNICATION -> UiText.StringResource(R.string.expense_category_utilities_and_communication)
    ExpenseCategory.RENT_AND_MAINTENANCE -> UiText.StringResource(R.string.expense_category_rent_and_maintenance)
    ExpenseCategory.OTHERS -> UiText.StringResource(R.string.expense_category_others)
}

fun PaymentMethod.asUiText(): UiText = when (this) {
    PaymentMethod.CASH -> UiText.StringResource(R.string.payment_method_cash)
    PaymentMethod.BANK_TRANSFER -> UiText.StringResource(R.string.payment_method_bank_transfer)
    PaymentMethod.MOBILE_BANKING -> UiText.StringResource(R.string.payment_method_mobile_banking)
}
