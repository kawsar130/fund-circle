package com.fundcircle.app.presentation.screens.transaction

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.fundcircle.app.domain.usecase.ObserveCircleMembersUseCase
import com.fundcircle.app.domain.usecase.ObserveCircleUseCase
import com.fundcircle.app.domain.usecase.RecordDepositUseCase
import com.fundcircle.app.domain.usecase.RecordExpenseUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
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
class RecordTransactionViewModelTest {

    private val observeCircleUseCase: ObserveCircleUseCase = mockk(relaxed = true)
    private val observeCircleMembersUseCase: ObserveCircleMembersUseCase = mockk(relaxed = true)
    private val recordDepositUseCase: RecordDepositUseCase = mockk(relaxed = true)
    private val recordExpenseUseCase: RecordExpenseUseCase = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val testFundId = "fund-202"

    private val sampleMember = CircleMember(
        userId = "user-1",
        memberId = "FC-001",
        email = "member@fundcircle.app",
        name = "Rahim Uddin",
        role = MemberRole.MEMBER,
        status = MemberStatus.ACTIVE,
        totalContributed = 100_000L,
        joinedAt = 1_000L,
        lastActiveAt = 2_000L
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

    private val sampleUser = UserSession(
        uid = "admin-1",
        email = "admin@fundcircle.app",
        displayName = "Admin User",
        photoUrl = null,
        idToken = "token",
        activeCircleId = testFundId
    )

    private val circleFlow = MutableStateFlow(sampleCircle)
    private val membersFlow = MutableStateFlow(listOf(sampleMember))
    private val userSessionFlow = MutableStateFlow<UserSession?>(sampleUser)

    private lateinit var viewModel: RecordTransactionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { observeCircleUseCase(testFundId) } returns circleFlow
        every { observeCircleMembersUseCase(testFundId) } returns membersFlow
        every { authRepository.userSession } returns userSessionFlow

        val savedStateHandle = SavedStateHandle(mapOf(RecordTransactionViewModel.KEY_FUND_ID to testFundId))
        viewModel = RecordTransactionViewModel(
            savedStateHandle = savedStateHandle,
            observeCircleUseCase = observeCircleUseCase,
            observeCircleMembersUseCase = observeCircleMembersUseCase,
            recordDepositUseCase = recordDepositUseCase,
            recordExpenseUseCase = recordExpenseUseCase,
            authRepository = authRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsMembersAndSetsCurrency() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.currency).isEqualTo(Currency.BDT)
        assertThat(state.members).containsExactly(sampleMember)
        assertThat(state.selectedMember).isEqualTo(sampleMember)
        assertThat(state.mode).isEqualTo(RecordTransactionMode.DEPOSIT)
    }

    @Test
    fun updateAmount_restrictsToDigitsOnly_rejectingDecimalsAndSymbols() = runTest {
        viewModel.onAction(RecordTransactionUiAction.UpdateAmount("100.50"))
        assertThat(viewModel.uiState.value.amountInput).isEqualTo("10050")

        viewModel.onAction(RecordTransactionUiAction.UpdateAmount("abc75000xyz"))
        assertThat(viewModel.uiState.value.amountInput).isEqualTo("75000")
    }

    @Test
    fun updateDate_whenFutureDate_setsDateError() = runTest {
        val futureDate = System.currentTimeMillis() + 100_000L
        viewModel.onAction(RecordTransactionUiAction.UpdateDate(futureDate))

        assertThat(viewModel.uiState.value.dateError)
            .isEqualTo(UiText.StringResource(R.string.error_future_date))

        val pastDate = System.currentTimeMillis() - 100_000L
        viewModel.onAction(RecordTransactionUiAction.UpdateDate(pastDate))

        assertThat(viewModel.uiState.value.dateError).isNull()
    }

    @Test
    fun submitDeposit_whenAmountEmptyOrZero_setsAmountError() = runTest {
        advanceUntilIdle()

        viewModel.onAction(RecordTransactionUiAction.UpdateAmount(""))
        viewModel.onAction(RecordTransactionUiAction.Submit)

        assertThat(viewModel.uiState.value.amountError)
            .isEqualTo(UiText.StringResource(R.string.error_amount_required))

        viewModel.onAction(RecordTransactionUiAction.UpdateAmount("0"))
        viewModel.onAction(RecordTransactionUiAction.Submit)

        assertThat(viewModel.uiState.value.amountError)
            .isEqualTo(UiText.StringResource(R.string.error_amount_required))
    }

    @Test
    fun submitDeposit_success_callsRecordDepositUseCase_andEmitsEffects() = runTest {
        advanceUntilIdle()

        coEvery { recordDepositUseCase(any(), any(), any()) } returns Result.success(Unit)
        coEvery { recordDepositUseCase(any(), any()) } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onAction(RecordTransactionUiAction.UpdateAmount("50000"))
            viewModel.onAction(RecordTransactionUiAction.SelectMember(sampleMember))
            viewModel.onAction(RecordTransactionUiAction.SelectPaymentMethod(PaymentMethod.BANK_TRANSFER))
            viewModel.onAction(RecordTransactionUiAction.UpdateNote("Tranche 1"))
            viewModel.onAction(RecordTransactionUiAction.Submit)
            advanceUntilIdle()

            val snackbarEffect = awaitItem()
            assertThat(snackbarEffect).isInstanceOf(RecordTransactionUiEffect.ShowSnackbar::class.java)

            val savedEffect = awaitItem()
            assertThat(savedEffect).isEqualTo(RecordTransactionUiEffect.TransactionSaved)
        }

        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
    }

    @Test
    fun submitExpense_whenDescriptionTooShort_setsDescriptionError() = runTest {
        advanceUntilIdle()

        viewModel.onAction(RecordTransactionUiAction.SelectMode(RecordTransactionMode.EXPENSE))
        viewModel.onAction(RecordTransactionUiAction.UpdateAmount("12000"))
        viewModel.onAction(RecordTransactionUiAction.UpdateDescription("ab")) // 2 chars
        viewModel.onAction(RecordTransactionUiAction.Submit)

        assertThat(viewModel.uiState.value.descriptionError)
            .isEqualTo(UiText.StringResource(R.string.error_description_too_short))
    }

    @Test
    fun submitExpense_success_callsRecordExpenseUseCase_andEmitsEffects() = runTest {
        advanceUntilIdle()

        coEvery { recordExpenseUseCase(any(), any(), any()) } returns Result.success(Unit)
        coEvery { recordExpenseUseCase(any(), any()) } returns Result.success(Unit)

        viewModel.uiEffect.test {
            viewModel.onAction(RecordTransactionUiAction.SelectMode(RecordTransactionMode.EXPENSE))
            viewModel.onAction(RecordTransactionUiAction.UpdateAmount("25000"))
            viewModel.onAction(RecordTransactionUiAction.SelectCategory(ExpenseCategory.OFFICE_AND_ADMINISTRATION))
            viewModel.onAction(RecordTransactionUiAction.UpdateDescription("Office printing papers"))
            viewModel.onAction(RecordTransactionUiAction.UpdatePaidBy("Apex Paper Mills"))
            viewModel.onAction(RecordTransactionUiAction.SelectPaymentMethod(PaymentMethod.CASH))
            viewModel.onAction(RecordTransactionUiAction.Submit)
            advanceUntilIdle()

            val snackbarEffect = awaitItem()
            assertThat(snackbarEffect).isInstanceOf(RecordTransactionUiEffect.ShowSnackbar::class.java)

            val savedEffect = awaitItem()
            assertThat(savedEffect).isEqualTo(RecordTransactionUiEffect.TransactionSaved)
        }

        assertThat(viewModel.uiState.value.isSubmitting).isFalse()
    }

    @Test
    fun dismiss_emitsDismissSheetEffect() = runTest {
        viewModel.uiEffect.test {
            viewModel.onAction(RecordTransactionUiAction.Dismiss)
            val effect = awaitItem()
            assertThat(effect).isEqualTo(RecordTransactionUiEffect.DismissSheet)
        }
    }
}
