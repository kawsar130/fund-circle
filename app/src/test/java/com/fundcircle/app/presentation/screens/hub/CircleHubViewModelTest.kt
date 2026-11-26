package com.fundcircle.app.presentation.screens.hub

import app.cash.turbine.test
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.data.datasource.local.SessionLocalDataSource
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.usecase.ObserveUserCirclesUseCase
import com.fundcircle.app.domain.usecase.SetActiveCircleUseCase
import com.fundcircle.app.domain.usecase.SignOutUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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
class CircleHubViewModelTest {

    private val observeUserCirclesUseCase: ObserveUserCirclesUseCase = mockk(relaxed = true)
    private val sessionLocalDataSource: SessionLocalDataSource = mockk(relaxed = true)
    private val setActiveCircleUseCase: SetActiveCircleUseCase = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)

    private val isOnlineFlow = MutableStateFlow(true)
    private val circlesFlow = MutableStateFlow<List<FundCircle>>(emptyList())
    private val userSessionFlow = MutableStateFlow<UserSession?>(null)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CircleHubViewModel

    private val sampleUser = UserSession(
        uid = "uid_admin",
        email = "kawsar@example.com",
        displayName = "Kawsar",
        photoUrl = null,
        idToken = "token",
        activeCircleId = "circle-1"
    )

    private val managedCircle = FundCircle(
        id = "circle-1",
        name = "Apex Syndicate",
        description = "Managed circle",
        currency = Currency.BDT,
        adminId = "uid_admin",
        adminEmail = "kawsar@example.com",
        memberEmails = listOf("kawsar@example.com"),
        memberCount = 5,
        totalDeposits = 100000L,
        totalExpenses = 10000L,
        recordedBalance = 90000L,
        isLocked = false,
        createdAt = 1000L
    )

    private val joinedCircle = FundCircle(
        id = "circle-2",
        name = "Friend Syndicate",
        description = "Joined circle",
        currency = Currency.USD,
        adminId = "uid_other",
        adminEmail = "other@example.com",
        memberEmails = listOf("other@example.com", "kawsar@example.com"),
        memberCount = 10,
        totalDeposits = 50000L,
        totalExpenses = 2000L,
        recordedBalance = 48000L,
        isLocked = true,
        createdAt = 2000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { observeUserCirclesUseCase() } returns circlesFlow
        every { sessionLocalDataSource.observeUserSession() } returns userSessionFlow

        userSessionFlow.value = sampleUser
        circlesFlow.value = listOf(managedCircle, joinedCircle)

        viewModel = CircleHubViewModel(
            observeUserCirclesUseCase = observeUserCirclesUseCase,
            sessionLocalDataSource = sessionLocalDataSource,
            setActiveCircleUseCase = setActiveCircleUseCase,
            networkMonitor = networkMonitor,
            signOutUseCase = signOutUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun signOutClicked_onlyAsksForConfirmation() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(CircleHubUiAction.SignOutClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.uiState.value.showSignOutConfirm).isTrue()
            coVerify(exactly = 0) { signOutUseCase() }
            expectNoEvents()
        }
    }

    @Test
    fun dismissSignOut_closesTheDialogWithoutSigningOut() = runTest {
        viewModel.onAction(CircleHubUiAction.SignOutClicked)
        viewModel.onAction(CircleHubUiAction.DismissSignOut)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.showSignOutConfirm).isFalse()
        coVerify(exactly = 0) { signOutUseCase() }
    }

    @Test
    fun confirmSignOut_signsOutAndEmitsNavigateToAuth() = runTest {
        coEvery { signOutUseCase() } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onAction(CircleHubUiAction.SignOutClicked)
            viewModel.onAction(CircleHubUiAction.ConfirmSignOut)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { signOutUseCase() }
            assertThat(awaitItem()).isEqualTo(CircleHubUiEffect.NavigateToAuth)
            assertThat(viewModel.uiState.value.showSignOutConfirm).isFalse()
            assertThat(viewModel.uiState.value.isSigningOut).isFalse()
        }
    }

    @Test
    fun confirmSignOut_whenSignOutFails_staysOnHubAndShowsAnError() = runTest {
        coEvery { signOutUseCase() } returns Result.failure(IllegalStateException("boom"))

        viewModel.uiEffect.test {
            viewModel.onAction(CircleHubUiAction.SignOutClicked)
            viewModel.onAction(CircleHubUiAction.ConfirmSignOut)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(
                CircleHubUiEffect.ShowSnackbar(UiText.StringResource(R.string.hub_sign_out_failed))
            )
            expectNoEvents()
            assertThat(viewModel.uiState.value.showSignOutConfirm).isFalse()
        }
    }

    @Test
    fun confirmSignOut_tappedTwice_signsOutOnce() = runTest {
        coEvery { signOutUseCase() } returns Result.success(Unit)

        viewModel.onAction(CircleHubUiAction.SignOutClicked)
        viewModel.onAction(CircleHubUiAction.ConfirmSignOut)
        viewModel.onAction(CircleHubUiAction.ConfirmSignOut)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { signOutUseCase() }
    }

    @Test
    fun circleTransferredToUserByEmail_isListedAsManaged() = runTest {
        val transferred = joinedCircle.copy(id = "circle-3", adminId = "invite-uuid", adminEmail = "Kawsar@Example.com")
        circlesFlow.value = listOf(managedCircle, joinedCircle, transferred)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.managedCircles).containsExactly(managedCircle, transferred)
        assertThat(state.joinedCircles).containsExactly(joinedCircle)
    }

    @Test
    fun initialState_segregatesManagedAndJoinedCircles() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.managedCircles).containsExactly(managedCircle)
        assertThat(state.joinedCircles).containsExactly(joinedCircle)
        assertThat(state.totalCircleCount).isEqualTo(2)
        assertThat(state.activeCircleId).isEqualTo("circle-1")
        assertThat(state.canCreateMoreCircles).isTrue()
        assertThat(state.isEmpty).isFalse()
    }

    @Test
    fun networkOffline_updatesIsOfflineState() = runTest {
        isOnlineFlow.value = false
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun circleSelected_persistsActiveCircleAndNavigatesToDashboard() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { setActiveCircleUseCase("circle-2") } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onAction(CircleHubUiAction.CircleSelected("circle-2"))

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CircleHubUiEffect.NavigateToDashboard("circle-2"))
            coVerify(exactly = 1) { setActiveCircleUseCase("circle-2") }
        }
    }

    @Test
    fun createCircleClicked_whenUnder5Circles_emitsNavigateToCreateCircle() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(CircleHubUiAction.CreateCircleClicked)

            val effect = awaitItem()
            assertThat(effect).isEqualTo(CircleHubUiEffect.NavigateToCreateCircle)
        }
    }

    @Test
    fun createCircleClicked_when5CirclesExist_emitsLimitReachedSnackbar() = runTest {
        val fiveCircles = List(5) { i ->
            managedCircle.copy(id = "c-$i")
        }
        circlesFlow.value = fiveCircles
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.totalCircleCount).isEqualTo(5)
        assertThat(viewModel.uiState.value.canCreateMoreCircles).isFalse()

        viewModel.uiEffect.test {
            viewModel.onAction(CircleHubUiAction.CreateCircleClicked)

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(CircleHubUiEffect.ShowSnackbar::class.java)
            val snackbar = effect as CircleHubUiEffect.ShowSnackbar
            assertThat(snackbar.message).isEqualTo(UiText.StringResource(R.string.hub_limit_reached_toast))
        }
    }

    @Test
    fun dismissError_clearsErrorMessage() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(CircleHubUiAction.DismissError)
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun refreshTriggered_reloadsCirclesAndSession() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedManagedCircle = managedCircle.copy(name = "Updated Apex")
        circlesFlow.value = listOf(updatedManagedCircle)

        viewModel.onAction(CircleHubUiAction.RefreshTriggered)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.managedCircles).containsExactly(updatedManagedCircle)
    }
}
