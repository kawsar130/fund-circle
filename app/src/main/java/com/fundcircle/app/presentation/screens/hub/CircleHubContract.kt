package com.fundcircle.app.presentation.screens.hub

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

data class CircleHubUiState(
    val isLoading: Boolean = true,
    val managedCircles: List<FundCircle> = emptyList(),
    val joinedCircles: List<FundCircle> = emptyList(),
    val activeCircleId: String? = null,
    val currentUser: UserSession? = null,
    val totalCircleCount: Int = 0,
    val isOffline: Boolean = false,
    val errorMessage: UiText? = null
) : UiState {
    val canCreateMoreCircles: Boolean get() = totalCircleCount < 5
    val isEmpty: Boolean get() = !isLoading && managedCircles.isEmpty() && joinedCircles.isEmpty()
}

sealed interface CircleHubUiAction : UiAction {
    data class CircleSelected(val fundId: String) : CircleHubUiAction
    data object CreateCircleClicked : CircleHubUiAction
    data object RefreshTriggered : CircleHubUiAction
    data object DismissError : CircleHubUiAction
}

sealed interface CircleHubUiEffect : UiEffect {
    data class NavigateToDashboard(val fundId: String) : CircleHubUiEffect
    data object NavigateToCreateCircle : CircleHubUiEffect
    data class ShowSnackbar(val message: UiText) : CircleHubUiEffect
}
