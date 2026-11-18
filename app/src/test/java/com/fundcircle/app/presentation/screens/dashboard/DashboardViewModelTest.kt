package com.fundcircle.app.presentation.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveRecentTransactionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val observeRecentTransactionsUseCase: ObserveRecentTransactionsUseCase = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val testFundId = "fund-circle-101"

    private val sampleAdminUser = UserSession(
        uid = "usr_admin_1",
        email = "admin@fundcircle.app",
        displayName = "Admin User",
        photoUrl = null,
        idToken = "token-admin",
        activeCircleId = testFundId
    )

    private val sampleMemberUser = UserSession(
        uid = "usr_member_2",
        email = "member@fundcircle.app",
        displayName = "Stakeholder Member",
        photoUrl = null,
        idToken = "token-member",
        activeCircleId = testFundId
    )

    private val sampleCircle = FundCircle(
        id = testFundId,
        name = "Green Valley Property Pool",
        description = "Joint property acquisition",
        currency = Currency.BDT,
        adminId = "usr_admin_1",
        adminEmail = "admin@fundcircle.app",
        memberEmails = listOf("admin@fundcircle.app", "member@fundcircle.app"),
        memberCount = 2,
        totalDeposits = 1_600_000L,
        totalExpenses = 150_000L,
        recordedBalance = 1_450_000L,
        isLocked = false,
        createdAt = 1_700_000_000_000L
    )

    private val sampleTransactions = listOf(
        TransactionItem(
            id = "tx-1",
            fundId = testFundId,
            isExpense = false,
            title = "Rahim Uddin",
            subtitle = "12 Nov 2026 • Bank Transfer",
            amount = 100_000L,
            timestamp = 1_700_000_100_000L,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            isVoid = false
        ),
        TransactionItem(
            id = "tx-2",
            fundId = testFundId,
            isExpense = true,
            title = "Office & Administration",
            subtitle = "10 Nov 2026 • Stationery",
            amount = 12_000L,
            timestamp = 1_700_000_050_000L,
            paymentMethod = PaymentMethod.CASH,
            isVoid = true,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION
        )
    )

    private val isOnlineFlow = MutableStateFlow(true)
    private val circleFlow = MutableStateFlow(sampleCircle)
    private val transactionsFlow = MutableStateFlow<List<TransactionItem>>(emptyList())
    private val userSessionFlow = MutableStateFlow<UserSession?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { authRepository.userSession } returns userSessionFlow
        every { observeCircleUseCase(testFundId) } returns circleFlow
        every { observeRecentTransactionsUseCase(testFundId, 5) } returns transactionsFlow

        circleFlow.value = sampleCircle
        userSessionFlow.value = sampleAdminUser
        transactionsFlow.value = sampleTransactions
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(fundId: String = testFundId): DashboardViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(DashboardViewModel.KEY_FUND_ID to fundId))
        return DashboardViewModel(
            savedStateHandle = savedStateHandle,
            observeCircleUseCase = observeCircleUseCase,
            observeRecentTransactionsUseCase = observeRecentTransactionsUseCase,
            networkMonitor = networkMonitor,
            authRepository = authRepository
        )
    }

    @Test
    fun initialState_loadsDataSuccessfullyAndCalculatesAdminCorrectly() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.circle).isEqualTo(sampleCircle)
        // Invariant 2: Aggregates read directly from parent circle
        assertThat(state.circle?.recordedBalance).isEqualTo(1_450_000L)
        assertThat(state.circle?.totalDeposits).isEqualTo(1_600_000L)
        assertThat(state.circle?.totalExpenses).isEqualTo(150_000L)
        assertThat(state.recentTransactions).hasSize(2)
        // Admin user matches circle.adminId
        assertThat(state.isAdmin).isTrue()
        assertThat(state.isOffline).isFalse()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun stakeholderUser_identifiesAsNonAdmin() = runTest {
        userSessionFlow.value = sampleMemberUser
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isAdmin).isFalse()
    }

    @Test
    fun networkOfflineTransition_updatesIsOfflineState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isFalse()

        isOnlineFlow.value = false
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()

        isOnlineFlow.value = true
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isFalse()
    }

    @Test
    fun emptyTransactions_setsEmptyListWithoutError() = runTest {
        transactionsFlow.value = emptyList()
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.recentTransactions).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun navigateToLedger_emitsNavigateToLedgerEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(DashboardUiAction.NavigateToLedger)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(DashboardUiEffect.NavigateToLedger(testFundId))
        }
    }

    @Test
    fun navigateToTransactionDetail_emitsNavigateToDetailEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(DashboardUiAction.NavigateToTransactionDetail(txId = "tx-1", isExpense = false))
            val effect = awaitItem()
            assertThat(effect).isEqualTo(
                DashboardUiEffect.NavigateToTransactionDetail(
                    fundId = testFundId,
                    txId = "tx-1",
                    isExpense = false
                )
            )
        }
    }

    @Test
    fun navigateToRecordTransaction_emitsNavigateToRecordEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(DashboardUiAction.NavigateToRecordTransaction)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(DashboardUiEffect.NavigateToRecordTransaction(testFundId))
        }
    }

    @Test
    fun navigateToSettings_emitsNavigateToSettingsEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(DashboardUiAction.NavigateToSettings)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(DashboardUiEffect.NavigateToSettings(testFundId))
        }
    }

    @Test
    fun navigateToMembers_emitsNavigateToMembersEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onAction(DashboardUiAction.NavigateToMembers)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(DashboardUiEffect.NavigateToMembers(testFundId))
        }
    }

    @Test
    fun blankFundId_setsNotFoundError() = runTest {
        val viewModel = createViewModel(fundId = "")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNotNull()
    }

    @Test
    fun useCaseError_setsErrorMessageInState() = runTest {
        every { observeCircleUseCase(testFundId) } returns flow { throw RuntimeException("Firestore stream failed") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNotNull()
    }

    @Test
    fun launch_persistsActiveCircleId() = runTest {
        createViewModel()
        advanceUntilIdle()

        io.mockk.coVerify(exactly = 1) { authRepository.setActiveCircleId(testFundId) }
    }

    @Test
    fun dismissError_clearsErrorMessage() = runTest {
        every { observeCircleUseCase(testFundId) } returns flow { throw RuntimeException("Stream failed") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.onAction(DashboardUiAction.DismissError)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun refresh_reloadsDashboardData() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(DashboardUiAction.Refresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.circle).isEqualTo(sampleCircle)
    }
}
