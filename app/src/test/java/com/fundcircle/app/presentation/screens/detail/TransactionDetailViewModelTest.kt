package com.fundcircle.app.presentation.screens.detail

import com.fundcircle.app.domain.model.WriteStatus
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.model.VoidMetadata
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.ObserveDepositUseCase
import com.fundcircle.app.domain.usecase.ObserveExpenseUseCase
import com.fundcircle.app.domain.usecase.VoidTransactionUseCase
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
class TransactionDetailViewModelTest {

    private val observeDepositUseCase: ObserveDepositUseCase = mockk(relaxed = true)
    private val observeExpenseUseCase: ObserveExpenseUseCase = mockk(relaxed = true)
    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val voidTransactionUseCase: VoidTransactionUseCase = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val fundId = "fund-circle-1"
    private val depositId = "dep-101"
    private val expenseId = "exp-202"

    private val isOnlineFlow = MutableStateFlow(true)
    private val userSessionFlow = MutableStateFlow<UserSession?>(null)

    private val sampleAdminUser = UserSession(
        uid = "admin-uid-1",
        email = "admin@fundcircle.app",
        displayName = "Circle Admin",
        photoUrl = null,
        idToken = "token-1",
        activeCircleId = fundId
    )

    private val sampleMemberUser = UserSession(
        uid = "member-uid-2",
        email = "member@fundcircle.app",
        displayName = "Regular Member",
        photoUrl = null,
        idToken = "token-2",
        activeCircleId = fundId
    )

    private val sampleCircle = FundCircle(
        id = fundId,
        name = "Capital Alpha",
        description = "Investment Pool",
        currency = Currency.BDT,
        adminId = "admin-uid-1",
        adminEmail = "admin@fundcircle.app",
        memberEmails = listOf("admin@fundcircle.app", "member@fundcircle.app"),
        memberCount = 2,
        totalDeposits = 100_000L,
        totalExpenses = 25_000L,
        recordedBalance = 75_000L,
        isLocked = false,
        createdAt = 1_000L
    )

    private val sampleDeposit = Deposit(
        id = depositId,
        fundId = fundId,
        memberId = "mem-1",
        memberEmail = "member@fundcircle.app",
        memberName = "Tareq Hasan",
        amount = 100_000L,
        depositDate = 2_000L,
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        note = "Tranche 1",
        isVoid = false
    )

    private val sampleExpense = Expense(
        id = expenseId,
        fundId = fundId,
        amount = 25_000L,
        expenseDate = 2_500L,
        category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
        description = "Office chairs",
        paymentMethod = PaymentMethod.MOBILE_BANKING,
        paidBy = "Apex Stationery",
        createdBy = "admin-uid-1",
        creatorEmail = "admin@fundcircle.app",
        isVoid = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { authRepository.userSession } returns userSessionFlow
        every { observeCircleUseCase(fundId) } returns flowOf(sampleCircle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        isExpense: Boolean = false,
        txId: String = if (isExpense) expenseId else depositId
    ): TransactionDetailViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                TransactionDetailViewModel.KEY_FUND_ID to fundId,
                TransactionDetailViewModel.KEY_TX_ID to txId,
                TransactionDetailViewModel.KEY_IS_EXPENSE to isExpense
            )
        )
        return TransactionDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeDepositUseCase = observeDepositUseCase,
            observeExpenseUseCase = observeExpenseUseCase,
            observeCircleUseCase = observeCircleUseCase,
            voidTransactionUseCase = voidTransactionUseCase,
            networkMonitor = networkMonitor,
            authRepository = authRepository
        )
    }

    @Test
    fun init_loadsDepositDetails_andResolvesAdminState() = runTest {
        userSessionFlow.value = sampleAdminUser
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)

        val viewModel = createViewModel(isExpense = false, txId = depositId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.deposit).isEqualTo(sampleDeposit)
        assertThat(state.isAdmin).isTrue()
        assertThat(state.adminEmail).isEqualTo("admin@fundcircle.app")
        assertThat(state.currency).isEqualTo(Currency.BDT)
        assertThat(state.isVoid).isFalse()
        assertThat(state.originalAmount).isEqualTo(100_000L)
    }

    @Test
    fun init_loadsExpenseDetails_successfully() = runTest {
        userSessionFlow.value = sampleMemberUser
        every { observeExpenseUseCase(fundId, expenseId) } returns flowOf(sampleExpense)

        val viewModel = createViewModel(isExpense = true, txId = expenseId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.expense).isEqualTo(sampleExpense)
        assertThat(state.isAdmin).isFalse()
        assertThat(state.isExpense).isTrue()
        assertThat(state.originalAmount).isEqualTo(25_000L)
    }

    @Test
    fun init_whenReversed_setsVoidAndExposesAuditMetadata() = runTest {
        val voidedDeposit = sampleDeposit.copy(
            isVoid = true,
            voidMetadata = VoidMetadata(
                reason = "Duplicate voucher entered",
                voidedAt = 5_000L,
                voidedBy = "admin@fundcircle.app"
            )
        )
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(voidedDeposit)

        val viewModel = createViewModel(isExpense = false, txId = depositId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isVoid).isTrue()
        assertThat(state.deposit?.voidMetadata?.reason).isEqualTo("Duplicate voucher entered")
    }

    @Test
    fun init_whenTransactionNotFound_emitsError() = runTest {
        every { observeDepositUseCase(fundId, "dep-missing") } returns flowOf(null)

        val viewModel = createViewModel(isExpense = false, txId = "dep-missing")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.deposit).isNull()
        assertThat(state.errorMessage).isNotNull()
    }

    @Test
    fun networkState_whenOffline_updatesIsOfflineState() = runTest {
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)
        val viewModel = createViewModel(isExpense = false)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isFalse()

        isOnlineFlow.value = false
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun clickBack_emitsNavigateBackEffect() = runTest {
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)
        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.onAction(TransactionDetailUiAction.ClickBack)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(TransactionDetailUiEffect.NavigateBack)
        }
    }

    @Test
    fun openReverseDialog_whenAdminAndNotVoidAndNotLocked_showsDialog() = runTest {
        userSessionFlow.value = sampleAdminUser
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)

        val viewModel = createViewModel(isExpense = false)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showReverseDialog).isFalse()

        viewModel.onAction(TransactionDetailUiAction.OpenReverseDialog)
        assertThat(viewModel.uiState.value.showReverseDialog).isTrue()

        viewModel.onAction(TransactionDetailUiAction.DismissReverseDialog)
        assertThat(viewModel.uiState.value.showReverseDialog).isFalse()
    }

    @Test
    fun openReverseDialog_whenNotAdmin_doesNotShowDialog() = runTest {
        userSessionFlow.value = sampleMemberUser
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)

        val viewModel = createViewModel(isExpense = false)
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailUiAction.OpenReverseDialog)
        assertThat(viewModel.uiState.value.showReverseDialog).isFalse()
    }

    @Test
    fun openReverseDialog_whenCircleLocked_doesNotShowDialog() = runTest {
        userSessionFlow.value = sampleAdminUser
        val lockedCircle = sampleCircle.copy(isLocked = true)
        every { observeCircleUseCase(fundId) } returns flowOf(lockedCircle)
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)

        val viewModel = createViewModel(isExpense = false)
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailUiAction.OpenReverseDialog)
        assertThat(viewModel.uiState.value.showReverseDialog).isFalse()
    }

    @Test
    fun openReverseDialog_whenAlreadyVoid_doesNotShowDialog() = runTest {
        userSessionFlow.value = sampleAdminUser
        val voidedDeposit = sampleDeposit.copy(isVoid = true)
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(voidedDeposit)

        val viewModel = createViewModel(isExpense = false)
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailUiAction.OpenReverseDialog)
        assertThat(viewModel.uiState.value.showReverseDialog).isFalse()
    }

    @Test
    fun confirmReversal_whenSuccessful_updatesStateAndEmitsSnackbar() = runTest {
        userSessionFlow.value = sampleAdminUser
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)
        coEvery {
            voidTransactionUseCase(
                fundId = fundId,
                isExpense = false,
                transactionId = depositId,
                originalAmount = 100_000L,
                voidReason = "Wrong entry recorded",
                adminEmail = "admin@fundcircle.app"
            )
        } returns Result.success(WriteStatus.SYNCED)

        val viewModel = createViewModel(isExpense = false)
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailUiAction.OpenReverseDialog)
        assertThat(viewModel.uiState.value.showReverseDialog).isTrue()

        viewModel.uiEffect.test {
            viewModel.onAction(TransactionDetailUiAction.ConfirmReversal("Wrong entry recorded"))
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TransactionDetailUiEffect.ShowSnackbar::class.java)
        }

        assertThat(viewModel.uiState.value.showReverseDialog).isFalse()
        assertThat(viewModel.uiState.value.isReversing).isFalse()
    }

    @Test
    fun confirmReversal_whenFailure_updatesErrorMessageAndEmitsSnackbar() = runTest {
        userSessionFlow.value = sampleAdminUser
        every { observeDepositUseCase(fundId, depositId) } returns flowOf(sampleDeposit)
        coEvery {
            voidTransactionUseCase(
                fundId = fundId,
                isExpense = false,
                transactionId = depositId,
                originalAmount = 100_000L,
                voidReason = "Wrong entry",
                adminEmail = "admin@fundcircle.app"
            )
        } returns Result.failure(FundCircleError.Conflict.TransactionAlreadyVoided(depositId))

        val viewModel = createViewModel(isExpense = false)
        advanceUntilIdle()

        viewModel.onAction(TransactionDetailUiAction.OpenReverseDialog)

        viewModel.uiEffect.test {
            viewModel.onAction(TransactionDetailUiAction.ConfirmReversal("Wrong entry"))
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(TransactionDetailUiEffect.ShowSnackbar::class.java)
        }

        assertThat(viewModel.uiState.value.isReversing).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.onAction(TransactionDetailUiAction.DismissError)
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
