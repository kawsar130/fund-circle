package com.fundcircle.app.presentation.screens.ledger

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositsUseCase
import com.fundcircle.app.domain.usecase.ObserveExpensesUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModelTest {

    private val observeDepositsUseCase: ObserveDepositsUseCase = mockk(relaxed = true)
    private val observeExpensesUseCase: ObserveExpensesUseCase = mockk(relaxed = true)
    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val testFundId = "fund-101"

    private val sampleAdminUser = UserSession(
        uid = "admin-1",
        email = "admin@fundcircle.app",
        displayName = "Admin",
        photoUrl = null,
        idToken = "token",
        activeCircleId = testFundId
    )

    private val sampleCircle = FundCircle(
        id = testFundId,
        name = "Test Syndicate",
        description = null,
        currency = Currency.BDT,
        adminId = "admin-1",
        adminEmail = "admin@fundcircle.app",
        memberEmails = listOf("admin@fundcircle.app"),
        memberCount = 1,
        totalDeposits = 100_000L,
        totalExpenses = 20_000L,
        recordedBalance = 80_000L,
        isLocked = false,
        createdAt = 1_000L
    )

    private val deposit1 = Deposit(
        id = "dep-1",
        fundId = testFundId,
        memberId = "mem-1",
        memberEmail = "alice@example.com",
        memberName = "Alice Wonderland",
        amount = 100_000L,
        depositDate = 2_000L,
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        note = "Monthly tranche 1"
    )

    private val deposit2 = Deposit(
        id = "dep-2",
        fundId = testFundId,
        memberId = "mem-2",
        memberEmail = "bob@example.com",
        memberName = "Bob Builder",
        amount = 50_000L,
        depositDate = 1_000L,
        paymentMethod = PaymentMethod.CASH,
        note = null
    )

    private val expense1 = Expense(
        id = "exp-1",
        fundId = testFundId,
        amount = 20_000L,
        expenseDate = 2_000L,
        category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
        description = "Office desks",
        paymentMethod = PaymentMethod.CASH,
        paidBy = "Apex Furniture",
        createdBy = "admin-1",
        creatorEmail = "admin@fundcircle.app"
    )

    private val expense2 = Expense(
        id = "exp-2",
        fundId = testFundId,
        amount = 10_000L,
        expenseDate = 1_000L,
        category = ExpenseCategory.TRAVEL_AND_TRANSPORTATION,
        description = "Flight tickets",
        paymentMethod = PaymentMethod.MOBILE_BANKING,
        paidBy = "Biman",
        createdBy = "admin-1",
        creatorEmail = "admin@fundcircle.app"
    )

    private val depositsFlow = MutableStateFlow(listOf(deposit1, deposit2))
    private val expensesFlow = MutableStateFlow(listOf(expense1, expense2))
    private val circleFlow = MutableStateFlow(sampleCircle)
    private val isOnlineFlow = MutableStateFlow(true)
    private val userSessionFlow = MutableStateFlow<UserSession?>(sampleAdminUser)

    private lateinit var viewModel: LedgerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { observeDepositsUseCase(testFundId) } returns depositsFlow
        every { observeExpensesUseCase(testFundId) } returns expensesFlow
        every { observeCircleUseCase(testFundId) } returns circleFlow
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { authRepository.userSession } returns userSessionFlow

        val savedStateHandle = SavedStateHandle(mapOf(LedgerViewModel.KEY_FUND_ID to testFundId))
        viewModel = LedgerViewModel(
            savedStateHandle = savedStateHandle,
            observeDepositsUseCase = observeDepositsUseCase,
            observeExpensesUseCase = observeExpensesUseCase,
            observeCircleUseCase = observeCircleUseCase,
            networkMonitor = networkMonitor,
            authRepository = authRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDepositsAndExpensesAndSetsAdmin() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.isAdmin).isTrue()
        assertThat(state.currency).isEqualTo(Currency.BDT)
        assertThat(state.deposits).containsExactly(deposit1, deposit2)
        assertThat(state.expenses).containsExactly(expense1, expense2)
        assertThat(state.selectedTab).isEqualTo(LedgerTab.DEPOSITS)
    }

    @Test
    fun selectTab_updatesSelectedTab() = runTest {
        advanceUntilIdle()

        viewModel.onAction(LedgerUiAction.SelectTab(LedgerTab.EXPENSES))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedTab).isEqualTo(LedgerTab.EXPENSES)
    }

    @Test
    fun updateSearchQuery_filtersDepositsByMemberNameOrNote() = runTest {
        advanceUntilIdle()

        // Filter by name
        viewModel.onAction(LedgerUiAction.UpdateSearchQuery("Alice"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.deposits).containsExactly(deposit1)

        // Filter by note
        viewModel.onAction(LedgerUiAction.UpdateSearchQuery("tranche"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.deposits).containsExactly(deposit1)

        // Reset query
        viewModel.onAction(LedgerUiAction.UpdateSearchQuery(""))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.deposits).containsExactly(deposit1, deposit2)
    }

    @Test
    fun updateSearchQuery_filtersExpensesByDescriptionOrPaidByOrCategory() = runTest {
        advanceUntilIdle()

        // Filter by description
        viewModel.onAction(LedgerUiAction.UpdateSearchQuery("flight"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.expenses).containsExactly(expense2)

        // Filter by category name
        viewModel.onAction(LedgerUiAction.UpdateSearchQuery("Office & Administration"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.expenses).containsExactly(expense1)
    }

    @Test
    fun filterByPaymentMethod_filtersBothDepositsAndExpenses() = runTest {
        advanceUntilIdle()

        viewModel.onAction(LedgerUiAction.FilterByPaymentMethod(PaymentMethod.BANK_TRANSFER))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.deposits).containsExactly(deposit1)
        assertThat(viewModel.uiState.value.expenses).isEmpty()

        viewModel.onAction(LedgerUiAction.FilterByPaymentMethod(PaymentMethod.CASH))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.deposits).containsExactly(deposit2)
        assertThat(viewModel.uiState.value.expenses).containsExactly(expense1)
    }

    @Test
    fun filterByCategory_filtersExpensesOnly() = runTest {
        advanceUntilIdle()

        viewModel.onAction(LedgerUiAction.FilterByCategory(ExpenseCategory.TRAVEL_AND_TRANSPORTATION))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.expenses).containsExactly(expense2)
        assertThat(viewModel.uiState.value.deposits).containsExactly(deposit1, deposit2)

        viewModel.onAction(LedgerUiAction.FilterByCategory(null))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.expenses).containsExactly(expense1, expense2)
    }

    @Test
    fun clickTransaction_emitsNavigateToDetailEffect() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(LedgerUiAction.ClickTransaction(txId = "dep-1", isExpense = false))
            val effect = awaitItem()
            assertThat(effect).isEqualTo(
                LedgerUiEffect.NavigateToDetail(
                    fundId = testFundId,
                    txId = "dep-1",
                    isExpense = false
                )
            )
        }
    }

    @Test
    fun clickAddTransaction_emitsOpenAddTransactionSheetEffect() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(LedgerUiAction.ClickAddTransaction)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(
                LedgerUiEffect.OpenAddTransactionSheet(fundId = testFundId)
            )
        }
    }

    @Test
    fun navigateBack_emitsNavigateBackEffect() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(LedgerUiAction.NavigateBack)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(LedgerUiEffect.NavigateBack)
        }
    }

    @Test
    fun networkOffline_updatesIsOfflineState() = runTest {
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isOffline).isFalse()

        isOnlineFlow.value = false
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun circleLocked_updatesIsLockedState() = runTest {
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isLocked).isFalse()

        circleFlow.value = sampleCircle.copy(isLocked = true)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLocked).isTrue()
    }
}
