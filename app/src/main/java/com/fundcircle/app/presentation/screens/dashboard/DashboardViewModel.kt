package com.fundcircle.app.presentation.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveRecentTransactionsUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import com.fundcircle.app.presentation.navigation.ScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val observeRecentTransactionsUseCase: ObserveRecentTransactionsUseCase,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) : BaseViewModel<DashboardUiState, DashboardUiAction, DashboardUiEffect>(DashboardUiState()) {

    val fundId: String = savedStateHandle.get<String>(KEY_FUND_ID)
        ?: runCatching { savedStateHandle.toRoute<ScreenRoute.Dashboard>().fundId }.getOrNull()
        .orEmpty()

    private var dashboardJob: Job? = null

    init {
        observeNetworkState()
        observeDashboardData()
        if (fundId.isNotBlank()) {
            viewModelScope.launch {
                authRepository.setActiveCircleId(fundId)
            }
        }
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                setState { copy(isOffline = !isOnline) }
            }
        }
    }

    private fun observeDashboardData() {
        if (fundId.isBlank()) {
            setState {
                copy(
                    isLoading = false,
                    errorMessage = UiText.StringResource(R.string.circle_error_not_found)
                )
            }
            return
        }

        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            combine(
                observeCircleUseCase(fundId),
                observeRecentTransactionsUseCase(fundId, RECENT_TX_LIMIT),
                authRepository.userSession
            ) { circle, transactions, userSession ->
                val currentUserId = userSession?.uid.orEmpty()
                val userEmail = userSession?.email.orEmpty()
                val isAdmin = (currentUserId.isNotBlank() && circle.adminId == currentUserId) ||
                    (userEmail.isNotBlank() && circle.adminEmail.equals(userEmail, ignoreCase = true))
                Triple(circle, transactions, isAdmin)
            }.catch { error ->
                val errorUiText = when (error) {
                    is FundCircleError.Authorization -> UiText.StringResource(R.string.error_not_authorized)
                    is FundCircleError.NotFound -> UiText.StringResource(R.string.circle_error_not_found)
                    is FundCircleError.Network -> UiText.StringResource(R.string.dashboard_error_loading)
                    else -> error.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(R.string.dashboard_error_loading)
                }
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = errorUiText
                    )
                }
            }.collectLatest { (circle, transactions, isAdmin) ->
                setState {
                    copy(
                        isLoading = false,
                        circle = circle,
                        recentTransactions = transactions,
                        isAdmin = isAdmin,
                        errorMessage = null
                    )
                }
            }
        }
    }

    override fun onAction(action: DashboardUiAction) {
        when (action) {
            DashboardUiAction.Refresh -> observeDashboardData()
            DashboardUiAction.OpenNotifications -> {
                sendEffect(DashboardUiEffect.ShowSnackbar(UiText.StringResource(R.string.dashboard_notifications_empty)))
            }
            DashboardUiAction.NavigateToLedger -> {
                if (fundId.isNotBlank()) {
                    sendEffect(DashboardUiEffect.NavigateToLedger(fundId))
                }
            }
            is DashboardUiAction.NavigateToTransactionDetail -> {
                if (fundId.isNotBlank()) {
                    sendEffect(
                        DashboardUiEffect.NavigateToTransactionDetail(
                            fundId = fundId,
                            txId = action.txId,
                            isExpense = action.isExpense
                        )
                    )
                }
            }
            is DashboardUiAction.NavigateToRecordTransaction -> {
                if (fundId.isNotBlank()) {
                    sendEffect(
                        DashboardUiEffect.NavigateToRecordTransaction(
                            fundId = fundId,
                            initialMode = action.initialMode
                        )
                    )
                }
            }
            DashboardUiAction.NavigateToSettings -> {
                if (fundId.isNotBlank()) {
                    sendEffect(DashboardUiEffect.NavigateToSettings(fundId))
                }
            }
            DashboardUiAction.NavigateToMembers -> {
                if (fundId.isNotBlank()) {
                    sendEffect(DashboardUiEffect.NavigateToMembers(fundId))
                }
            }
            DashboardUiAction.NavigateToReports -> {
                if (fundId.isNotBlank()) {
                    sendEffect(DashboardUiEffect.NavigateToReports(fundId))
                }
            }
            DashboardUiAction.DismissError -> {
                setState { copy(errorMessage = null) }
            }
        }
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
        const val RECENT_TX_LIMIT = 5
    }
}
