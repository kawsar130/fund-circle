package com.fundcircle.app.presentation.screens.auth

import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.usecase.GetActiveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveAuthStateUseCase
import com.fundcircle.app.domain.usecase.SignInWithGoogleUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val getActiveCircleUseCase: GetActiveCircleUseCase,
    networkMonitor: NetworkMonitor
) : BaseViewModel<AuthUiState, AuthUiAction, AuthUiEffect>(AuthUiState()) {

    init {
        networkMonitor.isOnline
            .onEach { isOnline ->
                setState { copy(isOffline = !isOnline) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val initialSession = observeAuthStateUseCase().first()
            setState { copy(currentUser = initialSession) }
            if (initialSession != null) {
                val activeCircleId = initialSession.activeCircleId ?: getActiveCircleUseCase()
                if (!activeCircleId.isNullOrBlank()) {
                    sendEffect(AuthUiEffect.NavigateToDashboard(activeCircleId))
                } else {
                    sendEffect(AuthUiEffect.NavigateToHub)
                }
            }

            observeAuthStateUseCase()
                .collect { session ->
                    setState { copy(currentUser = session) }
                }
        }
    }

    override fun onAction(action: AuthUiAction) {
        when (action) {
            is AuthUiAction.SignInClicked -> handleSignInClicked()
            is AuthUiAction.GoogleTokenReceived -> handleGoogleTokenReceived(action.idToken)
            is AuthUiAction.GoogleSignInFailed -> handleGoogleSignInFailed(action.error)
            is AuthUiAction.GoogleSignInCancelled -> handleGoogleSignInCancelled()
            is AuthUiAction.DismissError -> setState { copy(errorMessage = null) }
        }
    }

    private fun handleSignInClicked() {
        if (uiState.value.isLoading) return

        if (uiState.value.isOffline) {
            val error = UiText.StringResource(R.string.auth_error_network)
            setState { copy(errorMessage = error) }
            sendEffect(AuthUiEffect.ShowSnackbar(error))
            return
        }

        setState { copy(isLoading = true, errorMessage = null) }
        sendEffect(AuthUiEffect.LaunchGoogleSignIn)
    }

    private fun handleGoogleSignInCancelled() {
        setState { copy(isLoading = false) }
    }

    private fun handleGoogleSignInFailed(error: UiText) {
        setState { copy(isLoading = false, errorMessage = error) }
        sendEffect(AuthUiEffect.ShowSnackbar(error))
    }

    private fun handleGoogleTokenReceived(idToken: String) {
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            signInWithGoogleUseCase(idToken)
                .onSuccess { session ->
                    setState { copy(isLoading = false, currentUser = session) }
                    val activeCircleId = session.activeCircleId ?: getActiveCircleUseCase()
                    if (!activeCircleId.isNullOrBlank()) {
                        sendEffect(AuthUiEffect.NavigateToDashboard(activeCircleId))
                    } else {
                        sendEffect(AuthUiEffect.NavigateToHub)
                    }
                }
                .onFailure { throwable ->
                    val uiText = when (throwable) {
                        is FundCircleError.Network -> UiText.StringResource(R.string.auth_error_network)
                        is FundCircleError.Authorization.SessionExpired -> UiText.StringResource(R.string.auth_error_session_expired)
                        is FundCircleError.Authorization.SignInCancelled -> UiText.StringResource(R.string.auth_error_cancelled)
                        else -> UiText.StringResource(R.string.auth_error_sign_in_failed)
                    }
                    setState { copy(isLoading = false, errorMessage = uiText) }
                    sendEffect(AuthUiEffect.ShowSnackbar(uiText))
                }
        }
    }
}
