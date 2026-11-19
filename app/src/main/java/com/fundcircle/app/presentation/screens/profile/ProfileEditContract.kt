package com.fundcircle.app.presentation.screens.profile

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

/**
 * MVI Contract for Screen 11: Edit My Profile / Self-Service KYC (CUJ-07).
 */
data class ProfileEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val fundId: String = "",
    val userId: String = "",
    val circle: FundCircle? = null,
    val member: CircleMember? = null,
    val fullName: String = "",
    val nidNumber: String = "",
    val maskedNid: String = "",
    val nomineeName: String = "",
    val nomineePhone: String = "",
    val nomineeRelation: String = "",
    val nidError: UiText? = null,
    val nomineeNameError: UiText? = null,
    val nomineePhoneError: UiText? = null,
    val nomineeRelationError: UiText? = null,
    val generalError: UiText? = null,
    val isSuccess: Boolean = false
) : UiState

sealed interface ProfileEditUiAction : UiAction {
    data class Init(val fundId: String) : ProfileEditUiAction
    data class FullNameChanged(val name: String) : ProfileEditUiAction
    data class NidChanged(val nid: String) : ProfileEditUiAction
    data class NomineeNameChanged(val name: String) : ProfileEditUiAction
    data class NomineePhoneChanged(val phone: String) : ProfileEditUiAction
    data class NomineeRelationChanged(val relation: String) : ProfileEditUiAction
    data object Save : ProfileEditUiAction
    data object DismissError : ProfileEditUiAction
    data object NavigateBack : ProfileEditUiAction
}

sealed interface ProfileEditUiEffect : UiEffect {
    data object NavigateBack : ProfileEditUiEffect
    data class ShowSnackbar(val message: UiText) : ProfileEditUiEffect
}
