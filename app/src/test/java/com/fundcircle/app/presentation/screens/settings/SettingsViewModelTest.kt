package com.fundcircle.app.presentation.screens.settings

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.core.theme.ThemePreferenceManager
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.repository.FundRepository
import com.fundcircle.app.domain.usecase.LeaveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.SignOutUseCase
import com.fundcircle.app.domain.usecase.TransferAdminOwnershipUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase = mockk(relaxed = true)
    private val fundRepository: FundRepository = mockk(relaxed = true)
    private val leaveCircleUseCase: LeaveCircleUseCase = mockk(relaxed = true)
    private val transferAdminOwnershipUseCase: TransferAdminOwnershipUseCase = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val themePreferenceManager: ThemePreferenceManager = mockk(relaxed = true)
    private val darkThemeOverrideFlow = MutableStateFlow<Boolean?>(null)

    private val testDispatcher = StandardTestDispatcher()
    private val testFundId = "circle-123"

    private val sampleAdminSession = UserSession(
        uid = "user-admin-1",
        email = "admin@example.com",
        displayName = "Admin User",
        photoUrl = null,
        idToken = "token",
        activeCircleId = testFundId
    )

    private val sampleMemberSession = UserSession(
        uid = "user-member-2",
        email = "member@example.com",
        displayName = "Member User",
        photoUrl = null,
        idToken = "token",
        activeCircleId = testFundId
    )

    private val sampleCircle = FundCircle(
        id = testFundId,
        name = "Green Valley Syndicate",
        currency = Currency.BDT,
        description = "Pooled capital",
        adminId = "user-admin-1",
        adminEmail = "admin@example.com",
        memberEmails = listOf("admin@example.com", "member@example.com"),
        memberCount = 2,
        totalDeposits = 1000000L,
        totalExpenses = 100000L,
        recordedBalance = 900000L,
        isLocked = false,
        createdAt = 1700000000000L
    )

    private val adminMember = CircleMember(
        userId = "user-admin-1",
        memberId = "FC-001",
        name = "Kawsar Admin",
        email = "admin@example.com",
        role = MemberRole.ADMIN,
        status = MemberStatus.ACTIVE,
        totalContributed = 600000L,
        joinedAt = 1700000000000L,
        lastActiveAt = 1700000000000L
    )

    private val regularMember = CircleMember(
        userId = "user-member-2",
        memberId = "FC-002",
        name = "Tariqul Member",
        email = "member@example.com",
        role = MemberRole.MEMBER,
        status = MemberStatus.ACTIVE,
        totalContributed = 400000L,
        joinedAt = 1700000000000L,
        lastActiveAt = 1700000000000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { observeCircleUseCase(testFundId) } returns flowOf(sampleCircle)
        every { observeCircleMembersUseCase(testFundId) } returns flowOf(listOf(adminMember, regularMember))
        every { authRepository.userSession } returns flowOf(sampleAdminSession)
        every { themePreferenceManager.darkThemeOverride } returns darkThemeOverrideFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("fundId" to testFundId))
        return SettingsViewModel(
            savedStateHandle = savedStateHandle,
            observeCircleUseCase = observeCircleUseCase,
            observeCircleMembersUseCase = observeCircleMembersUseCase,
            fundRepository = fundRepository,
            leaveCircleUseCase = leaveCircleUseCase,
            transferAdminOwnershipUseCase = transferAdminOwnershipUseCase,
            signOutUseCase = signOutUseCase,
            authRepository = authRepository,
            themePreferenceManager = themePreferenceManager
        )
    }

    @Test
    fun `initial load as Admin sets isAdmin true and populates active members for transfer`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isAdmin).isTrue()
        assertThat(state.currentMember).isEqualTo(adminMember)
        assertThat(state.isCircleLocked).isFalse()
        // Active members excluding current user (admin)
        assertThat(state.activeMembers).containsExactly(regularMember)
    }

    @Test
    fun `initial load as Stakeholder sets isAdmin false`() = runTest {
        every { authRepository.userSession } returns flowOf(sampleMemberSession)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isAdmin).isFalse()
        assertThat(state.currentMember).isEqualTo(regularMember)
    }

    @Test
    fun `ClickSwitchCircle sends NavigateToHub effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ClickSwitchCircle)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(SettingsUiEffect.NavigateToHub)
        }
    }

    @Test
    fun `ClickMyProfile sends NavigateToProfile effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ClickMyProfile)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(SettingsUiEffect.NavigateToProfile(testFundId))
        }
    }

    @Test
    fun `Admin attempting to leave circle is blocked with warning snackbar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.OpenLeaveDialog)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(SettingsUiEffect.ShowSnackbar::class.java)
            assertThat(viewModel.uiState.value.showLeaveDialog).isFalse()
        }
    }

    @Test
    fun `Stakeholder leaving circle calls leaveCircleUseCase and navigates to hub on success`() = runTest {
        every { authRepository.userSession } returns flowOf(sampleMemberSession)
        coEvery { leaveCircleUseCase(testFundId, "user-member-2", sampleCircle) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SettingsUiAction.OpenLeaveDialog)
        assertThat(viewModel.uiState.value.showLeaveDialog).isTrue()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ConfirmLeaveCircle)
            advanceUntilIdle()

            val snackbarEffect = awaitItem()
            assertThat(snackbarEffect).isInstanceOf(SettingsUiEffect.ShowSnackbar::class.java)
            val navEffect = awaitItem()
            assertThat(navEffect).isEqualTo(SettingsUiEffect.NavigateToHub)
        }

        coVerify(exactly = 1) { leaveCircleUseCase(testFundId, "user-member-2", sampleCircle) }
    }

    @Test
    fun `Invited stakeholder leaves using their member document id, not their auth uid`() = runTest {
        val invitedMember = regularMember.copy(userId = "invite-uuid-9")
        every { observeCircleMembersUseCase(testFundId) } returns flowOf(listOf(adminMember, invitedMember))
        every { authRepository.userSession } returns flowOf(sampleMemberSession)
        coEvery { leaveCircleUseCase(testFundId, "invite-uuid-9", sampleCircle) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SettingsUiAction.OpenLeaveDialog)
        viewModel.onAction(SettingsUiAction.ConfirmLeaveCircle)
        advanceUntilIdle()

        coVerify(exactly = 1) { leaveCircleUseCase(testFundId, "invite-uuid-9", sampleCircle) }
    }

    @Test
    fun `Transfer Admin Ownership executes atomic transfer and notifies user`() = runTest {
        coEvery {
            transferAdminOwnershipUseCase(
                testFundId,
                "user-admin-1",
                "user-member-2",
                "member@example.com",
                sampleCircle
            )
        } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SettingsUiAction.OpenTransferAdminDialog)
        assertThat(viewModel.uiState.value.showTransferAdminDialog).isTrue()
        assertThat(viewModel.uiState.value.selectedNewAdminUserId).isEqualTo("user-member-2")

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ConfirmTransferAdmin)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(SettingsUiEffect.ShowSnackbar::class.java)
            assertThat(viewModel.uiState.value.showTransferAdminDialog).isFalse()
            assertThat(viewModel.uiState.value.isAdmin).isFalse()
        }

        coVerify(exactly = 1) {
            transferAdminOwnershipUseCase(
                testFundId,
                "user-admin-1",
                "user-member-2",
                "member@example.com",
                sampleCircle
            )
        }
    }

    @Test
    fun `Toggle circle lock calls fundRepository and updates isCircleLocked`() = runTest {
        coEvery { fundRepository.toggleCircleLock(testFundId, true) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SettingsUiAction.OpenLockDialog)
        assertThat(viewModel.uiState.value.showLockDialog).isTrue()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ConfirmToggleCircleLock)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(SettingsUiEffect.ShowSnackbar::class.java)
            assertThat(viewModel.uiState.value.isCircleLocked).isTrue()
            assertThat(viewModel.uiState.value.showLockDialog).isFalse()
        }

        coVerify(exactly = 1) { fundRepository.toggleCircleLock(testFundId, true) }
    }

    @Test
    fun `Sign Out calls signOutUseCase and navigates to Auth`() = runTest {
        coEvery { signOutUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(SettingsUiAction.OpenSignOutDialog)
        assertThat(viewModel.uiState.value.showSignOutDialog).isTrue()

        viewModel.uiEffect.test {
            viewModel.onAction(SettingsUiAction.ConfirmSignOut)
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(SettingsUiEffect.NavigateToAuth)
            assertThat(viewModel.uiState.value.showSignOutDialog).isFalse()
        }

        coVerify(exactly = 1) { signOutUseCase() }
    }

    @Test
    fun `SetDarkTheme delegates to ThemePreferenceManager and updates uiState override`() = runTest {
        every { themePreferenceManager.setDarkTheme(any()) } answers {
            darkThemeOverrideFlow.value = firstArg()
        }

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.darkThemeOverride).isNull()

        viewModel.onAction(SettingsUiAction.SetDarkTheme(true))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.darkThemeOverride).isTrue()

        viewModel.onAction(SettingsUiAction.SetDarkTheme(false))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.darkThemeOverride).isFalse()

        verify(exactly = 1) { themePreferenceManager.setDarkTheme(true) }
        verify(exactly = 1) { themePreferenceManager.setDarkTheme(false) }
    }
}
