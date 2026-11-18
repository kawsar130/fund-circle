package com.fundcircle.app.presentation.screens.create

import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.usecase.CreateCircleUseCase
import com.fundcircle.app.domain.usecase.SetActiveCircleUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateCircleViewModel @Inject constructor(
    private val createCircleUseCase: CreateCircleUseCase,
    private val setActiveCircleUseCase: SetActiveCircleUseCase,
    networkMonitor: NetworkMonitor
) : BaseViewModel<CreateCircleUiState, CreateCircleUiAction, CreateCircleUiEffect>(CreateCircleUiState()) {

    init {
        networkMonitor.isOnline
            .onEach { isOnline -> setState { copy(isOffline = !isOnline) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: CreateCircleUiAction) {
        when (action) {
            is CreateCircleUiAction.NameChanged -> {
                val trimmed = action.name
                val nameError = if (trimmed.isNotBlank() && (trimmed.trim().length < 3 || trimmed.trim().length > 50)) {
                    UiText.StringResource(R.string.create_circle_name_error)
                } else null
                setState { copy(name = action.name, nameError = nameError, generalError = null) }
            }

            is CreateCircleUiAction.CurrencySelected -> {
                setState { copy(currency = action.currency) }
            }

            is CreateCircleUiAction.DescriptionChanged -> {
                val descError = if (action.description.length > 250) {
                    UiText.StringResource(R.string.create_circle_description_error)
                } else null
                setState { copy(description = action.description, descriptionError = descError) }
            }

            is CreateCircleUiAction.CreateClicked -> handleCreateClicked()
            is CreateCircleUiAction.DismissError -> setState { copy(generalError = null) }
            is CreateCircleUiAction.BackClicked -> sendEffect(CreateCircleUiEffect.NavigateBack)
        }
    }

    private fun handleCreateClicked() {
        val currentState = uiState.value
        val trimmedName = currentState.name.trim()

        if (trimmedName.length !in 3..50) {
            setState { copy(nameError = UiText.StringResource(R.string.create_circle_name_error)) }
            return
        }

        if (currentState.description.length > 250) {
            setState { copy(descriptionError = UiText.StringResource(R.string.create_circle_description_error)) }
            return
        }

        setState { copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            val result = createCircleUseCase(
                name = trimmedName,
                currency = currentState.currency,
                description = currentState.description.trim().ifBlank { null }
            )

            result.fold(
                onSuccess = { fundId ->
                    setActiveCircleUseCase(fundId)
                    setState { copy(isLoading = false) }
                    sendEffect(CreateCircleUiEffect.NavigateToDashboard(fundId))
                },
                onFailure = { error ->
                    val errorMessage = when (error) {
                        is FundCircleError.Business.CircleLimitReached -> {
                            UiText.StringResource(R.string.circle_error_limit_reached)
                        }
                        is FundCircleError.Validation.InvalidName -> {
                            UiText.StringResource(R.string.create_circle_name_error)
                        }
                        is FundCircleError.Validation.DescriptionTooLong -> {
                            UiText.StringResource(R.string.create_circle_description_error)
                        }
                        is FundCircleError.Conflict.CurrencyLocked -> {
                            UiText.StringResource(R.string.circle_error_currency_locked)
                        }
                        is FundCircleError.Network.Timeout -> {
                            UiText.StringResource(R.string.auth_error_network)
                        }
                        else -> {
                            error.message?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.circle_error_generic)
                        }
                    }
                    setState { copy(isLoading = false, generalError = errorMessage) }
                    sendEffect(CreateCircleUiEffect.ShowSnackbar(errorMessage))
                }
            )
        }
    }
}
