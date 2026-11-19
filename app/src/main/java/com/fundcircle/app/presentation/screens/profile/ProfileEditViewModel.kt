package com.fundcircle.app.presentation.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.security.PrivacyMaskUtils
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.GetMemberKycUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveMemberUseCase
import com.fundcircle.app.domain.usecase.UpdateMemberKycUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val getMemberKycUseCase: GetMemberKycUseCase,
    private val updateMemberKycUseCase: UpdateMemberKycUseCase,
    private val observeCircleUseCase: ObserveCircleUseCase,
    private val observeMemberUseCase: ObserveMemberUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<ProfileEditUiState, ProfileEditUiAction, ProfileEditUiEffect>(
    ProfileEditUiState()
) {
    init {
        val fundId: String? = savedStateHandle[KEY_FUND_ID]
        viewModelScope.launch {
            val resolvedFundId = fundId?.ifBlank { null } ?: authRepository.getActiveCircleId().orEmpty()
            if (resolvedFundId.isNotBlank()) {
                loadProfile(resolvedFundId)
            } else {
                setState { copy(isLoading = false) }
            }
        }
    }

    override fun onAction(action: ProfileEditUiAction) {
        when (action) {
            is ProfileEditUiAction.Init -> loadProfile(action.fundId)
            is ProfileEditUiAction.FullNameChanged -> {
                setState { copy(fullName = action.name) }
            }
            is ProfileEditUiAction.NidChanged -> {
                val digitsOnly = action.nid.filter { it.isDigit() }.take(17)
                setState {
                    copy(
                        nidNumber = digitsOnly,
                        nidError = if (digitsOnly.length in setOf(10, 13, 17)) null else nidError
                    )
                }
            }
            is ProfileEditUiAction.NomineeNameChanged -> {
                setState {
                    copy(
                        nomineeName = action.name,
                        nomineeNameError = if (action.name.trim().length in 3..100) null else nomineeNameError
                    )
                }
            }
            is ProfileEditUiAction.NomineePhoneChanged -> {
                val digitsOnly = action.phone.filter { it.isDigit() }.take(11)
                setState {
                    copy(
                        nomineePhone = digitsOnly,
                        nomineePhoneError = if (digitsOnly.length == 11) null else nomineePhoneError
                    )
                }
            }
            is ProfileEditUiAction.NomineeRelationChanged -> {
                setState {
                    copy(
                        nomineeRelation = action.relation,
                        nomineeRelationError = if (action.relation.isNotBlank()) null else nomineeRelationError
                    )
                }
            }
            is ProfileEditUiAction.Save -> saveProfile()
            is ProfileEditUiAction.DismissError -> {
                setState { copy(generalError = null) }
            }
            is ProfileEditUiAction.NavigateBack -> {
                sendEffect(ProfileEditUiEffect.NavigateBack)
            }
        }
    }

    private fun loadProfile(fundId: String) {
        if (fundId.isBlank()) return
        setState { copy(fundId = fundId, isLoading = true, generalError = null) }

        viewModelScope.launch {
            try {
                val session = authRepository.userSession.first()
                val currentUid = session?.uid.orEmpty()

                val circle = try {
                    observeCircleUseCase(fundId).first()
                } catch (_: Exception) {
                    null
                }

                val member = try {
                    observeMemberUseCase(fundId, currentUid).first()
                } catch (_: Exception) {
                    null
                }

                val kycResult = getMemberKycUseCase(fundId, currentUid)
                val kyc = kycResult.getOrNull()

                setState {
                    copy(
                        isLoading = false,
                        fundId = fundId,
                        userId = currentUid,
                        circle = circle,
                        member = member,
                        fullName = member?.name ?: session?.displayName.orEmpty(),
                        nidNumber = kyc?.nidNumber.orEmpty(),
                        maskedNid = kyc?.maskedNid ?: PrivacyMaskUtils.maskNid(kyc?.nidNumber.orEmpty()),
                        nomineeName = kyc?.nomineeName.orEmpty(),
                        nomineePhone = kyc?.nomineePhone.orEmpty(),
                        nomineeRelation = kyc?.nomineeRelation.orEmpty()
                    )
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        generalError = UiText.StringResource(R.string.profile_edit_error_loading)
                    )
                }
            }
        }
    }

    private fun saveProfile() {
        val currentState = uiState.value
        val trimmedNid = currentState.nidNumber.trim()
        val trimmedNomineeName = currentState.nomineeName.trim()
        val trimmedNomineePhone = currentState.nomineePhone.trim()
        val trimmedNomineeRelation = currentState.nomineeRelation.trim()

        var hasError = false
        var nidErr: UiText? = null
        var nomineeNameErr: UiText? = null
        var nomineePhoneErr: UiText? = null
        var nomineeRelationErr: UiText? = null

        val validNidLengths = setOf(10, 13, 17)
        if (!trimmedNid.all { it.isDigit() } || trimmedNid.length !in validNidLengths) {
            nidErr = UiText.StringResource(R.string.profile_edit_nid_error)
            hasError = true
        }

        if (trimmedNomineeName.length !in 3..100) {
            nomineeNameErr = UiText.StringResource(R.string.profile_edit_nominee_name_error)
            hasError = true
        }

        if (trimmedNomineePhone.length != 11 || !trimmedNomineePhone.all { it.isDigit() }) {
            nomineePhoneErr = UiText.StringResource(R.string.profile_edit_nominee_phone_error)
            hasError = true
        }

        if (trimmedNomineeRelation.isBlank()) {
            nomineeRelationErr = UiText.StringResource(R.string.profile_edit_relation_error)
            hasError = true
        }

        if (hasError) {
            setState {
                copy(
                    nidError = nidErr,
                    nomineeNameError = nomineeNameErr,
                    nomineePhoneError = nomineePhoneErr,
                    nomineeRelationError = nomineeRelationErr
                )
            }
            return
        }

        setState {
            copy(
                isSaving = true,
                nidError = null,
                nomineeNameError = null,
                nomineePhoneError = null,
                nomineeRelationError = null,
                generalError = null
            )
        }

        val updatedKyc = MemberKyc(
            userId = currentState.userId,
            nidNumber = trimmedNid,
            maskedNid = PrivacyMaskUtils.maskNid(trimmedNid),
            nomineeName = trimmedNomineeName,
            nomineePhone = trimmedNomineePhone,
            nomineeRelation = trimmedNomineeRelation,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            updateMemberKycUseCase(currentState.fundId, updatedKyc).fold(
                onSuccess = {
                    setState {
                        copy(
                            isSaving = false,
                            isSuccess = true,
                            maskedNid = updatedKyc.maskedNid
                        )
                    }
                    sendEffect(ProfileEditUiEffect.ShowSnackbar(UiText.StringResource(R.string.profile_edit_save_success)))
                    sendEffect(ProfileEditUiEffect.NavigateBack)
                },
                onFailure = { error ->
                    setState {
                        copy(
                            isSaving = false,
                            generalError = mapErrorToUiText(error)
                        )
                    }
                }
            )
        }
    }

    private fun mapErrorToUiText(error: Throwable): UiText = when (error) {
        is FundCircleError.Authorization -> UiText.StringResource(R.string.error_not_authorized)
        is FundCircleError.Validation.InvalidNid -> UiText.StringResource(R.string.profile_edit_nid_error)
        is FundCircleError.Validation.InvalidNomineeName -> UiText.StringResource(R.string.profile_edit_nominee_name_error)
        is FundCircleError.Validation.InvalidPhone -> UiText.StringResource(R.string.profile_edit_nominee_phone_error)
        is FundCircleError.Validation.InvalidNomineeRelation -> UiText.StringResource(R.string.profile_edit_relation_error)
        is FundCircleError.Network -> UiText.StringResource(R.string.dashboard_error_loading)
        else -> error.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.profile_edit_error_general)
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
    }
}
