package com.fundcircle.app.presentation.screens.ledger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositsUseCase
import com.fundcircle.app.domain.usecase.ObserveExpensesUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import com.fundcircle.app.presentation.navigation.ScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeDepositsUseCase: ObserveDepositsUseCase,
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) : BaseViewModel<LedgerUiState, LedgerUiAction, LedgerUiEffect>(LedgerUiState()) {

    val fundId: String = savedStateHandle.get<String>(KEY_FUND_ID)
        ?: runCatching { savedStateHandle.toRoute<ScreenRoute.TransactionsLedger>().fundId }.getOrNull()
        .orEmpty()

    private val rawDeposits = MutableStateFlow<List<Deposit>>(emptyList())
    private val rawExpenses = MutableStateFlow<List<Expense>>(emptyList())
    private val selectedTab = MutableStateFlow(LedgerTab.DEPOSITS)
    private val searchQuery = MutableStateFlow("")
    private val filterPaymentMethod = MutableStateFlow<PaymentMethod?>(null)

    private var ledgerJob: Job? = null

    init {
        observeNetworkState()
        observeLedgerData()
        observeFilterPipeline()
    }

    override fun onAction(action: LedgerUiAction) {
        when (action) {
            is LedgerUiAction.SelectTab -> {
                selectedTab.value = action.tab
                setState { copy(selectedTab = action.tab) }
            }
            is LedgerUiAction.UpdateSearchQuery -> {
                searchQuery.value = action.query
                setState { copy(searchQuery = action.query) }
            }
            is LedgerUiAction.FilterByPaymentMethod -> {
                filterPaymentMethod.value = action.method
                setState { copy(filterPaymentMethod = action.method) }
            }
            is LedgerUiAction.ClickTransaction -> {
                sendEffect(
                    LedgerUiEffect.NavigateToDetail(
                        fundId = fundId,
                        txId = action.txId,
                        isExpense = action.isExpense
                    )
                )
            }
            is LedgerUiAction.ClickAddTransaction -> {
                sendEffect(LedgerUiEffect.OpenAddTransactionSheet(fundId))
            }
            is LedgerUiAction.ToggleSearch -> {
                val newActive = !uiState.value.isSearchActive
                val updatedQuery = if (!newActive) {
                    searchQuery.value = ""
                    ""
                } else {
                    searchQuery.value
                }
                setState { copy(isSearchActive = newActive, searchQuery = updatedQuery) }
            }
            is LedgerUiAction.DismissError -> {
                setState { copy(errorMessage = null) }
            }
            is LedgerUiAction.NavigateBack -> {
                sendEffect(LedgerUiEffect.NavigateBack)
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

    private fun observeLedgerData() {
        if (fundId.isBlank()) {
            setState {
                copy(
                    isLoading = false,
                    errorMessage = UiText.StringResource(R.string.circle_error_not_found)
                )
            }
            return
        }

        ledgerJob?.cancel()
        ledgerJob = viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            launch {
                observeCircleUseCase(fundId)
                    .catch { /* Handled in combined streams */ }
                    .collectLatest { circle ->
                        setState {
                            copy(
                                currency = circle.currency,
                                isLocked = circle.isLocked
                            )
                        }
                    }
            }

            launch {
                combine(
                    observeCircleUseCase(fundId),
                    authRepository.userSession
                ) { circle, session ->
                    val uid = session?.uid.orEmpty()
                    val email = session?.email.orEmpty()
                    val isAdmin = uid.isNotBlank() && (circle.adminId == uid || circle.adminEmail.equals(email, ignoreCase = true))
                    isAdmin
                }.catch { /* Ignore */ }
                    .collectLatest { isAdmin ->
                        setState { copy(isAdmin = isAdmin) }
                    }
            }

            launch {
                observeDepositsUseCase(fundId)
                    .catch { error -> handleDataError(error) }
                    .collectLatest { deposits ->
                        rawDeposits.value = deposits
                        setState { copy(isLoading = false) }
                    }
            }

            launch {
                observeExpensesUseCase(fundId)
                    .catch { error -> handleDataError(error) }
                    .collectLatest { expenses ->
                        rawExpenses.value = expenses
                        setState { copy(isLoading = false) }
                    }
            }
        }
    }

    private fun observeFilterPipeline() {
        viewModelScope.launch {
            combine(
                rawDeposits,
                rawExpenses,
                selectedTab,
                searchQuery,
                filterPaymentMethod
            ) { deposits, expenses, tab, query, method ->
                val trimmedQuery = query.trim()

                val filteredDeposits = deposits.filter { dep ->
                    val matchesMethod = method == null || dep.paymentMethod == method
                    val matchesQuery = trimmedQuery.isBlank() ||
                        dep.memberName.contains(trimmedQuery, ignoreCase = true) ||
                        dep.memberEmail.contains(trimmedQuery, ignoreCase = true) ||
                        dep.note?.contains(trimmedQuery, ignoreCase = true) == true
                    matchesMethod && matchesQuery
                }.sortedWith(compareByDescending<Deposit> { it.depositDate }.thenByDescending { it.id })

                val filteredExpenses = expenses.filter { exp ->
                    val matchesMethod = method == null || exp.paymentMethod == method
                    val matchesQuery = trimmedQuery.isBlank() ||
                        exp.description.contains(trimmedQuery, ignoreCase = true) ||
                        exp.paidBy.contains(trimmedQuery, ignoreCase = true) ||
                        exp.category.displayName.contains(trimmedQuery, ignoreCase = true)
                    matchesMethod && matchesQuery
                }.sortedWith(compareByDescending<Expense> { it.expenseDate }.thenByDescending { it.id })

                Pair(filteredDeposits, filteredExpenses)
            }.collectLatest { (filteredDeposits, filteredExpenses) ->
                setState {
                    copy(
                        deposits = filteredDeposits,
                        expenses = filteredExpenses
                    )
                }
            }
        }
    }

    private fun handleDataError(error: Throwable) {
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
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
    }
}
