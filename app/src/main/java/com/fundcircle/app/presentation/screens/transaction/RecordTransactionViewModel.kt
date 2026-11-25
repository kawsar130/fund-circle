package com.fundcircle.app.presentation.screens.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.RecordDepositUseCase
import com.fundcircle.app.domain.usecase.RecordExpenseUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecordTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase,
    private val recordDepositUseCase: RecordDepositUseCase,
    private val recordExpenseUseCase: RecordExpenseUseCase,
    private val authRepository: AuthRepository
) : BaseViewModel<RecordTransactionUiState, RecordTransactionUiAction, RecordTransactionUiEffect>(
    RecordTransactionUiState()
) {

    var fundId: String = savedStateHandle.get<String>(KEY_FUND_ID).orEmpty()
        private set

    private var circleJob: Job? = null
    private var membersJob: Job? = null

    init {
        loadCircleAndMembers()
    }

    private fun loadCircleAndMembers() {
        if (fundId.isBlank()) return

        circleJob?.cancel()
        circleJob = viewModelScope.launch {
            observeCircleUseCase(fundId)
                .catch { /* Handled in collector */ }
                .collectLatest { circle ->
                    setState {
                        copy(
                            currency = circle.currency,
                            isCircleLocked = circle.isLocked
                        )
                    }
                }
        }

        membersJob?.cancel()
        membersJob = viewModelScope.launch {
            observeCircleMembersUseCase(fundId)
                .catch { /* Handled in collector */ }
                .collectLatest { membersList ->
                    val activeMembers = membersList.filter { it.status == MemberStatus.ACTIVE }
                        .ifEmpty { membersList }
                    setState {
                        copy(
                            members = activeMembers,
                            selectedMember = selectedMember ?: activeMembers.firstOrNull()
                        )
                    }
                }
        }
    }

    override fun onAction(action: RecordTransactionUiAction) {
        when (action) {
            is RecordTransactionUiAction.Initialize -> {
                val isNewFund = action.fundId.isNotBlank() && action.fundId != fundId
                if (isNewFund) {
                    fundId = action.fundId
                }
                setState {
                    copy(
                        mode = action.initialMode,
                        amountInput = "",
                        dateEpochMs = System.currentTimeMillis(),
                        note = "",
                        description = "",
                        paidBy = "",
                        isSubmitting = false,
                        amountError = null,
                        dateError = null,
                        descriptionError = null,
                        memberError = null,
                        generalError = null
                    )
                }
                if (isNewFund) {
                    loadCircleAndMembers()
                }
            }
            is RecordTransactionUiAction.SelectMode -> {
                setState {
                    copy(
                        mode = action.mode,
                        amountError = null,
                        dateError = null,
                        descriptionError = null,
                        memberError = null,
                        generalError = null
                    )
                }
            }
            is RecordTransactionUiAction.UpdateAmount -> {
                // Invariant 1: Restrict to digits only, rejecting decimals and non-digits
                val filteredDigits = action.amount.filter { it.isDigit() }
                val cleanDigits = if (filteredDigits.length > 1 && filteredDigits.startsWith("0")) {
                    filteredDigits.trimStart('0').ifEmpty { "0" }
                } else {
                    filteredDigits
                }.take(15) // Max 15 digits per API Spec § 1.2
                setState {
                    copy(
                        amountInput = cleanDigits,
                        amountError = null
                    )
                }
            }
            is RecordTransactionUiAction.UpdateDate -> {
                val isFuture = DateUtils.isDateInFuture(action.epochMs)
                setState {
                    copy(
                        dateEpochMs = action.epochMs,
                        dateError = if (isFuture) UiText.StringResource(R.string.error_future_date) else null
                    )
                }
            }
            is RecordTransactionUiAction.SelectPaymentMethod -> {
                setState { copy(paymentMethod = action.method) }
            }
            is RecordTransactionUiAction.SelectMember -> {
                setState {
                    copy(
                        selectedMember = action.member,
                        memberError = null
                    )
                }
            }
            is RecordTransactionUiAction.UpdateNote -> {
                val limited = if (action.note.length <= 250) action.note else action.note.take(250)
                setState { copy(note = limited) }
            }
            is RecordTransactionUiAction.SelectCategory -> {
                setState { copy(category = action.category) }
            }
            is RecordTransactionUiAction.UpdateDescription -> {
                val limited = if (action.description.length <= 250) action.description else action.description.take(250)
                setState {
                    copy(
                        description = limited,
                        descriptionError = null
                    )
                }
            }
            is RecordTransactionUiAction.UpdatePaidBy -> {
                val limited = if (action.paidBy.length <= 100) action.paidBy else action.paidBy.take(100)
                setState { copy(paidBy = limited) }
            }
            is RecordTransactionUiAction.Submit -> {
                handleSubmit()
            }
            is RecordTransactionUiAction.DismissError -> {
                setState { copy(generalError = null) }
            }
            is RecordTransactionUiAction.Dismiss -> {
                sendEffect(RecordTransactionUiEffect.DismissSheet)
            }
        }
    }

    private fun handleSubmit() {
        val currentState = uiState.value

        if (currentState.isCircleLocked) {
            setState {
                copy(generalError = UiText.StringResource(R.string.dashboard_locked_notice))
            }
            return
        }

        val parsedAmount = currentState.amountInput.toLongOrNull()

        var hasError = false
        var amountErr: UiText? = null
        var dateErr: UiText? = null
        var memberErr: UiText? = null
        var descErr: UiText? = null

        // Invariant 1: Whole-integer money >= 1
        if (parsedAmount == null || parsedAmount < 1L) {
            amountErr = UiText.StringResource(R.string.error_amount_required)
            hasError = true
        }

        // Validate Date <= Now
        if (DateUtils.isDateInFuture(currentState.dateEpochMs)) {
            dateErr = UiText.StringResource(R.string.error_future_date)
            hasError = true
        }

        when (currentState.mode) {
            RecordTransactionMode.DEPOSIT -> {
                if (currentState.selectedMember == null) {
                    memberErr = UiText.StringResource(R.string.error_member_required)
                    hasError = true
                }
            }
            RecordTransactionMode.EXPENSE -> {
                val trimmedDesc = currentState.description.trim()
                if (trimmedDesc.length < 3) {
                    descErr = UiText.StringResource(R.string.error_description_too_short)
                    hasError = true
                } else if (trimmedDesc.length > 250) {
                    descErr = UiText.StringResource(R.string.error_description_too_long)
                    hasError = true
                }
            }
        }

        if (hasError) {
            setState {
                copy(
                    amountError = amountErr,
                    dateError = dateErr,
                    memberError = memberErr,
                    descriptionError = descErr
                )
            }
            return
        }

        val nonNullAmount = checkNotNull(parsedAmount)

        viewModelScope.launch {
            setState { copy(isSubmitting = true, generalError = null) }
            val session = authRepository.userSession.firstOrNull()

            when (currentState.mode) {
                RecordTransactionMode.DEPOSIT -> {
                    val member = checkNotNull(currentState.selectedMember)
                    val rawName = member.name.ifBlank { member.email }
                    val memberDisplayName = if (member.memberId.isNotBlank() && !rawName.contains("(")) {
                        "$rawName (${member.memberId})"
                    } else {
                        rawName
                    }
                    val deposit = Deposit(
                        id = UUID.randomUUID().toString(),
                        fundId = fundId,
                        memberId = member.userId,
                        memberEmail = member.email,
                        memberName = memberDisplayName,
                        amount = nonNullAmount,
                        depositDate = currentState.dateEpochMs,
                        paymentMethod = currentState.paymentMethod,
                        note = currentState.note.trim().takeIf { it.isNotBlank() }
                    )

                    recordDepositUseCase(fundId, deposit).fold(
                        onSuccess = {
                            setState { copy(isSubmitting = false) }
                            sendEffect(RecordTransactionUiEffect.ShowSnackbar(UiText.StringResource(R.string.modal_add_tx_success_deposit)))
                            sendEffect(RecordTransactionUiEffect.TransactionSaved)
                        },
                        onFailure = { error ->
                            setState {
                                copy(
                                    isSubmitting = false,
                                    generalError = mapErrorToUiText(error)
                                )
                            }
                        }
                    )
                }
                RecordTransactionMode.EXPENSE -> {
                    val trimmedDesc = currentState.description.trim()
                    val expense = Expense(
                        id = UUID.randomUUID().toString(),
                        fundId = fundId,
                        amount = nonNullAmount,
                        expenseDate = currentState.dateEpochMs,
                        category = currentState.category,
                        description = trimmedDesc,
                        paymentMethod = currentState.paymentMethod,
                        paidBy = currentState.paidBy.trim().ifBlank { session?.displayName.orEmpty() },
                        createdBy = session?.uid.orEmpty(),
                        creatorEmail = session?.email.orEmpty()
                    )

                    recordExpenseUseCase(fundId, expense).fold(
                        onSuccess = {
                            setState { copy(isSubmitting = false) }
                            sendEffect(RecordTransactionUiEffect.ShowSnackbar(UiText.StringResource(R.string.modal_add_tx_success_expense)))
                            sendEffect(RecordTransactionUiEffect.TransactionSaved)
                        },
                        onFailure = { error ->
                            setState {
                                copy(
                                    isSubmitting = false,
                                    generalError = mapErrorToUiText(error)
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private fun mapErrorToUiText(error: Throwable): UiText = when (error) {
        is FundCircleError.Authorization -> UiText.StringResource(R.string.error_not_authorized)
        is FundCircleError.Validation.InvalidAmount -> UiText.StringResource(R.string.error_amount_required)
        is FundCircleError.Validation.FutureDateNotAllowed -> UiText.StringResource(R.string.error_future_date)
        is FundCircleError.Validation.DescriptionTooShort -> UiText.StringResource(R.string.error_description_too_short)
        is FundCircleError.Validation.DescriptionTooLong -> UiText.StringResource(R.string.error_description_too_long)
        is FundCircleError.Validation.MemberRequired -> UiText.StringResource(R.string.error_member_required)
        is FundCircleError.Network -> UiText.StringResource(R.string.dashboard_error_loading)
        else -> error.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.dashboard_error_loading)
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
    }
}
