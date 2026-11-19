package com.fundcircle.app.presentation.screens.members.detail

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 09: Member Details & Individual Ledger (CUJ-07).
 */
data class MemberDetailUiState(
    val isLoading: Boolean = true,
    val fundId: String = "",
    val userId: String = "",
    val circle: FundCircle? = null,
    val member: CircleMember? = null,
    val deposits: List<Deposit> = emptyList(),
    val kyc: MemberKyc? = null,
    val isKycLoading: Boolean = false,
    val isNidRevealed: Boolean = false,
    val isAdmin: Boolean = false,
    val isSelf: Boolean = false,
    val isOffline: Boolean = false,
    val pendingStatusChange: MemberStatus? = null,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: UiText? = null
) : UiState

sealed interface MemberDetailUiAction : UiAction {
    data class Init(val fundId: String, val userId: String) : MemberDetailUiAction
    data class ClickDeposit(val txId: String) : MemberDetailUiAction
    data object ToggleRevealNid : MemberDetailUiAction
    data object NavigateToProfileEdit : MemberDetailUiAction
    data class RequestStatusChange(val newStatus: MemberStatus) : MemberDetailUiAction
    data object ConfirmStatusChange : MemberDetailUiAction
    data object DismissStatusChangeDialog : MemberDetailUiAction
    data object Refresh : MemberDetailUiAction
    data object DismissError : MemberDetailUiAction
    data object NavigateBack : MemberDetailUiAction
}

sealed interface MemberDetailUiEffect : UiEffect {
    data class NavigateToTransactionDetail(val fundId: String, val txId: String, val isExpense: Boolean) : MemberDetailUiEffect
    data class NavigateToProfileEdit(val fundId: String) : MemberDetailUiEffect
    data object NavigateBack : MemberDetailUiEffect
    data class ShowSnackbar(val message: UiText) : MemberDetailUiEffect
}
