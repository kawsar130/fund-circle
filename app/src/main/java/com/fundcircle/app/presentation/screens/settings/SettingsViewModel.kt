package com.fundcircle.app.presentation.screens.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.repository.FundRepository
import com.fundcircle.app.domain.usecase.LeaveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.SignOutUseCase
import com.fundcircle.app.domain.usecase.TransferAdminOwnershipUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import com.fundcircle.app.presentation.navigation.ScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase,
    private val fundRepository: FundRepository,
    private val leaveCircleUseCase: LeaveCircleUseCase,
    private val transferAdminOwnershipUseCase: TransferAdminOwnershipUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val authRepository: AuthRepository
) : BaseViewModel<SettingsUiState, SettingsUiAction, SettingsUiEffect>(SettingsUiState()) {

    val fundId: String = savedStateHandle.get<String>(KEY_FUND_ID)
        ?: runCatching { savedStateHandle.toRoute<ScreenRoute.Settings>().fundId }.getOrNull()
        .orEmpty()

    init {
        setState { copy(fundId = this@SettingsViewModel.fundId) }
        loadSettingsData()
    }

    private fun loadSettingsData() {
        if (fundId.isBlank()) {
            setState { copy(isLoading = false, errorMessage = UiText.StringResource(R.string.circle_error_not_found)) }
            return
        }

        viewModelScope.launch {
            combine(
                authRepository.userSession,
                observeCircleUseCase(fundId),
                observeCircleMembersUseCase(fundId)
            ) { session, circle, members ->
                val currentUid = session?.uid.orEmpty()
                val currentEmail = session?.email.orEmpty()

                val isAdmin = (currentUid.isNotBlank() && circle.adminId == currentUid) ||
                        circle.adminEmail.equals(currentEmail, ignoreCase = true)

                val currentMember = members.firstOrNull { member ->
                    (currentUid.isNotBlank() && member.userId == currentUid) ||
                            member.email.equals(currentEmail, ignoreCase = true)
                }

                val activeMembers = members.filter { member ->
                    member.status == MemberStatus.ACTIVE && member.userId != currentUid
                }

                Triple(currentUid, circle, Pair(currentMember, activeMembers))
            }.catch { error ->
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = UiText.DynamicString(error.localizedMessage ?: "Failed to load settings")
                    )
                }
            }.collect { (currentUid, circle, memberPair) ->
                val (currentMember, activeMembers) = memberPair
                val isAdmin = (currentUid.isNotBlank() && circle.adminId == currentUid) ||
                        (currentMember?.email?.equals(circle.adminEmail, ignoreCase = true) == true)

                setState {
                    copy(
                        isLoading = false,
                        circle = circle,
                        currentUserId = currentUid,
                        currentMember = currentMember,
                        isAdmin = isAdmin,
                        isCircleLocked = circle.isLocked,
                        activeMembers = activeMembers
                    )
                }
            }
        }
    }

    override fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.ClickSwitchCircle -> {
                sendEffect(SettingsUiEffect.NavigateToHub)
            }
            is SettingsUiAction.ClickMyProfile -> {
                sendEffect(SettingsUiEffect.NavigateToProfile(fundId))
            }
            is SettingsUiAction.ToggleDarkTheme -> {
                setState { copy(isDarkTheme = !isDarkTheme) }
            }
            is SettingsUiAction.OpenLeaveDialog -> {
                if (uiState.value.isAdmin) {
                    sendEffect(SettingsUiEffect.ShowSnackbar(UiText.StringResource(R.string.settings_leave_admin_disabled)))
                } else {
                    setState { copy(showLeaveDialog = true) }
                }
            }
            is SettingsUiAction.ConfirmLeaveCircle -> {
                executeLeaveCircle()
            }
            is SettingsUiAction.OpenTransferAdminDialog -> {
                val firstMemberId = uiState.value.activeMembers.firstOrNull()?.userId
                setState {
                    copy(
                        showTransferAdminDialog = true,
                        selectedNewAdminUserId = firstMemberId
                    )
                }
            }
            is SettingsUiAction.SelectNewAdmin -> {
                setState { copy(selectedNewAdminUserId = action.userId) }
            }
            is SettingsUiAction.ConfirmTransferAdmin -> {
                executeTransferAdmin()
            }
            is SettingsUiAction.OpenLockDialog -> {
                setState { copy(showLockDialog = true) }
            }
            is SettingsUiAction.ConfirmToggleCircleLock -> {
                executeToggleLock()
            }
            is SettingsUiAction.OpenSignOutDialog -> {
                setState { copy(showSignOutDialog = true) }
            }
            is SettingsUiAction.ConfirmSignOut -> {
                executeSignOut()
            }
            is SettingsUiAction.DismissDialogs -> {
                setState {
                    copy(
                        showLeaveDialog = false,
                        showTransferAdminDialog = false,
                        showLockDialog = false,
                        showSignOutDialog = false
                    )
                }
            }
            is SettingsUiAction.DismissError -> {
                setState { copy(errorMessage = null) }
            }
        }
    }

    private fun executeLeaveCircle() {
        val state = uiState.value
        val circle = state.circle ?: return

        setState { copy(isActionInProgress = true) }
        viewModelScope.launch {
            // Invited members' documents are keyed by a generated ID, not their Auth UID.
            val result = leaveCircleUseCase(
                fundId = fundId,
                userId = state.currentMember?.userId ?: state.currentUserId,
                circle = circle
            )
            if (result.isSuccess) {
                setState { copy(showLeaveDialog = false, isActionInProgress = false) }
                sendEffect(SettingsUiEffect.ShowSnackbar(UiText.StringResource(R.string.settings_success_left_circle)))
                sendEffect(SettingsUiEffect.NavigateToHub)
            } else {
                setState {
                    copy(
                        isActionInProgress = false,
                        errorMessage = UiText.StringResource(R.string.settings_error_leave_failed)
                    )
                }
            }
        }
    }

    private fun executeTransferAdmin() {
        val state = uiState.value
        val circle = state.circle ?: return
        val newAdminId = state.selectedNewAdminUserId ?: return
        val newAdminMember = state.activeMembers.firstOrNull { it.userId == newAdminId } ?: return

        setState { copy(isActionInProgress = true) }
        viewModelScope.launch {
            val result = transferAdminOwnershipUseCase(
                fundId = fundId,
                currentAdminUserId = state.currentUserId,
                newAdminUserId = newAdminId,
                newAdminEmail = newAdminMember.email,
                circle = circle
            )
            if (result.isSuccess) {
                setState {
                    copy(
                        showTransferAdminDialog = false,
                        isActionInProgress = false,
                        isAdmin = false
                    )
                }
                sendEffect(SettingsUiEffect.ShowSnackbar(UiText.StringResource(R.string.settings_success_admin_transferred)))
            } else {
                setState {
                    copy(
                        isActionInProgress = false,
                        errorMessage = UiText.StringResource(R.string.settings_error_transfer_failed)
                    )
                }
            }
        }
    }

    private fun executeToggleLock() {
        val state = uiState.value
        val newLockState = !state.isCircleLocked

        setState { copy(isActionInProgress = true) }
        viewModelScope.launch {
            val result = fundRepository.toggleCircleLock(fundId, newLockState)
            if (result.isSuccess) {
                setState {
                    copy(
                        showLockDialog = false,
                        isActionInProgress = false,
                        isCircleLocked = newLockState
                    )
                }
                val msgRes = if (newLockState) {
                    R.string.settings_success_circle_locked
                } else {
                    R.string.settings_success_circle_unlocked
                }
                sendEffect(SettingsUiEffect.ShowSnackbar(UiText.StringResource(msgRes)))
            } else {
                setState {
                    copy(
                        isActionInProgress = false,
                        errorMessage = UiText.StringResource(R.string.settings_error_lock_failed)
                    )
                }
            }
        }
    }

    private fun executeSignOut() {
        setState { copy(isActionInProgress = true) }
        viewModelScope.launch {
            val result = signOutUseCase()
            if (result.isSuccess) {
                setState { copy(showSignOutDialog = false, isActionInProgress = false) }
                sendEffect(SettingsUiEffect.NavigateToAuth)
            } else {
                setState {
                    copy(
                        isActionInProgress = false,
                        errorMessage = UiText.StringResource(R.string.settings_error_sign_out_failed)
                    )
                }
            }
        }
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
    }
}
