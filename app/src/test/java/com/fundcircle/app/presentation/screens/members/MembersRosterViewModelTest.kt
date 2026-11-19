package com.fundcircle.app.presentation.screens.members

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
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
class MembersRosterViewModelTest {

    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase = mockk(relaxed = true)
    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val observeDepositsUseCase: ObserveDepositsUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val fundId = "fund-test-101"
    private val isOnlineFlow = MutableStateFlow(true)
    private val userSessionFlow = MutableStateFlow<UserSession?>(null)

    private val sampleAdmin = UserSession(
        uid = "usr-admin",
        email = "admin@example.com",
        displayName = "Admin",
        photoUrl = null,
        idToken = "token",
        activeCircleId = fundId
    )

    private val sampleCircle = FundCircle(
        id = fundId,
        name = "Apex Syndicate",
        description = null,
        currency = Currency.BDT,
        adminId = "usr-admin",
        adminEmail = "admin@example.com",
        memberEmails = listOf("admin@example.com", "rahim@example.com"),
        memberCount = 2,
        totalDeposits = 500_000L,
        totalExpenses = 0L,
        recordedBalance = 500_000L,
        isLocked = false,
        createdAt = 1000L
    )

    private val sampleMembers = listOf(
        CircleMember(
            userId = "usr-admin",
            memberId = "FC-001",
            email = "admin@example.com",
            name = "Kawsar Admin",
            phone = "01711111111",
            role = MemberRole.ADMIN,
            status = MemberStatus.ACTIVE,
            totalContributed = 300_000L,
            joinedAt = 1000L,
            lastActiveAt = 2000L
        ),
        CircleMember(
            userId = "usr-2",
            memberId = "FC-002",
            email = "rahim@example.com",
            name = "Rahim Uddin",
            phone = "01722222222",
            role = MemberRole.MEMBER,
            status = MemberStatus.ACTIVE,
            totalContributed = 200_000L,
            joinedAt = 1500L,
            lastActiveAt = 2500L
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { authRepository.userSession } returns userSessionFlow
        every { observeCircleUseCase(fundId) } returns flowOf(sampleCircle)
        every { observeCircleMembersUseCase(fundId) } returns flowOf(sampleMembers)
        every { observeDepositsUseCase(fundId) } returns flowOf(emptyList())
        userSessionFlow.value = sampleAdmin
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(initialFundId: String = fundId): MembersRosterViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("fundId" to initialFundId))
        return MembersRosterViewModel(
            observeCircleMembersUseCase = observeCircleMembersUseCase,
            observeCircleUseCase = observeCircleUseCase,
            observeDepositsUseCase = observeDepositsUseCase,
            authRepository = authRepository,
            networkMonitor = networkMonitor,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun init_loadsCircleAndMembers_determinesAdminStatus() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.members).hasSize(2)
        assertThat(state.filteredMembers).hasSize(2)
        assertThat(state.isAdmin).isTrue()
        assertThat(state.circle?.name).isEqualTo("Apex Syndicate")
    }

    @Test
    fun search_filtersByNamePhoneAndMemberId() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Filter by name
        viewModel.onAction(MembersRosterUiAction.SearchQueryChanged("Rahim"))
        assertThat(viewModel.uiState.value.filteredMembers).hasSize(1)
        assertThat(viewModel.uiState.value.filteredMembers[0].name).isEqualTo("Rahim Uddin")

        // Filter by phone
        viewModel.onAction(MembersRosterUiAction.SearchQueryChanged("017111"))
        assertThat(viewModel.uiState.value.filteredMembers).hasSize(1)
        assertThat(viewModel.uiState.value.filteredMembers[0].userId).isEqualTo("usr-admin")

        // Filter by Member ID
        viewModel.onAction(MembersRosterUiAction.SearchQueryChanged("FC-002"))
        assertThat(viewModel.uiState.value.filteredMembers).hasSize(1)
        assertThat(viewModel.uiState.value.filteredMembers[0].memberId).isEqualTo("FC-002")

        // Empty search
        viewModel.onAction(MembersRosterUiAction.SearchQueryChanged(""))
        assertThat(viewModel.uiState.value.filteredMembers).hasSize(2)
    }

    @Test
    fun clickMember_emitsNavigateToMemberDetailEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(MembersRosterUiAction.ClickMember("usr-2"))
            val effect = awaitItem()
            assertThat(effect).isEqualTo(
                MembersRosterUiEffect.NavigateToMemberDetail(fundId, "usr-2")
            )
        }
    }

    @Test
    fun addMemberSheet_opensAndDismisses() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(MembersRosterUiAction.OpenAddMemberSheet)
        assertThat(viewModel.uiState.value.showAddMemberSheet).isTrue()

        viewModel.onAction(MembersRosterUiAction.DismissAddMemberSheet)
        assertThat(viewModel.uiState.value.showAddMemberSheet).isFalse()
    }

    @Test
    fun networkState_updatesOfflineFlag() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        isOnlineFlow.value = false
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }
}
