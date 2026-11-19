package com.fundcircle.app.presentation.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositUseCase
import com.fundcircle.app.domain.usecase.ObserveExpenseUseCase
import com.fundcircle.app.domain.usecase.VoidTransactionUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import com.fundcircle.app.presentation.navigation.ScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeDepositUseCase: ObserveDepositUseCase,
    private val observeExpenseUseCase: ObserveExpenseUseCase,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val voidTransactionUseCase: VoidTransactionUseCase,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) : BaseViewModel<TransactionDetailUiState, TransactionDetailUiAction, TransactionDetailUiEffect>(
    TransactionDetailUiState()
) {

    private val navArgs: ScreenRoute.TransactionDetail? = runCatching {
        savedStateHandle.toRoute<ScreenRoute.TransactionDetail>()
    }.getOrNull()

    val fundId: String = navArgs?.fundId
        ?: savedStateHandle.get<String>(KEY_FUND_ID).orEmpty()

    val txId: String = navArgs?.txId
        ?: savedStateHandle.get<String>(KEY_TX_ID).orEmpty()

    val isExpense: Boolean = navArgs?.isExpense
        ?: savedStateHandle.get<Boolean>(KEY_IS_EXPENSE) ?: false

    init {
        setState {
            copy(
                fundId = this@TransactionDetailViewModel.fundId,
                txId = this@TransactionDetailViewModel.txId,
                isExpense = this@TransactionDetailViewModel.isExpense
            )
        }
        observeNetworkState()
        observeCircleAndAuth()
        observeTransactionData()
    }

    override fun onAction(action: TransactionDetailUiAction) {
        when (action) {
            is TransactionDetailUiAction.ClickBack -> {
                sendEffect(TransactionDetailUiEffect.NavigateBack)
            }
            is TransactionDetailUiAction.OpenReverseDialog -> {
                if (uiState.value.isAdmin && !uiState.value.isCircleLocked && !uiState.value.isVoid) {
                    setState { copy(showReverseDialog = true) }
                }
            }
            is TransactionDetailUiAction.DismissReverseDialog -> {
                if (!uiState.value.isReversing) {
                    setState { copy(showReverseDialog = false) }
                }
            }
            is TransactionDetailUiAction.ConfirmReversal -> {
                executeReversal(action.reason)
            }
            is TransactionDetailUiAction.DismissError -> {
                setState { copy(errorMessage = null) }
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

    private fun observeCircleAndAuth() {
        if (fundId.isBlank()) return

        viewModelScope.launch {
            combine(
                observeCircleUseCase(fundId),
                authRepository.userSession
            ) { circle, session ->
                val currentUid = session?.uid.orEmpty()
                val currentEmail = session?.email.orEmpty()
                val isAdmin = (currentUid.isNotBlank() && circle.adminId == currentUid) ||
                    (currentEmail.isNotBlank() && circle.adminEmail.equals(currentEmail, ignoreCase = true))
                val effectiveEmail = currentEmail.ifBlank { circle.adminEmail }
                Triple(circle, isAdmin, effectiveEmail)
            }.catch { /* Ignored, handled in transaction streams */ }
                .collectLatest { (circle, isAdmin, effectiveEmail) ->
                    setState {
                        copy(
                            currency = circle.currency,
                            isAdmin = isAdmin,
                            adminEmail = effectiveEmail,
                            isCircleLocked = circle.isLocked
                        )
                    }
                }
        }
    }

    private fun observeTransactionData() {
        if (fundId.isBlank() || txId.isBlank()) {
            setState {
                copy(
                    isLoading = false,
                    errorMessage = UiText.StringResource(R.string.tx_detail_error_not_found)
                )
            }
            return
        }

        viewModelScope.launch {
            if (isExpense) {
                observeExpenseUseCase(fundId, txId)
                    .catch { error ->
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = when (error) {
                                    is FundCircleError.NotFound -> UiText.StringResource(R.string.tx_detail_error_not_found)
                                    is FundCircleError.Authorization -> UiText.StringResource(R.string.error_not_authorized)
                                    else -> UiText.StringResource(R.string.tx_detail_error_loading)
                                }
                            )
                        }
                    }
                    .collectLatest { expense ->
                        setState {
                            copy(
                                isLoading = false,
                                expense = expense,
                                errorMessage = if (expense == null) UiText.StringResource(R.string.tx_detail_error_not_found) else null
                            )
                        }
                    }
            } else {
                observeDepositUseCase(fundId, txId)
                    .catch { error ->
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = when (error) {
                                    is FundCircleError.NotFound -> UiText.StringResource(R.string.tx_detail_error_not_found)
                                    is FundCircleError.Authorization -> UiText.StringResource(R.string.error_not_authorized)
                                    else -> UiText.StringResource(R.string.tx_detail_error_loading)
                                }
                            )
                        }
                    }
                    .collectLatest { deposit ->
                        setState {
                            copy(
                                isLoading = false,
                                deposit = deposit,
                                errorMessage = if (deposit == null) UiText.StringResource(R.string.tx_detail_error_not_found) else null
                            )
                        }
                    }
            }
        }
    }

    private fun executeReversal(reason: String) {
        val currentState = uiState.value
        if (currentState.isReversing || currentState.isVoid || !currentState.isAdmin || currentState.originalAmount < 1L) {
            return
        }

        viewModelScope.launch {
            setState { copy(isReversing = true) }

            val adminEmail = currentState.adminEmail.ifBlank {
                runCatching { authRepository.userSession.first()?.email }.getOrNull().orEmpty()
            }
            val originalAmount = currentState.originalAmount

            val result = voidTransactionUseCase(
                fundId = fundId,
                isExpense = isExpense,
                transactionId = txId,
                originalAmount = originalAmount,
                voidReason = reason,
                adminEmail = adminEmail
            )

            result.fold(
                onSuccess = {
                    setState {
                        copy(
                            isReversing = false,
                            showReverseDialog = false
                        )
                    }
                    sendEffect(
                        TransactionDetailUiEffect.ShowSnackbar(
                            UiText.StringResource(R.string.tx_detail_reverse_success)
                        )
                    )
                },
                onFailure = { error ->
                    val errorUiText = when (error) {
                        is FundCircleError.Validation.VoidReasonRequired ->
                            UiText.StringResource(R.string.dialog_reverse_reason_error)
                        is FundCircleError.Conflict.TransactionAlreadyVoided ->
                            UiText.StringResource(R.string.tx_detail_error_already_voided)
                        is FundCircleError.Authorization ->
                            UiText.StringResource(R.string.error_not_authorized)
                        else ->
                            if (!error.message.isNullOrBlank()) {
                                UiText.DynamicString(error.message.orEmpty())
                            } else {
                                UiText.StringResource(R.string.circle_error_generic)
                            }
                    }
                    setState {
                        copy(
                            isReversing = false,
                            errorMessage = errorUiText
                        )
                    }
                    sendEffect(TransactionDetailUiEffect.ShowSnackbar(errorUiText))
                }
            )
        }
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
        const val KEY_TX_ID = "txId"
        const val KEY_IS_EXPENSE = "isExpense"
    }
}
