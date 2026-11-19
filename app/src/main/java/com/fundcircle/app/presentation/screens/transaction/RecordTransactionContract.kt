package com.fundcircle.app.presentation.screens.transaction

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

enum class RecordTransactionMode {
    DEPOSIT,
    EXPENSE
}

data class RecordTransactionUiState(
    val mode: RecordTransactionMode = RecordTransactionMode.DEPOSIT,
    val amountInput: String = "",
    val dateEpochMs: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    // Deposit specific
    val members: List<CircleMember> = emptyList(),
    val selectedMember: CircleMember? = null,
    val note: String = "",
    // Expense specific
    val category: ExpenseCategory = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
    val description: String = "",
    val paidBy: String = "",
    // Metadata & Validation
    val currency: Currency = Currency.BDT,
    val isSubmitting: Boolean = false,
    val amountError: UiText? = null,
    val dateError: UiText? = null,
    val descriptionError: UiText? = null,
    val memberError: UiText? = null,
    val generalError: UiText? = null
) : UiState

sealed interface RecordTransactionUiAction : UiAction {
    data class SelectMode(val mode: RecordTransactionMode) : RecordTransactionUiAction
    data class UpdateAmount(val amount: String) : RecordTransactionUiAction
    data class UpdateDate(val epochMs: Long) : RecordTransactionUiAction
    data class SelectPaymentMethod(val method: PaymentMethod) : RecordTransactionUiAction
    // Deposit
    data class SelectMember(val member: CircleMember) : RecordTransactionUiAction
    data class UpdateNote(val note: String) : RecordTransactionUiAction
    // Expense
    data class SelectCategory(val category: ExpenseCategory) : RecordTransactionUiAction
    data class UpdateDescription(val description: String) : RecordTransactionUiAction
    data class UpdatePaidBy(val paidBy: String) : RecordTransactionUiAction
    // Submit & Dismiss
    data object Submit : RecordTransactionUiAction
    data object DismissError : RecordTransactionUiAction
    data object Dismiss : RecordTransactionUiAction
}

sealed interface RecordTransactionUiEffect : UiEffect {
    data class ShowSnackbar(val message: UiText) : RecordTransactionUiEffect
    data object TransactionSaved : RecordTransactionUiEffect
    data object DismissSheet : RecordTransactionUiEffect
}
