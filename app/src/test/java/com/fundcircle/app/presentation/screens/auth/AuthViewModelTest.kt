package com.fundcircle.app.presentation.screens.auth

import app.cash.turbine.test
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.usecase.GetActiveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveAuthStateUseCase
import com.fundcircle.app.domain.usecase.SignInWithGoogleUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk(relaxed = true)
    private val observeAuthStateUseCase: ObserveAuthStateUseCase = mockk(relaxed = true)
    private val getActiveCircleUseCase: GetActiveCircleUseCase = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)

    private val isOnlineFlow = MutableStateFlow(true)
    private val authStateFlow = MutableStateFlow<UserSession?>(null)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AuthViewModel

    private val sampleSession = UserSession(
        uid = "user-123",
        email = "user@fundcircle.app",
        displayName = "Sample User",
        photoUrl = null,
        idToken = "valid-token",
        activeCircleId = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { observeAuthStateUseCase() } returns authStateFlow
        coEvery { getActiveCircleUseCase() } returns null

        viewModel = AuthViewModel(
            signInWithGoogleUseCase = signInWithGoogleUseCase,
            observeAuthStateUseCase = observeAuthStateUseCase,
            getActiveCircleUseCase = getActiveCircleUseCase,
            networkMonitor = networkMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdleAndUnauthenticated() {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNull()
        assertThat(state.currentUser).isNull()
        assertThat(state.isOffline).isFalse()
    }

    @Test
    fun networkOffline_updatesUiStateIsOffline() = runTest {
        isOnlineFlow.value = false
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun signInClicked_whenOffline_showsNetworkErrorAndDoesNotLaunchSignIn() = runTest {
        isOnlineFlow.value = false
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(AuthUiAction.SignInClicked)

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AuthUiEffect.ShowSnackbar::class.java)
            val showSnackbar = effect as AuthUiEffect.ShowSnackbar
            assertThat(showSnackbar.message).isEqualTo(UiText.StringResource(R.string.auth_error_network))
            expectNoEvents()
        }

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun signInClicked_whenOnline_triggersLaunchGoogleSignInEffect() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(AuthUiAction.SignInClicked)

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AuthUiEffect.LaunchGoogleSignIn)
            expectNoEvents()
        }

        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun googleSignInCancelled_resetsLoadingState() {
        viewModel.onAction(AuthUiAction.SignInClicked)
        assertThat(viewModel.uiState.value.isLoading).isTrue()

        viewModel.onAction(AuthUiAction.GoogleSignInCancelled)

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun googleSignInFailed_updatesErrorMessageAndEmitsSnackbar() = runTest {
        val errorText = UiText.DynamicString("No account selected")

        viewModel.uiEffect.test {
            viewModel.onAction(AuthUiAction.GoogleSignInFailed(errorText))

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AuthUiEffect.ShowSnackbar(errorText))
            expectNoEvents()
        }

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo(errorText)
    }

    @Test
    fun googleTokenReceived_successWithoutActiveCircle_navigatesToHub() = runTest {
        coEvery { signInWithGoogleUseCase("token-xyz") } returns Result.success(sampleSession)

        viewModel.uiEffect.test {
            viewModel.onAction(AuthUiAction.GoogleTokenReceived("token-xyz"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AuthUiEffect.NavigateToHub)
            expectNoEvents()
        }

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.currentUser).isEqualTo(sampleSession)
    }

    @Test
    fun googleTokenReceived_successWithActiveCircle_navigatesToDashboard() = runTest {
        val sessionWithCircle = sampleSession.copy(activeCircleId = "fund-circle-888")
        coEvery { signInWithGoogleUseCase("token-xyz") } returns Result.success(sessionWithCircle)

        viewModel.uiEffect.test {
            viewModel.onAction(AuthUiAction.GoogleTokenReceived("token-xyz"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(AuthUiEffect.NavigateToDashboard("fund-circle-888"))
            expectNoEvents()
        }

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.currentUser).isEqualTo(sessionWithCircle)
    }

    @Test
    fun googleTokenReceived_failure_mapsErrorAndEmitsSnackbar() = runTest {
        coEvery { signInWithGoogleUseCase(any()) } returns Result.failure(FundCircleError.Network.Timeout)

        viewModel.uiEffect.test {
            viewModel.onAction(AuthUiAction.GoogleTokenReceived("token-xyz"))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(AuthUiEffect.ShowSnackbar::class.java)
            val snackbarEffect = effect as AuthUiEffect.ShowSnackbar
            assertThat(snackbarEffect.message).isEqualTo(UiText.StringResource(R.string.auth_error_network))
            expectNoEvents()
        }

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo(UiText.StringResource(R.string.auth_error_network))
    }

    @Test
    fun dismissError_clearsErrorMessage() {
        viewModel.onAction(AuthUiAction.GoogleSignInFailed(UiText.DynamicString("Error")))
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.onAction(AuthUiAction.DismissError)

        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
