package com.fundcircle.app.presentation.screens.members.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.GetMemberDepositsUseCase
import com.fundcircle.app.domain.usecase.GetMemberKycUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveMemberUseCase
import com.fundcircle.app.domain.usecase.ToggleMemberStatusUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val observeMemberUseCase: ObserveMemberUseCase,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val getMemberDepositsUseCase: GetMemberDepositsUseCase,
    private val getMemberKycUseCase: GetMemberKycUseCase,
    private val toggleMemberStatusUseCase: ToggleMemberStatusUseCase,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<MemberDetailUiState, MemberDetailUiAction, MemberDetailUiEffect>(
    MemberDetailUiState()
) {
    private var dataCollectionJob: Job? = null
    private var depositsJob: Job? = null
    private var kycJob: Job? = null

    init {
        val fundId: String? = savedStateHandle[KEY_FUND_ID]
        val userId: String? = savedStateHandle[KEY_USER_ID] ?: savedStateHandle[KEY_MEMBER_ID]
        if (!fundId.isNullOrBlank() && !userId.isNullOrBlank()) {
            loadMember(fundId, userId)
        }
        observeNetwork()
    }

    override fun onAction(action: MemberDetailUiAction) {
        when (action) {
            is MemberDetailUiAction.Init -> loadMember(action.fundId, action.userId)
            is MemberDetailUiAction.ClickDeposit -> {
                sendEffect(
                    MemberDetailUiEffect.NavigateToTransactionDetail(
                        fundId = uiState.value.fundId,
                        txId = action.txId,
                        isExpense = false
                    )
                )
            }
            is MemberDetailUiAction.ToggleRevealNid -> {
                setState { copy(isNidRevealed = !isNidRevealed) }
            }
            is MemberDetailUiAction.NavigateToProfileEdit -> {
                sendEffect(MemberDetailUiEffect.NavigateToProfileEdit(uiState.value.fundId))
            }
            is MemberDetailUiAction.RequestStatusChange -> {
                setState { copy(pendingStatusChange = action.newStatus) }
            }
            is MemberDetailUiAction.DismissStatusChangeDialog -> {
                setState { copy(pendingStatusChange = null) }
            }
            is MemberDetailUiAction.ConfirmStatusChange -> confirmStatusChange()
            is MemberDetailUiAction.Refresh -> {
                val current = uiState.value
                loadMember(current.fundId, current.userId)
            }
            is MemberDetailUiAction.DismissError -> {
                setState { copy(errorMessage = null) }
            }
            is MemberDetailUiAction.NavigateBack -> {
                sendEffect(MemberDetailUiEffect.NavigateBack)
            }
        }
    }

    private fun loadMember(fundId: String, userId: String) {
        if (fundId.isBlank() || userId.isBlank()) return
        setState { copy(fundId = fundId, userId = userId, isLoading = true, errorMessage = null) }
        dataCollectionJob?.cancel()

        dataCollectionJob = viewModelScope.launch {
            combine(
                observeCircleUseCase(fundId),
                observeMemberUseCase(fundId, userId),
                authRepository.userSession
            ) { circle, member, session ->
                Triple(circle, member, session)
            }.catch {
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = UiText.StringResource(R.string.member_detail_error_loading)
                    )
                }
            }.collect { (circle, member, session) ->
                val currentUid = session?.uid.orEmpty()
                val currentEmail = session?.email.orEmpty()
                val isAdmin = circle.adminId == currentUid ||
                        circle.adminEmail.equals(currentEmail, ignoreCase = true)

                val isSelf = member != null && (
                        member.userId == currentUid ||
                                member.email.equals(currentEmail, ignoreCase = true)
                        )

                setState {
                    copy(
                        isLoading = false,
                        circle = circle,
                        member = member,
                        isAdmin = isAdmin,
                        isSelf = isSelf,
                        errorMessage = if (member == null) UiText.StringResource(R.string.member_detail_error_not_found) else null
                    )
                }

                if (member != null) {
                    loadDeposits(fundId, member.userId, member.email)
                    if (isAdmin || isSelf) {
                        loadKyc(fundId, member.userId)
                    }
                }
            }
        }
    }

    private fun loadKyc(fundId: String, memberUserId: String) {
        kycJob?.cancel()
        kycJob = viewModelScope.launch {
            setState { copy(isKycLoading = true) }
            getMemberKycUseCase(fundId, memberUserId).fold(
                onSuccess = { kyc ->
                    setState { copy(kyc = kyc, isKycLoading = false) }
                },
                onFailure = {
                    setState { copy(kyc = null, isKycLoading = false) }
                }
            )
        }
    }

    private fun loadDeposits(fundId: String, memberUserId: String, memberEmail: String? = null) {
        depositsJob?.cancel()
        depositsJob = viewModelScope.launch {
            getMemberDepositsUseCase(fundId, memberUserId, memberEmail)
                .catch { /* ignore, empty list */ }
                .collect { depositList ->
                    val activeSum = depositList.filter { !it.isVoid }.sumOf { it.amount }
                    setState {
                        copy(
                            deposits = depositList,
                            member = if (member != null && depositList.isNotEmpty()) {
                                member.copy(totalContributed = activeSum)
                            } else {
                                member
                            }
                        )
                    }
                }
        }
    }

    private fun confirmStatusChange() {
        val targetStatus = uiState.value.pendingStatusChange ?: return
        val currentFundId = uiState.value.fundId
        val currentUserId = uiState.value.userId

        if (currentUserId == uiState.value.circle?.adminId) {
            setState {
                copy(
                    pendingStatusChange = null,
                    errorMessage = UiText.StringResource(R.string.member_detail_creator_cannot_deactivate)
                )
            }
            return
        }

        setState { copy(isUpdatingStatus = true) }

        viewModelScope.launch {
            toggleMemberStatusUseCase(currentFundId, currentUserId, targetStatus).fold(
                onSuccess = {
                    setState {
                        copy(
                            isUpdatingStatus = false,
                            pendingStatusChange = null
                        )
                    }
                    sendEffect(
                        MemberDetailUiEffect.ShowSnackbar(
                            UiText.StringResource(R.string.member_detail_status_change_success)
                        )
                    )
                },
                onFailure = { error ->
                    setState {
                        copy(
                            isUpdatingStatus = false,
                            pendingStatusChange = null,
                            errorMessage = mapErrorToUiText(error)
                        )
                    }
                }
            )
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                setState { copy(isOffline = !isOnline) }
            }
        }
    }

    private fun mapErrorToUiText(error: Throwable): UiText = when (error) {
        is FundCircleError.Authorization -> UiText.StringResource(R.string.error_not_authorized)
        is FundCircleError.Network -> UiText.StringResource(R.string.dashboard_error_loading)
        else -> error.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.dashboard_error_loading)
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
        const val KEY_USER_ID = "userId"
        const val KEY_MEMBER_ID = "memberId"
    }
}
