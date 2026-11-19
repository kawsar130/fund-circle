package com.fundcircle.app.presentation.screens.members.components

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.MemberRepository
import com.fundcircle.app.domain.usecase.AddMemberUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStakeholderViewModel @Inject constructor(
    private val addMemberUseCase: AddMemberUseCase,
    private val memberRepository: MemberRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<AddStakeholderUiState, AddStakeholderUiAction, AddStakeholderUiEffect>(
    AddStakeholderUiState()
) {
    init {
        val fundId: String? = savedStateHandle[KEY_FUND_ID]
        if (!fundId.isNullOrBlank()) {
            initFund(fundId)
        }
    }

    override fun onAction(action: AddStakeholderUiAction) {
        when (action) {
            is AddStakeholderUiAction.Init -> initFund(action.fundId)
            is AddStakeholderUiAction.NameChanged -> {
                setState {
                    copy(
                        name = action.name,
                        nameError = if (action.name.isNotBlank() && action.name.trim().length in 2..100) null else nameError
                    )
                }
            }
            is AddStakeholderUiAction.PhoneChanged -> {
                val digitsOnly = action.phone.filter { it.isDigit() }.take(11)
                setState {
                    copy(
                        phone = digitsOnly,
                        phoneError = if (digitsOnly.length == 11) null else phoneError
                    )
                }
            }
            is AddStakeholderUiAction.EmailChanged -> {
                setState {
                    copy(
                        email = action.email,
                        emailError = null
                    )
                }
            }
            is AddStakeholderUiAction.Submit -> submit()
            is AddStakeholderUiAction.DismissError -> {
                setState { copy(generalError = null) }
            }
        }
    }

    fun initFund(fundId: String) {
        if (fundId.isBlank()) return
        setState { copy(fundId = fundId) }
        viewModelScope.launch {
            memberRepository.observeMembers(fundId)
                .catch { /* ignore in preview calculation */ }
                .collect { members ->
                    val allocatedId = AddMemberUseCase.computeNextMemberId(members)
                    setState { copy(allocatedMemberId = allocatedId) }
                }
        }
    }

    private fun submit() {
        val currentState = uiState.value
        val trimmedName = currentState.name.trim()
        val trimmedPhone = currentState.phone.trim()
        val trimmedEmail = currentState.email.trim()

        var hasError = false
        var nameErr: UiText? = null
        var phoneErr: UiText? = null
        var emailErr: UiText? = null

        if (trimmedName.length !in 2..100) {
            nameErr = UiText.StringResource(R.string.add_member_sheet_name_error)
            hasError = true
        }

        if (trimmedPhone.length != 11 || !trimmedPhone.all { it.isDigit() }) {
            phoneErr = UiText.StringResource(R.string.add_member_sheet_phone_error)
            hasError = true
        }

        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(trimmedEmail)) {
            emailErr = UiText.StringResource(R.string.add_member_sheet_email_error)
            hasError = true
        }

        if (hasError) {
            setState {
                copy(
                    nameError = nameErr,
                    phoneError = phoneErr,
                    emailError = emailErr
                )
            }
            return
        }

        setState {
            copy(
                isSubmitting = true,
                nameError = null,
                phoneError = null,
                emailError = null,
                generalError = null
            )
        }

        viewModelScope.launch {
            addMemberUseCase(
                fundId = currentState.fundId,
                name = trimmedName,
                phone = trimmedPhone,
                email = trimmedEmail
            ).fold(
                onSuccess = {
                    setState { copy(isSubmitting = false, isSuccess = true) }
                    sendEffect(
                        AddStakeholderUiEffect.MemberAdded(
                            memberName = trimmedName,
                            memberId = currentState.allocatedMemberId
                        )
                    )
                    sendEffect(
                        AddStakeholderUiEffect.ShowSnackbar(
                            UiText.StringResource(
                                R.string.add_member_sheet_success,
                                trimmedName,
                                currentState.allocatedMemberId
                            )
                        )
                    )
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

    private fun mapErrorToUiText(error: Throwable): UiText = when (error) {
        is FundCircleError.Authorization -> UiText.StringResource(R.string.error_not_authorized)
        is FundCircleError.Validation.MemberNameRequired -> UiText.StringResource(R.string.add_member_sheet_name_error)
        is FundCircleError.Validation.InvalidPhone -> UiText.StringResource(R.string.add_member_sheet_phone_error)
        is FundCircleError.Validation.InvalidEmail -> UiText.StringResource(R.string.add_member_sheet_email_error)
        is FundCircleError.Conflict.DuplicateEntry -> UiText.StringResource(R.string.add_member_sheet_error_duplicate_email)
        is FundCircleError.Network -> UiText.StringResource(R.string.dashboard_error_loading)
        else -> error.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.dashboard_error_loading)
    }

    companion object {
        const val KEY_FUND_ID = "fundId"
    }
}
