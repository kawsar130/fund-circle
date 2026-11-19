package com.fundcircle.app.presentation.screens.members.components

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 10: Add Stakeholder Modal Bottom Sheet.
 */
data class AddStakeholderUiState(
    val fundId: String = "",
    val allocatedMemberId: String = "FC-001",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val nameError: UiText? = null,
    val phoneError: UiText? = null,
    val emailError: UiText? = null,
    val generalError: UiText? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
) : UiState

sealed interface AddStakeholderUiAction : UiAction {
    data class Init(val fundId: String) : AddStakeholderUiAction
    data class NameChanged(val name: String) : AddStakeholderUiAction
    data class PhoneChanged(val phone: String) : AddStakeholderUiAction
    data class EmailChanged(val email: String) : AddStakeholderUiAction
    data object Submit : AddStakeholderUiAction
    data object DismissError : AddStakeholderUiAction
}

sealed interface AddStakeholderUiEffect : UiEffect {
    data class MemberAdded(val memberName: String, val memberId: String) : AddStakeholderUiEffect
    data class ShowSnackbar(val message: UiText) : AddStakeholderUiEffect
}
