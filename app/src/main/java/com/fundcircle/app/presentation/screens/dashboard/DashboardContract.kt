package com.fundcircle.app.presentation.screens.dashboard

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 04: Circle Home Dashboard (CUJ-04).
 * Single Source of Truth for state, intents, and one-shot effects.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val circle: FundCircle? = null,
    val recentTransactions: List<TransactionItem> = emptyList(),
    val isOffline: Boolean = false,
    val isAdmin: Boolean = false,
    val errorMessage: UiText? = null
) : UiState

sealed interface DashboardUiAction : UiAction {
    data object Refresh : DashboardUiAction
    data object NavigateToLedger : DashboardUiAction
    data class NavigateToTransactionDetail(val txId: String, val isExpense: Boolean) : DashboardUiAction
    data object NavigateToRecordTransaction : DashboardUiAction
    data object NavigateToSettings : DashboardUiAction
    data object NavigateToMembers : DashboardUiAction
    data object DismissError : DashboardUiAction
}

sealed interface DashboardUiEffect : UiEffect {
    data class NavigateToLedger(val fundId: String) : DashboardUiEffect
    data class NavigateToTransactionDetail(val fundId: String, val txId: String, val isExpense: Boolean) : DashboardUiEffect
    data class NavigateToRecordTransaction(val fundId: String) : DashboardUiEffect
    data class NavigateToSettings(val fundId: String) : DashboardUiEffect
    data class NavigateToMembers(val fundId: String) : DashboardUiEffect
    data class ShowSnackbar(val message: UiText) : DashboardUiEffect
}
