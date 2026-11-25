package com.fundcircle.app.presentation.screens.settings

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 13: Settings & Circle Governance (CUJ-08).
 * Single Source of Truth for state, intents, and one-shot effects.
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val fundId: String = "",
    val circle: FundCircle? = null,
    val currentMember: CircleMember? = null,
    val currentUserId: String = "",
    val isAdmin: Boolean = false,
    val isCircleLocked: Boolean = false,
    val activeMembers: List<CircleMember> = emptyList(),
    val showLeaveDialog: Boolean = false,
    val showTransferAdminDialog: Boolean = false,
    val selectedNewAdminUserId: String? = null,
    val showLockDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val isActionInProgress: Boolean = false,
    val darkThemeOverride: Boolean? = null,
    val errorMessage: UiText? = null
) : UiState

sealed interface SettingsUiAction : UiAction {
    data object ClickSwitchCircle : SettingsUiAction
    data object ClickMyProfile : SettingsUiAction
    data class SetDarkTheme(val isDark: Boolean) : SettingsUiAction
    data object OpenLeaveDialog : SettingsUiAction
    data object ConfirmLeaveCircle : SettingsUiAction
    data object OpenTransferAdminDialog : SettingsUiAction
    data class SelectNewAdmin(val userId: String) : SettingsUiAction
    data object ConfirmTransferAdmin : SettingsUiAction
    data object OpenLockDialog : SettingsUiAction
    data object ConfirmToggleCircleLock : SettingsUiAction
    data object OpenSignOutDialog : SettingsUiAction
    data object ConfirmSignOut : SettingsUiAction
    data object DismissDialogs : SettingsUiAction
    data object DismissError : SettingsUiAction
}

sealed interface SettingsUiEffect : UiEffect {
    data object NavigateToHub : SettingsUiEffect
    data class NavigateToProfile(val fundId: String) : SettingsUiEffect
    data object NavigateToAuth : SettingsUiEffect
    data class ShowSnackbar(val message: UiText) : SettingsUiEffect
}
