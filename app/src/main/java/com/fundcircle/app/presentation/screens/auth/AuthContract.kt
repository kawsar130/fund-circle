package com.fundcircle.app.presentation.screens.auth

import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.presentation.common.UiAction
import com.fundcircle.app.presentation.common.UiEffect
import com.fundcircle.app.presentation.common.UiState

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val currentUser: UserSession? = null,
    val isOffline: Boolean = false
) : UiState

sealed interface AuthUiAction : UiAction {
    data object SignInClicked : AuthUiAction
    data class GoogleTokenReceived(val idToken: String) : AuthUiAction
    data class GoogleSignInFailed(val error: UiText) : AuthUiAction
    data object GoogleSignInCancelled : AuthUiAction
    data object DismissError : AuthUiAction
}

sealed interface AuthUiEffect : UiEffect {
    data object LaunchGoogleSignIn : AuthUiEffect
    data object NavigateToHub : AuthUiEffect
    data class NavigateToDashboard(val fundId: String) : AuthUiEffect
    data class ShowSnackbar(val message: UiText) : AuthUiEffect
}
