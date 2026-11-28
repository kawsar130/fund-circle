package com.fundcircle.app.presentation.screens.detail

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 06: Transaction Details & Audit Card.
 */
data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val fundId: String = "",
    val txId: String = "",
    val isExpense: Boolean = false,
    val deposit: Deposit? = null,
    val expense: Expense? = null,
    val currency: Currency = Currency.BDT,
    val isAdmin: Boolean = false,
    val adminEmail: String = "",
    val isOffline: Boolean = false,
    val isCircleLocked: Boolean = false,
    val showReverseDialog: Boolean = false,
    val isReversing: Boolean = false,
    val errorMessage: UiText? = null
) : UiState {
    val isVoid: Boolean
        get() = if (isExpense) expense?.isVoid == true else deposit?.isVoid == true

    /** True while this transaction exists only on this device (Invariant 5). */
    val isPendingSync: Boolean
        get() = if (isExpense) expense?.isPendingSync == true else deposit?.isPendingSync == true

    val originalAmount: Long
        get() = if (isExpense) expense?.amount ?: 0L else deposit?.amount ?: 0L
}

sealed interface TransactionDetailUiAction : UiAction {
    data object ClickBack : TransactionDetailUiAction
    data object OpenReverseDialog : TransactionDetailUiAction
    data object DismissReverseDialog : TransactionDetailUiAction
    data class ConfirmReversal(val reason: String) : TransactionDetailUiAction
    data object DismissError : TransactionDetailUiAction
}

sealed interface TransactionDetailUiEffect : UiEffect {
    data object NavigateBack : TransactionDetailUiEffect
    data class ShowSnackbar(val message: UiText) : TransactionDetailUiEffect
}
