package com.fundcircle.app.presentation.screens.create

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

data class CreateCircleUiState(
    val name: String = "",
    val currency: Currency = Currency.BDT,
    val description: String = "",
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val nameError: UiText? = null,
    val descriptionError: UiText? = null,
    val generalError: UiText? = null
) : UiState {
    val isCreateEnabled: Boolean
        get() = name.trim().length in 3..50 &&
                (description.isBlank() || description.length <= 250) &&
                !isLoading
}

sealed interface CreateCircleUiAction : UiAction {
    data class NameChanged(val name: String) : CreateCircleUiAction
    data class CurrencySelected(val currency: Currency) : CreateCircleUiAction
    data class DescriptionChanged(val description: String) : CreateCircleUiAction
    data object CreateClicked : CreateCircleUiAction
    data object DismissError : CreateCircleUiAction
    data object BackClicked : CreateCircleUiAction
}

sealed interface CreateCircleUiEffect : UiEffect {
    data class NavigateToDashboard(val fundId: String) : CreateCircleUiEffect
    data object NavigateBack : CreateCircleUiEffect
    data class ShowSnackbar(val message: UiText) : CreateCircleUiEffect
}
