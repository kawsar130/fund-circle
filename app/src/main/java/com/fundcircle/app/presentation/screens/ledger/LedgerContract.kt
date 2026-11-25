package com.fundcircle.app.presentation.screens.ledger

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

enum class LedgerTab {
    DEPOSITS,
    EXPENSES
}

data class LedgerUiState(
    val isLoading: Boolean = true,
    val selectedTab: LedgerTab = LedgerTab.DEPOSITS,
    val deposits: List<Deposit> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val filterPaymentMethod: PaymentMethod? = null,
    val filterCategory: ExpenseCategory? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isOffline: Boolean = false,
    val isAdmin: Boolean = false,
    val isLocked: Boolean = false,
    val currency: Currency = Currency.BDT,
    val errorMessage: UiText? = null
) : UiState

sealed interface LedgerUiAction : UiAction {
    data class SelectTab(val tab: LedgerTab) : LedgerUiAction
    data class UpdateSearchQuery(val query: String) : LedgerUiAction
    data class FilterByPaymentMethod(val method: PaymentMethod?) : LedgerUiAction
    data class FilterByCategory(val category: ExpenseCategory?) : LedgerUiAction
    data class ClickTransaction(val txId: String, val isExpense: Boolean) : LedgerUiAction
    data object ClickAddTransaction : LedgerUiAction
    data object ToggleSearch : LedgerUiAction
    data object DismissError : LedgerUiAction
    data object NavigateBack : LedgerUiAction
}

sealed interface LedgerUiEffect : UiEffect {
    data class NavigateToDetail(val fundId: String, val txId: String, val isExpense: Boolean) : LedgerUiEffect
    data class OpenAddTransactionSheet(val fundId: String) : LedgerUiEffect
    data class ShowSnackbar(val message: UiText) : LedgerUiEffect
    data object NavigateBack : LedgerUiEffect
}
