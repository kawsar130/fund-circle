package com.fundcircle.app.presentation.screens.members.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.GetMemberDepositsUseCase
import com.fundcircle.app.domain.usecase.GetMemberKycUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveMemberUseCase
import com.fundcircle.app.domain.usecase.ToggleMemberStatusUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
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
class MemberDetailViewModelTest {

    private val observeMemberUseCase: ObserveMemberUseCase = mockk(relaxed = true)
    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val getMemberDepositsUseCase: GetMemberDepositsUseCase = mockk(relaxed = true)
    private val getMemberKycUseCase: GetMemberKycUseCase = mockk(relaxed = true)
    private val toggleMemberStatusUseCase: ToggleMemberStatusUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val fundId = "fund-test-101"
    private val targetUserId = "usr-2"

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
        totalDeposits = 250_000L,
        totalExpenses = 0L,
        recordedBalance = 250_000L,
        isLocked = false,
        createdAt = 1000L
    )

    private val sampleMember = CircleMember(
        userId = targetUserId,
        memberId = "FC-002",
        email = "rahim@example.com",
        name = "Rahim Uddin",
        phone = "01722222222",
        role = MemberRole.MEMBER,
        status = MemberStatus.ACTIVE,
        totalContributed = 250_000L,
        joinedAt = 1000L,
        lastActiveAt = 2000L
    )

    private val sampleDeposits = listOf(
        Deposit(
            id = "dep-1",
            fundId = fundId,
            memberId = targetUserId,
            memberEmail = "rahim@example.com",
            memberName = "Rahim Uddin",
            amount = 150_000L,
            depositDate = 2000L,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            note = "Seed capital",
            isVoid = false,
            voidMetadata = null
        ),
        Deposit(
            id = "dep-2",
            fundId = fundId,
            memberId = targetUserId,
            memberEmail = "rahim@example.com",
            memberName = "Rahim Uddin",
            amount = 100_000L,
            depositDate = 3000L,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            note = "Tranche 2",
            isVoid = false,
            voidMetadata = null
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { authRepository.userSession } returns userSessionFlow
        every { observeCircleUseCase(fundId) } returns flowOf(sampleCircle)
        every { observeMemberUseCase(any(), any()) } returns flowOf(sampleMember)
        every { getMemberDepositsUseCase(any(), any(), any()) } returns flowOf(sampleDeposits)
        every { getMemberDepositsUseCase(any(), any()) } returns flowOf(sampleDeposits)
        coEvery { getMemberKycUseCase(any(), any()) } returns Result.failure(FundCircleError.NotFound.ResourceNotFound)
        userSessionFlow.value = sampleAdmin
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MemberDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("fundId" to fundId, "userId" to targetUserId))
        return MemberDetailViewModel(
            observeMemberUseCase = observeMemberUseCase,
            observeCircleUseCase = observeCircleUseCase,
            getMemberDepositsUseCase = getMemberDepositsUseCase,
            getMemberKycUseCase = getMemberKycUseCase,
            toggleMemberStatusUseCase = toggleMemberStatusUseCase,
            authRepository = authRepository,
            networkMonitor = networkMonitor,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun init_loadsMemberAndDeposits_setsAdminAndSelfFlags() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.member?.name).isEqualTo("Rahim Uddin")
        assertThat(state.deposits).hasSize(2)
        assertThat(state.isAdmin).isTrue()
        assertThat(state.isSelf).isFalse()
    }

    @Test
    fun clickDeposit_emitsNavigateToTransactionDetailEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(MemberDetailUiAction.ClickDeposit("dep-1"))
            val effect = awaitItem()
            assertThat(effect).isEqualTo(
                MemberDetailUiEffect.NavigateToTransactionDetail(fundId, "dep-1", isExpense = false)
            )
        }
    }

    @Test
    fun statusChange_deactivateConfirmation_updatesStatusSuccessfully() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery {
            toggleMemberStatusUseCase(fundId, targetUserId, MemberStatus.INACTIVE)
        } returns Result.success(Unit)

        viewModel.onAction(MemberDetailUiAction.RequestStatusChange(MemberStatus.INACTIVE))
        assertThat(viewModel.uiState.value.pendingStatusChange).isEqualTo(MemberStatus.INACTIVE)

        viewModel.uiEffect.test {
            viewModel.onAction(MemberDetailUiAction.ConfirmStatusChange)
            advanceUntilIdle()
            val effect = awaitItem()
            assertThat(effect).isInstanceOf(MemberDetailUiEffect.ShowSnackbar::class.java)
        }

        assertThat(viewModel.uiState.value.pendingStatusChange).isNull()
        assertThat(viewModel.uiState.value.isUpdatingStatus).isFalse()
    }

    @Test
    fun toggleRevealNid_flipsRevealedState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isNidRevealed).isFalse()
        viewModel.onAction(MemberDetailUiAction.ToggleRevealNid)
        assertThat(viewModel.uiState.value.isNidRevealed).isTrue()
        viewModel.onAction(MemberDetailUiAction.ToggleRevealNid)
        assertThat(viewModel.uiState.value.isNidRevealed).isFalse()
    }

    @Test
    fun navigateToProfileEdit_emitsEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(MemberDetailUiAction.NavigateToProfileEdit)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(MemberDetailUiEffect.NavigateToProfileEdit(fundId))
        }
    }

    @Test
    fun confirmStatusChange_creatorCannotBeDeactivated() = runTest {
        // Target is the creator ("usr-admin")
        val savedStateHandle = SavedStateHandle(mapOf("fundId" to fundId, "userId" to "usr-admin"))
        val viewModel = MemberDetailViewModel(
            observeMemberUseCase = observeMemberUseCase,
            observeCircleUseCase = observeCircleUseCase,
            getMemberDepositsUseCase = getMemberDepositsUseCase,
            getMemberKycUseCase = getMemberKycUseCase,
            toggleMemberStatusUseCase = toggleMemberStatusUseCase,
            authRepository = authRepository,
            networkMonitor = networkMonitor,
            savedStateHandle = savedStateHandle
        )
        advanceUntilIdle()

        viewModel.onAction(MemberDetailUiAction.RequestStatusChange(MemberStatus.INACTIVE))
        viewModel.onAction(MemberDetailUiAction.ConfirmStatusChange)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }
}
