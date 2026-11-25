package com.fundcircle.app.presentation.screens.hub

import androidx.lifecycle.viewModelScope
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.data.datasource.local.SessionLocalDataSource
import com.fundcircle.app.domain.usecase.ObserveUserCirclesUseCase
import com.fundcircle.app.domain.usecase.SetActiveCircleUseCase
import com.fundcircle.app.domain.usecase.SignOutUseCase
import com.fundcircle.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CircleHubViewModel @Inject constructor(
    private val observeUserCirclesUseCase: ObserveUserCirclesUseCase,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val setActiveCircleUseCase: SetActiveCircleUseCase,
    private val networkMonitor: NetworkMonitor,
    private val signOutUseCase: SignOutUseCase
) : BaseViewModel<CircleHubUiState, CircleHubUiAction, CircleHubUiEffect>(CircleHubUiState()) {

    private var observeJob: Job? = null

    init {
        observeNetworkState()
        observeCirclesAndSession()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                setState { copy(isOffline = !isOnline) }
            }
        }
    }

    private fun observeCirclesAndSession() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            combine(
                observeUserCirclesUseCase(),
                sessionLocalDataSource.observeUserSession()
            ) { circles, userSession ->
                val currentUserId = userSession?.uid.orEmpty()
                val currentEmail = userSession?.email.orEmpty()
                // Admin is matched by UID (creator) or email (after an ownership transfer).
                val (managed, joined) = circles.partition {
                    (currentUserId.isNotBlank() && it.adminId == currentUserId) ||
                        (currentEmail.isNotBlank() && it.adminEmail.equals(currentEmail, ignoreCase = true))
                }
                Triple(managed, joined, userSession)
            }.catch { error ->
                val errorUiText = error.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.hub_error_loading)
                setState {
                    copy(
                        isLoading = false,
                        errorMessage = errorUiText
                    )
                }
            }.collectLatest { (managed, joined, userSession) ->
                setState {
                    copy(
                        isLoading = false,
                        managedCircles = managed,
                        joinedCircles = joined,
                        totalCircleCount = managed.size + joined.size,
                        activeCircleId = userSession?.activeCircleId,
                        currentUser = userSession
                    )
                }
            }
        }
    }

    override fun onAction(action: CircleHubUiAction) {
        when (action) {
            is CircleHubUiAction.CircleSelected -> handleCircleSelected(action.fundId)
            is CircleHubUiAction.CreateCircleClicked -> handleCreateCircleClicked()
            is CircleHubUiAction.RefreshTriggered -> observeCirclesAndSession()
            is CircleHubUiAction.DismissError -> setState { copy(errorMessage = null) }
            is CircleHubUiAction.SignOutClicked -> handleSignOut()
        }
    }

    private fun handleSignOut() {
        viewModelScope.launch {
            signOutUseCase()
            sendEffect(CircleHubUiEffect.NavigateToAuth)
        }
    }

    private fun handleCircleSelected(fundId: String) {
        viewModelScope.launch {
            setActiveCircleUseCase(fundId)
            sendEffect(CircleHubUiEffect.NavigateToDashboard(fundId))
        }
    }

    private fun handleCreateCircleClicked() {
        if (uiState.value.canCreateMoreCircles) {
            sendEffect(CircleHubUiEffect.NavigateToCreateCircle)
        } else {
            sendEffect(
                CircleHubUiEffect.ShowSnackbar(
                    UiText.StringResource(R.string.hub_limit_reached_toast)
                )
            )
        }
    }
}
